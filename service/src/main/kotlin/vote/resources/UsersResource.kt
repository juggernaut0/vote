package vote.resources

import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.serializer
import vote.api.v1.UsersApi
import vote.db.DaoProvider
import vote.db.Transactional
import vote.db.VoteUserDao
import vote.services.GoogleTokenVerifier
import javax.inject.Inject

@KtorExperimentalAPI
class UsersResource @Inject constructor(
        private val tx: Transactional,
        private val userDao: DaoProvider<VoteUserDao>,
        private val tokenVerifier: GoogleTokenVerifier
) : Resource, UsersApi {
    override fun register(rt: Route) {
        rt.post("users") {
            val body = call.receiveJson(String.serializer())
            call.respondJson(UnitSerializer, signIn(body))
        }
    }

    override suspend fun signIn(token: String) {
        val info = tokenVerifier.verify(token) ?: throw BadRequestException("Could not verify token")
        tx.withDao(userDao) { dao ->
            val user = dao.getByGoogleId(info.googleId)
            if (user == null) {
                dao.createUser(info.googleId, info.email)
            }
        }
    }
}
