package vote.resources

import io.ktor.application.call
import io.ktor.auth.authenticate
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
import vote.auth.AuthContext
import vote.db.Database
import vote.db.jooq.tables.records.PollRecord
import vote.db.jooq.tables.records.ResponseRecord
import vote.db.query.*
import vote.services.ResultsCalculator
import vote.util.nullable
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@KtorExperimentalAPI
class VoteResource @Inject constructor(
        private val db: Database,
        private val pollQueries: PollQueries,
        private val responseQueries: ResponseQueries,
        private val resultsCalculator: ResultsCalculator
) : Resource, VoteApi {
    override fun register(rt: Route) {
        with (rt) {
            authenticate {
                post("/polls") {
                    withAuthContext {
                        val body = call.receiveJson(PollCreateRequest.serializer())
                        call.respondJson(Poll.serializer(), createPoll(body))
                    }
                }
                get("/polls/{id}/response") {
                    withAuthContext {
                        val id = call.parameters.getUUID("id")
                        call.respondJson(PollResponse.serializer().nullable, getResponse(id))
                    }
                }
                put("/polls/{id}/response") {
                    withAuthContext {
                        val id = call.parameters.getUUID("id")
                        val body = call.receiveJson(PollResponse.serializer())
                        call.respondJson(UnitSerializer, submitResponse(id, body))
                    }
                }
            }
            get("/polls/{id}") {
                val id = call.parameters.getUUID("id")
                call.respondJson(Poll.serializer().nullable, getPoll(id))
            }
            get("/polls/{id}/results") {
                val id = call.parameters.getUUID("id")
                call.respondJson(PollResults.serializer().nullable, getResults(id))
            }
        }
    }

    override suspend fun createPoll(pollCreateRequest: PollCreateRequest): Poll {
        val userId = coroutineContext[AuthContext]!!.userId.id
        val id = db.transaction { q ->
            q.run(pollQueries.createPoll(pollCreateRequest.title, pollCreateRequest.questions, userId))
        }
        return Poll(
                id = id,
                title = pollCreateRequest.title,
                questions = pollCreateRequest.questions
        )
    }

    override suspend fun getPoll(id: UUID): Poll? {
        val pr = db.transaction { q ->
            q.run(pollQueries.getPoll(id))
        }
        return pr?.toApi()
    }

    override suspend fun getResponse(pollId: UUID): PollResponse? {
        val userId = coroutineContext[AuthContext]!!.userId.id
        val resp = db.transaction { q ->
            q.run(responseQueries.getResponse(pollId, userId))
        }
        return resp?.toApi()
    }

    override suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        val userId = coroutineContext[AuthContext]!!.userId.id
        db.transaction { q ->
            val pAsync = async { q.run(pollQueries.getPoll(pollId))?.toApi() }
            val rAsync = async { q.run(responseQueries.getResponse(pollId, userId)) }
            val p = pAsync.await() ?: throw NotFoundException("Poll with ID {$pollId} not found")

            // TODO more validation (extract to service?)
            if (p.questions.size != response.responses.size) throw BadRequestException("Response list does not match question list")

            val existing = rAsync.await()
            if (existing == null) {
                q.run(responseQueries.createResponse(pollId, userId, response))
            } else {
                q.run(responseQueries.updateResponse(existing.id, response))
            }
        }
    }

    override suspend fun getResults(pollId: UUID): PollResults? {
        val (poll, resps) = db.transaction { q ->
            val poll = async { q.run(pollQueries.getPoll(pollId)) }
            val resps = async { q.run(responseQueries.getAllResponses(pollId)) }
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
