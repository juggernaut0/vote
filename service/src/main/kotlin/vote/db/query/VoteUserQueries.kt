package vote.db.query

import kotlinx.coroutines.future.await
import vote.api.UUID
import vote.db.insertAsync
import vote.db.jooq.Tables.VOTE_USER

class VoteUserQueries {
    fun createUser(googleId: String, email: String) = queryOf { dsl ->
        val id = UUID.randomUUID()
        dsl.newRecord(VOTE_USER)
                .apply {
                    this.id = id
                    this.googleId = googleId
                    this.email = email
                }
                .insertAsync()
                .await()
    }

    fun getByGoogleId(googleId: String) = queryOf { dsl ->
        dsl.selectFrom(VOTE_USER)
                .where(VOTE_USER.GOOGLE_ID.eq(googleId))
                .fetchAsync()
                .await()
                .firstOrNull()
    }
}
