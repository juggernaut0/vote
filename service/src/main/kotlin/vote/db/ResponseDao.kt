package vote.db

import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.jooq.DSLContext
import vote.api.v1.PollResponse
import vote.api.v1.Response
import vote.api.UUID
import vote.db.jooq.Tables.RESPONSE
import vote.db.jooq.tables.records.ResponseRecord

class ResponseDao(private val dsl: DSLContext) {
    suspend fun createResponse(pollId: UUID, voterId: UUID, resp: PollResponse): UUID {
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
        return id
    }

    suspend fun updateResponse(respId: UUID, resp: PollResponse) {
        dsl.update(RESPONSE)
                .set(RESPONSE.VERSION, 1)
                .set(RESPONSE.RESPONSES, Json.stringify(Response.serializer().list, resp.responses))
                .where(RESPONSE.ID.eq(respId))
                .executeAsync()
                .await()
    }

    suspend fun getAllResponses(pollId: UUID): List<ResponseRecord> {
        return dsl.selectFrom(RESPONSE)
                .where(RESPONSE.POLL_ID.eq(pollId))
                .fetchAsync()
                .await()
    }

    suspend fun getResponse(pollId: UUID, voterId: UUID): ResponseRecord? {
        return dsl.selectFrom(RESPONSE)
                .where(RESPONSE.POLL_ID.eq(pollId))
                .and(RESPONSE.VOTER_ID.eq(voterId))
                .fetchAsync()
                .await()
                .firstOrNull()
    }

    companion object : DaoProvider<ResponseDao> {
        override fun get(dsl: DSLContext): ResponseDao {
            return ResponseDao(dsl)
        }
    }
}
