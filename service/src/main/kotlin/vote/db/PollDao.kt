package vote.db

import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.jooq.DSLContext
import vote.api.v1.Question
import vote.api.UUID
import vote.db.jooq.Tables.*
import vote.db.jooq.tables.records.PollRecord
import java.time.OffsetDateTime

class PollDao(private val dsl: DSLContext) {
    suspend fun createPoll(title: String, questions: List<Question>, createdBy: UUID): UUID {
        val id = UUID.randomUUID()
        dsl.newRecord(POLL)
                .apply {
                    this.id = id
                    this.title = title
                    this.version = 1
                    this.questions = Json.stringify(Question.serializer().list, questions)
                    this.createdBy = createdBy
                    this.createdDt = OffsetDateTime.now()
                }
                .insertAsync()
                .await()
        return id
    }

    suspend fun getPoll(id: UUID): PollRecord? {
        return dsl.selectFrom(POLL)
                .where(POLL.ID.eq(id))
                .fetchAsync()
                .await()
                .firstOrNull()
    }

    companion object : DaoProvider<PollDao> {
        override fun get(dsl: DSLContext): PollDao {
            return PollDao(dsl)
        }
    }
}
