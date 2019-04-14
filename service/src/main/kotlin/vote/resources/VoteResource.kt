package vote.resources

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.async
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import vote.api.UUID
import vote.api.v1.*
import vote.auth.AuthContext
import vote.db.Database
import vote.db.jooq.tables.records.PollRecord
import vote.db.jooq.tables.records.ResponseRecord
import vote.db.jooq.tables.records.VoteUserRecord
import vote.db.query.*
import vote.services.PollValidator
import vote.services.ResultsCalculator
import vote.util.BadRequestException
import vote.util.UnauthorizedException
import vote.util.nullable
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@KtorExperimentalAPI
class VoteResource @Inject constructor(
        private val db: Database,
        private val pollQueries: PollQueries,
        private val responseQueries: ResponseQueries,
        private val resultsCalculator: ResultsCalculator,
        private val pollValidator: PollValidator
) : Resource, VoteApi {
    override fun register(rt: Route) {
        with (rt) {
            authenticate {
                get("/polls") {
                    withAuthContext {
                        call.respondJson(PollHistory.serializer(), getPollHistory())
                    }
                }
                post("/polls") {
                    withAuthContext {
                        val body = call.receiveJson(PollCreateRequest.serializer())
                        call.respondJson(Poll.serializer(), createPoll(body))
                    }
                }
                get("polls/{id}/creator") {
                    withAuthContext {
                        val id = call.parameters.getUUID("id")
                        call.respondJson(Boolean.serializer(), isCreator(id))
                    }
                }
                get("/polls/{id}/response") {
                    withAuthContext {
                        val id = call.parameters.getUUID("id")
                        call.respondJson(PollResponse.serializer().nullable, getResponse(id))
                    }
                }
                get("/polls/{id}/responses") {
                    withAuthContext {
                        val id = call.parameters.getUUID("id")
                        call.respondJson(PollResponseDetails.serializer().list, getResponses(id))
                    }
                }
                put("/polls/{id}/response") {
                    withAuthContext {
                        val id = call.parameters.getUUID("id")
                        val body = call.receiveJson(PollResponse.serializer())
                        call.respondJson(UnitSerializer, submitResponse(id, body))
                    }
                }
                delete("/polls/{id}/responses/{respId}") {
                    withAuthContext {
                        val pollId = call.parameters.getUUID("id")
                        val respId = call.parameters.getUUID("respId")
                        deactivateResponse(pollId, respId)
                        call.respond(HttpStatusCode.NoContent)
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
        val validation = pollValidator.validate(pollCreateRequest)
        if (validation.isNotEmpty()) throw BadRequestException(validation.toString())
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

    override suspend fun isCreator(id: UUID): Boolean {
        val userId = coroutineContext[AuthContext]!!.userId.id
        return db.transaction { q ->
            q.run(pollQueries.getPoll(id))?.createdBy == userId
        }
    }

    override suspend fun getResponse(pollId: UUID): PollResponse? {
        val userId = coroutineContext[AuthContext]!!.userId.id
        val resp = db.transaction { q ->
            q.run(responseQueries.getResponse(pollId, userId))
        }
        return resp?.toApi()
    }

    override suspend fun getResponses(pollId: UUID): List<PollResponseDetails> {
        val userId = coroutineContext[AuthContext]!!.userId.id
        return db.transaction { q ->
            val poll = q.run(pollQueries.getPoll(pollId)) ?: throw NotFoundException("Poll with ID {$pollId} not found")
            if (poll.createdBy != userId) throw UnauthorizedException("Cannot view responses of a poll you didn't create")
            q.run(responseQueries.getAllResponsesWithUsers(pollId)).map { it.toApi() }
        }
    }

    override suspend fun submitResponse(pollId: UUID, response: PollResponse) {
        val userId = coroutineContext[AuthContext]!!.userId.id
        db.transaction { q ->
            val pAsync = async { q.run(pollQueries.getPoll(pollId))?.toApi() }
            val rAsync = async { q.run(responseQueries.getResponse(pollId, userId)) }
            val p = pAsync.await() ?: throw NotFoundException("Poll with ID {$pollId} not found")

            val validation = pollValidator.validateResponse(p, response)
            if (validation.isNotEmpty()) throw BadRequestException(validation.toString())

            val existing = rAsync.await()
            if (existing == null) {
                q.run(responseQueries.createResponse(pollId, userId, response))
            } else {
                q.run(responseQueries.updateResponse(existing.id, response))
            }
        }
    }

    override suspend fun deactivateResponse(pollId: UUID, responseId: UUID) {
        val success = db.transaction { q ->
            q.run(responseQueries.deactivateResponse(pollId, responseId))
        }
        if (!success) throw BadRequestException("Response ID {$responseId} does not belong to pollId {$pollId}")
    }

    override suspend fun getResults(pollId: UUID): PollResults? {
        val (poll, resps) = db.transaction { q ->
            val poll = async { q.run(pollQueries.getPoll(pollId)) }
            val resps = async { q.run(responseQueries.getAllActiveResponses(pollId)) }
            poll.await() to resps.await()
        }
        if (poll == null) return null
        return resultsCalculator.calculateResults(poll.toApi(), resps.map { it.toApi() })
    }

    override suspend fun getPollHistory(): PollHistory {
        val userId = coroutineContext[AuthContext]!!.userId.id
        return db.transaction { q ->
            val created = async { q.run(pollQueries.getCreatedPolls(userId)) }
            val responded = async { q.run(pollQueries.getRespondedPolls(userId)) }
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
                questions = Json.nonstrict.parse(Question.serializer().list, questions)
        )
    }

    private fun ResponseRecord.toApi(): PollResponse {
        return PollResponse(
                responses = Json.nonstrict.parse(Response.serializer().list, responses)
        )
    }

    private fun Pair<ResponseRecord, VoteUserRecord>.toApi(): PollResponseDetails {
        val (resp, user) = this
        return PollResponseDetails(
                id = resp.id,
                email = user.email,
                active = resp.active,
                responses = Json.nonstrict.parse(Response.serializer().list, resp.responses)
        )
    }
}
