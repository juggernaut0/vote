package vote.resources

import io.ktor.routing.Route
import vote.api.v1.signIn
import vote.db.Database
import vote.db.query.VoteUserQueries
import vote.services.GoogleTokenVerifier
import vote.util.BadRequestException
import javax.inject.Inject

class UsersResource @Inject constructor(
        private val db: Database,
        private val voteUserQueries: VoteUserQueries,
        private val tokenVerifier: GoogleTokenVerifier
) : Resource {
    override fun register(rt: Route) {
        rt.handleApi(signIn) { body, _ -> signIn(body) }
    }

    private suspend fun signIn(token: String) {
        val info = tokenVerifier.verify(token) ?: throw BadRequestException("Could not verify token")
        db.transaction { q ->
            val user = q.run(voteUserQueries.getByGoogleId(info.googleId))
            if (user == null) {
                q.run(voteUserQueries.createUser(info.googleId, info.email))
            }
        }
    }
}
