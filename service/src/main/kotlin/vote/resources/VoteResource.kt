package vote.resources

import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.async
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import vote.api.UUID
import vote.api.v1.*
import vote.db.DaoProvider
import vote.db.PollDao
import vote.db.Transactional
import vote.db.ResponseDao
import vote.db.jooq.tables.records.PollRecord
import vote.db.jooq.tables.records.ResponseRecord
import vote.services.ResultsCalculator
import vote.util.nullable
import javax.inject.Inject

@KtorExperimentalAPI
class VoteResource @Inject constructor(
        private val tx: Transactional,
        private val pollDao: DaoProvider<PollDao>,
        private val responseDao: DaoProvider<ResponseDao>,
        private val resultsCalculator: ResultsCalculator
) : Resource, VoteApi {
    override fun register(rt: Route) {
        rt.post("/polls") {
            val body = call.receiveJson(PollCreateRequest.serializer())
            call.respondJson(Poll.serializer(), createPoll(body))
        }
        rt.get("/polls/{id}") {
            val id = call.parameters.getUUID("id")
            call.respondJson(Poll.serializer().nullable, getPoll(id))
        }
        rt.get("/polls/{id}/response") {
            val id = call.parameters.getUUID("id")
            call.respondJson(PollResponse.serializer().nullable, getResponse(id))
        }
        rt.put("/polls/{id}/response") {
            val id = call.parameters.getUUID("id")
            val body = call.receiveJson(PollResponse.serializer())
            call.respondJson(UnitSerializer, submitResponse(id, body))
        }
        rt.get("/polls/{id}/results") {
            val id = call.parameters.getUUID("id")
            call.respondJson(PollResults.serializer().nullable, getResults(id))
        }
    }

    override suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll {
        val id = tx.withDao(pollDao) { dao ->
            dao.createPoll(pollCreateRequest.title, pollCreateRequest.questions)
        }
        return Poll(
                id = id,
                title = pollCreateRequest.title,
                questions = pollCreateRequest.questions
        )
    }

    override suspend fun getPoll(id: UUID): Poll? {
        val pr = tx.withDao(pollDao) { dao ->
            dao.getPoll(id)
        }
        return pr?.toApi()
    }

    override suspend fun getResponse(pollId: UUID): PollResponse? {
        return null // TODO need users
    }

    override suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        tx.withDaos(pollDao, responseDao) { pollDao, responseDao ->
            val p = pollDao.getPoll(pollId)?.toApi() ?: throw NotFoundException("Poll with ID {$pollId} not found")
            if (p.questions.size != response.responses.size) throw BadRequestException("Response list does not match question list")
            responseDao.createResponse(pollId, response)
        }
    }

    override suspend fun getResults(pollId: UUID): PollResults? {
        val (poll, resps) = tx.withDaos(pollDao, responseDao) { pollDao, responseDao ->
            val poll = async { pollDao.getPoll(pollId) }
            val resps = async { responseDao.getAllResponses(pollId) }
            poll.await() to resps.await()
        }
        if (poll == null || resps.isEmpty()) return null
        return resultsCalculator.calculateResults(poll.toApi(), resps.map { it.toApi() })
    }

    private fun PollRecord.toApi(): Poll {
        return Poll(
                id = id,
                title = title,
                questions = Json.parse(Question.serializer().list, questions)
        )
    }

    private fun ResponseRecord.toApi(): PollResponse {
        return PollResponse(
                responses = Json.parse(Response.serializer().list, responses)
        )
    }
}
