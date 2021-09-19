package vote.resources

import auth.ValidatedToken
import auth.api.v1.LookupParams
import auth.api.v1.lookup
import io.ktor.auth.authenticate
import io.ktor.client.*
import io.ktor.features.NotFoundException
import io.ktor.routing.*
import multiplatform.ktor.handleApi
import kotlinx.coroutines.async
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import multiplatform.ktor.BadRequestException
import multiplatform.ktor.KtorApiClient
import vote.api.v1.*
import vote.db.Database
import vote.db.jooq.tables.records.PollRecord
import vote.db.jooq.tables.records.ResponseRecord
import vote.db.query.*
import vote.services.PollValidator
import vote.services.ResultsCalculator
import vote.util.UnauthorizedException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class VoteResource @Inject constructor(
    @Named("authClient") private val authClient: HttpClient,
    private val db: Database,
    private val pollQueries: PollQueries,
    private val responseQueries: ResponseQueries,
    private val resultsCalculator: ResultsCalculator,
    private val pollValidator: PollValidator,
    private val json: Json,
) : Resource {
    override fun register(rt: Route) {
        with (rt) {
            authenticate {
                handleApi(getPollHistory) { getPollHistory(auth as ValidatedToken) }
                handleApi(createPoll) { body -> createPoll(auth as ValidatedToken, body) }
                handleApi(isCreator) { isCreator(auth as ValidatedToken, params.id) }
                handleApi(getResponse) { getResponse(auth as ValidatedToken, params.id) }
                handleApi(getResponses) { getResponses(auth as ValidatedToken, params.id) }
                handleApi(submitResponse) { body -> submitResponse(auth as ValidatedToken, params.id, body) }
                handleApi(deactivateResponse) { deactivateResponse(auth as ValidatedToken, params.id, params.respId) }
            }
            handleApi(getPoll) { getPoll(params.id) }
            handleApi(getResults) { getResults(params.id) }
        }
    }

    private suspend fun createPoll(userId: ValidatedToken, pollCreateRequest: PollCreateRequest): Poll {
        val validation = pollValidator.validate(pollCreateRequest)
        if (validation.isNotEmpty()) throw BadRequestException(validation.toString())
        val id = db.transaction { q ->
            q.run(pollQueries.createPoll(pollCreateRequest.title, pollCreateRequest.questions, userId.userId))
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

    private suspend fun isCreator(userId: ValidatedToken, id: UUID): Boolean {
        return db.transaction { q ->
            q.run(pollQueries.getPoll(id))?.createdBy == userId.userId
        }
    }

    private suspend fun getResponse(userId: ValidatedToken, pollId: UUID): PollResponse? {
        val resp = db.transaction { q ->
            q.run(responseQueries.getResponse(pollId, userId.userId))
        }
        return resp?.toApi()
    }

    private suspend fun getResponses(userId: ValidatedToken, pollId: UUID): List<PollResponseDetails> {
        return db.transaction { q ->
            val poll = q.run(pollQueries.getPoll(pollId)) ?: throw NotFoundException("Poll with ID {$pollId} not found")
            if (poll.createdBy != userId.userId) throw UnauthorizedException("Cannot view responses of a poll you didn't create")
            q.run(responseQueries.getAllResponsesWithUsers(pollId)).map { it.toDetails() }
        }
    }

    private suspend fun submitResponse(userId: ValidatedToken, pollId: UUID, response: PollResponse) {
        db.transaction { q ->
            val pAsync = async { q.run(pollQueries.getPoll(pollId))?.toApi() }
            val rAsync = async { q.run(responseQueries.getResponse(pollId, userId.userId)) }
            val p = pAsync.await() ?: throw NotFoundException("Poll with ID {$pollId} not found")

            val validation = pollValidator.validateResponse(p, response)
            if (validation.isNotEmpty()) throw BadRequestException(validation.toString())

            val existing = rAsync.await()
            if (existing == null) {
                q.run(responseQueries.createResponse(pollId, userId.userId, response))
            } else {
                q.run(responseQueries.updateResponse(existing.id, response))
            }
        }
    }

    private suspend fun deactivateResponse(userId: ValidatedToken, pollId: UUID, responseId: UUID) {
        val success = db.transaction { q ->
            val poll = q.run(pollQueries.getPoll(pollId)) ?: throw NotFoundException("Poll with ID {$pollId} not found")
            if (poll.createdBy != userId.userId) throw UnauthorizedException("Cannot deactivate response of a poll you didn't create")
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

    private suspend fun getPollHistory(userId: ValidatedToken): PollHistory {
        return db.transaction { q ->
            val created = async { q.run(pollQueries.getCreatedPolls(userId.userId)) }
            val responded = async { q.run(pollQueries.getRespondedPolls(userId.userId)) }
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
                questions = json.decodeFromString(ListSerializer(Question.serializer()), questions.data())
        )
    }

    private fun ResponseRecord.toApi(): PollResponse {
        return PollResponse(
                responses = json.decodeFromString(ListSerializer(Response.serializer()), responses.data())
        )
    }

    private suspend fun ResponseRecord.toDetails(): PollResponseDetails {
        val name = KtorApiClient(authClient).callApi(lookup, LookupParams(id = voterId))!!.let { it.displayName ?: it.id.toString() }
        return PollResponseDetails(
                id = id,
                email = name,
                active = active,
                responses = json.decodeFromString(ListSerializer(Response.serializer()), responses.data())
        )
    }
}
