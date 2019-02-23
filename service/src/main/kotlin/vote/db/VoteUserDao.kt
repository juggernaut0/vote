package vote.db

import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import vote.db.jooq.Tables.VOTE_USER
import vote.db.jooq.tables.records.VoteUserRecord
import java.util.*

class VoteUserDao(private val dsl: DSLContext) {
    suspend fun createUser(googleId: String, email: String) {
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

    suspend fun getByGoogleId(googleId: String): VoteUserRecord? {
        return dsl.selectFrom(VOTE_USER)
                .where(VOTE_USER.GOOGLE_ID.eq(googleId))
                .fetchAsync()
                .await()
                .firstOrNull()
    }

    companion object : DaoProvider<VoteUserDao> {
        override fun get(dsl: DSLContext): VoteUserDao {
            return VoteUserDao(dsl)
        }
    }
}