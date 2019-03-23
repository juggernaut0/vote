package vote.db.query

import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import vote.api.UUID
import vote.api.v1.PollResponse
import vote.api.v1.Response
import vote.db.insertAsync
import vote.db.jooq.Tables.RESPONSE

class ResponseQueries {
    fun createResponse(pollId: UUID, voterId: UUID, resp: PollResponse) = queryOf { dsl ->
        val id = UUID.randomUUID()
        dsl.newRecord(RESPONSE)
                .apply {
                    this.id = id
                    this.pollId = pollId
                    this.voterId = voterId
                    this.version = 1
                    this.responses = Json.stringify(Response.serializer().list, resp.responses)
                }
                .insertAsync()
                .await()
        id
    }

    fun updateResponse(respId: UUID, resp: PollResponse) = queryOf<Unit> { dsl ->
        dsl.update(RESPONSE)
                .set(RESPONSE.VERSION, 1)
                .set(RESPONSE.RESPONSES, Json.stringify(Response.serializer().list, resp.responses))
                .where(RESPONSE.ID.eq(respId))
                .executeAsync()
                .await()
    }

    fun getAllResponses(pollId: UUID) = queryOf { dsl ->
        dsl.selectFrom(RESPONSE)
                .where(RESPONSE.POLL_ID.eq(pollId))
                .fetchAsync()
                .await()
    }

    fun getResponse(pollId: UUID, voterId: UUID) = queryOf { dsl ->
        dsl.selectFrom(RESPONSE)
                .where(RESPONSE.POLL_ID.eq(pollId))
                .and(RESPONSE.VOTER_ID.eq(voterId))
                .fetchAsync()
                .await()
                .firstOrNull()
    }
}
