package vote.db.query

import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import vote.api.UUID
import vote.api.v1.PollResponse
import vote.api.v1.Response
import vote.db.insertAsync
import vote.db.jooq.Tables.RESPONSE
import vote.db.jooq.Tables.VOTE_USER
import vote.db.jooq.tables.records.ResponseRecord
import vote.db.jooq.tables.records.VoteUserRecord
import javax.inject.Inject

class ResponseQueries @Inject constructor(private val json: Json) {
    fun createResponse(pollId: UUID, voterId: UUID, resp: PollResponse) = queryOf { dsl ->
        val id = UUID.randomUUID()
        dsl.newRecord(RESPONSE)
                .apply {
                    this.id = id
                    this.pollId = pollId
                    this.voterId = voterId
                    this.version = 1
                    this.responses = json.stringify(Response.serializer().list, resp.responses)
                }
                .insertAsync()
                .await()
        id
    }

    fun updateResponse(respId: UUID, resp: PollResponse) = queryOf<Unit> { dsl ->
        dsl.update(RESPONSE)
                .set(RESPONSE.VERSION, 1)
                .set(RESPONSE.RESPONSES, json.stringify(Response.serializer().list, resp.responses))
                .where(RESPONSE.ID.eq(respId))
                .executeAsync()
                .await()
    }

    fun getAllActiveResponses(pollId: UUID) = queryOf { dsl ->
        dsl.selectFrom(RESPONSE)
                .where(RESPONSE.POLL_ID.eq(pollId))
                .and(RESPONSE.ACTIVE.eq(true))
                .fetchAsync()
                .await()
    }

    fun getAllResponsesWithUsers(pollId: UUID) = queryOf<List<Pair<ResponseRecord, VoteUserRecord>>> { dsl ->
        dsl.select().from(RESPONSE.join(VOTE_USER).onKey())
                .where(RESPONSE.POLL_ID.eq(pollId))
                .fetchAsync()
                .await()
                .map { it.into(RESPONSE) to it.into(VOTE_USER) }
    }

    fun getResponse(pollId: UUID, voterId: UUID) = queryOf { dsl ->
        dsl.selectFrom(RESPONSE)
                .where(RESPONSE.POLL_ID.eq(pollId))
                .and(RESPONSE.VOTER_ID.eq(voterId))
                .fetchAsync()
                .await()
                .firstOrNull()
    }

    fun deactivateResponse(pollId: UUID, responseId: UUID) = queryOf { dsl ->
        val rowsChanged = dsl.update(RESPONSE)
                .set(RESPONSE.ACTIVE, false)
                .where(RESPONSE.ID.eq(responseId))
                .and(RESPONSE.POLL_ID.eq(pollId)) // validation check
                .executeAsync()
                .await()
        rowsChanged > 0
    }
}
