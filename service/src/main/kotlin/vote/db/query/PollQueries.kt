package vote.db.query

import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import vote.api.UUID
import vote.api.v1.Question
import vote.db.insertAsync
import vote.db.jooq.Tables.POLL
import java.time.OffsetDateTime

class PollQueries {
    fun createPoll(title: String, questions: List<Question>, createdBy: UUID) = queryOf { dsl ->
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
        id
    }

    fun getPoll(id: UUID) = queryOf { dsl ->
        dsl.selectFrom(POLL)
                .where(POLL.ID.eq(id))
                .fetchAsync()
                .await()
                .firstOrNull()
    }
}
