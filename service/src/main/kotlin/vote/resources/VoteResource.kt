package vote.resources

import io.ktor.auth.authenticate
import io.ktor.features.NotFoundException
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import juggernaut0.multiplatform.ktor.handleApi
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import vote.api.UUID
import vote.api.v1.*
import vote.auth.AuthContext
import vote.auth.UserId
import vote.db.Database
import vote.db.jooq.tables.records.PollRecord
import vote.db.jooq.tables.records.ResponseRecord
import vote.db.jooq.tables.records.VoteUserRecord
import vote.db.query.*
import vote.services.PollValidator
import vote.services.ResultsCalculator
import vote.util.BadRequestException
import vote.util.UnauthorizedException
import javax.inject.Inject

@KtorExperimentalAPI
class VoteResource @Inject constructor(
        private val db: Database,
        private val pollQueries: PollQueries,
        private val responseQueries: ResponseQueries,
        private val resultsCalculator: ResultsCalculator,
        private val pollValidator: PollValidator,
        private val json: Json
) : Resource {
    override fun register(rt: Route) {
        with (rt) {
            authenticate {
                handleApi(getPollHistory) { getPollHistory(auth as UserId) }
                handleApi(createPoll) { body -> createPoll(auth as UserId, body) }
                handleApi(isCreator) { isCreator(auth as UserId, params.getUUID("id")) }
                handleApi(getResponse) { getResponse(auth as UserId, params.getUUID("id")) }
                handleApi(getResponses) { getResponses(auth as UserId, params.getUUID("id")) }
                handleApi(submitResponse) { body -> submitResponse(auth as UserId, params.getUUID("id"), body) }
                handleApi(deactivateResponse) { deactivateResponse(auth as UserId, params.getUUID("id"), params.getUUID("respId")) }
            }
            handleApi(getPoll) { getPoll(params.getUUID("id")) }
            handleApi(getResults) { getResults(params.getUUID("id")) }
        }
    }

    private suspend fun createPoll(userId: UserId, pollCreateRequest: PollCreateRequest): Poll {
        val validation = pollValidator.validate(pollCreateRequest)
        if (validation.isNotEmpty()) throw BadRequestException(validation.toString())
        val id = db.transaction { q ->
            q.run(pollQueries.createPoll(pollCreateRequest.title, pollCreateRequest.questions, userId.id))
        }
        return Poll(
                id = id,
                title = pollCreateRequest.title,
                questions = pollCreateRequest.questions
        )
    }

    private suspend fun getPoll(id: UUID): Poll? {
        val pr = db.transaction { q ->
            q.run(pollQueries.getPoll(id))
        }
        return pr?.toApi()
    }

    private suspend fun isCreator(userId: UserId, id: UUID): Boolean {
        return db.transaction { q ->
            q.run(pollQueries.getPoll(id))?.createdBy == userId.id
        }
    }

    private suspend fun getResponse(userId: UserId, pollId: UUID): PollResponse? {
        val resp = db.transaction { q ->
            q.run(responseQueries.getResponse(pollId, userId.id))
        }
        return resp?.toApi()
    }

    private suspend fun getResponses(userId: UserId, pollId: UUID): List<PollResponseDetails> {
        return db.transaction { q ->
            val poll = q.run(pollQueries.getPoll(pollId)) ?: throw NotFoundException("Poll with ID {$pollId} not found")
            if (poll.createdBy != userId.id) throw UnauthorizedException("Cannot view responses of a poll you didn't create")
            q.run(responseQueries.getAllResponsesWithUsers(pollId)).map { it.toApi() }
        }
    }

    private suspend fun submitResponse(userId: UserId, pollId: UUID, response: PollResponse) {
        db.transaction { q ->
            val pAsync = async { q.run(pollQueries.getPoll(pollId))?.toApi() }
            val rAsync = async { q.run(responseQueries.getResponse(pollId, userId.id)) }
            val p = pAsync.await() ?: throw NotFoundException("Poll with ID {$pollId} not found")

            val validation = pollValidator.validateResponse(p, response)
            if (validation.isNotEmpty()) throw BadRequestException(validation.toString())

            val existing = rAsync.await()
            if (existing == null) {
                q.run(responseQueries.createResponse(pollId, userId.id, response))
            } else {
                q.run(responseQueries.updateResponse(existing.id, response))
            }
        }
    }

    private suspend fun deactivateResponse(userId: UserId, pollId: UUID, responseId: UUID) {
        val success = db.transaction { q ->
            val poll = q.run(pollQueries.getPoll(pollId)) ?: throw NotFoundException("Poll with ID {$pollId} not found")
            if (poll.createdBy != userId.id) throw UnauthorizedException("Cannot deactivate response of a poll you didn't create")
            q.run(responseQueries.deactivateResponse(pollId, responseId))
        }
        if (!success) throw BadRequestException("Response ID {$responseId} does not belong to pollId {$pollId}")
    }

    private suspend fun getResults(pollId: UUID): PollResults? {
        val (poll, resps) = db.transaction { q ->
            val poll = async { q.run(pollQueries.getPoll(pollId)) }
            val resps = async { q.run(responseQueries.getAllActiveResponses(pollId)) }
            poll.await() to resps.await()
        }
        if (poll == null) return null
        return resultsCalculator.calculateResults(poll.toApi(), resps.map { it.toApi() })
    }

    private suspend fun getPollHistory(userId: UserId): PollHistory {
        return db.transaction { q ->
            val created = async { q.run(pollQueries.getCreatedPolls(userId.id)) }
            val responded = async { q.run(pollQueries.getRespondedPolls(userId.id)) }
            PollHistory(
                    created = created.await().map { it.toApi() },
                    responded = responded.await().map { it.toApi() }
            )
        }
    }

    private fun PollRecord.toApi(): Poll {
        return Poll(
                id = id,
                title = title,
                questions = json.parse(Question.serializer().list, questions)
        )
    }

    private fun ResponseRecord.toApi(): PollResponse {
        return PollResponse(
                responses = json.parse(Response.serializer().list, responses)
        )
    }

    private fun Pair<ResponseRecord, VoteUserRecord>.toApi(): PollResponseDetails {
        val (resp, user) = this
        return PollResponseDetails(
                id = resp.id,
                email = user.email,
                active = resp.active,
                responses = json.parse(Response.serializer().list, resp.responses)
        )
    }
}
