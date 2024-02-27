package vote.db.query

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jooq.JSONB
import vote.api.v1.Question
import vote.db.jooq.Tables.POLL
import vote.db.jooq.Tables.RESPONSE
import vote.db.jooq.tables.records.PollRecord
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Inject

class PollQueries @Inject constructor(private val json: Json) {
    fun createPoll(title: String, questions: List<Question>, createdBy: UUID) = queryOf { dsl ->
        val id = UUID.randomUUID()
        dsl.newRecord(POLL)
                .apply {
                    this.id = id
                    this.title = title
                    this.version = 1
                    this.questions = JSONB.valueOf(json.encodeToString(ListSerializer(Question.serializer()), questions))
                    this.createdBy = createdBy
                    this.createdDt = OffsetDateTime.now()
                }
                .insert()
        id
    }

    fun getPoll(id: UUID) = queryOf { dsl ->
        dsl.selectFrom(POLL)
                .where(POLL.ID.eq(id))
                .fetch()
                .firstOrNull()
    }

    fun getCreatedPolls(createdBy: UUID): Query<List<PollRecord>> = queryOf { dsl ->
        dsl.selectFrom(POLL)
                .where(POLL.CREATED_BY.eq(createdBy))
                .orderBy(POLL.CREATED_DT.desc())
                .limit(50)
                .fetch()
    }

    fun getRespondedPolls(voterId: UUID): Query<List<PollRecord>> = queryOf { dsl ->
        dsl.select(POLL.asterisk()).from(POLL.join(RESPONSE).onKey())
                .where(RESPONSE.VOTER_ID.eq(voterId))
                .orderBy(POLL.CREATED_DT.desc())
                .limit(50)
                .fetch()
                .map { it.into(POLL) }
    }
}
