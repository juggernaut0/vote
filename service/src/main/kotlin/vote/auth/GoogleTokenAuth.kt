package vote.auth

import com.google.inject.Injector
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.response.respond
import org.slf4j.LoggerFactory
import vote.api.UUID
import vote.db.Database
import vote.db.query.VoteUserQueries
import vote.services.GoogleTokenVerifier
import javax.inject.Inject

class GoogleTokenAuthProvider @Inject constructor(
        private val verifier: GoogleTokenVerifier,
        private val voteUserQueries: VoteUserQueries,
        private val db: Database
): AuthenticationProvider(object : Configuration(null) {}) {
    suspend fun verify(token: String): UserId? {
        val verified = verifier.verify(token) ?: return null
        val userId = db.transaction { q ->
            q.run(voteUserQueries.getByGoogleId(verified.googleId))?.id
        } ?: return null
        return UserId(userId)
    }
}

fun Authentication.Configuration.googleToken(injector: Injector) {
    val provider = injector.getInstance(GoogleTokenAuthProvider::class.java)
    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val header = call.request.parseAuthorizationHeader()?.takeIf { it.authScheme == "Bearer" } as? HttpAuthHeader.Single
        val principal = header?.let { provider.verify(it.blob) }

        val cause = when {
            header == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge("GoogleTokenAuth", cause) {
                log.warn("Missing or invalid authorization header")
                call.respond(HttpStatusCode.Unauthorized, "Missing or invalid authorization header")
                it.complete()
            }
        } else {
            context.principal(principal!!)
        }
    }

    register(provider)
}

class UserId(val id: UUID): Principal

private val log = LoggerFactory.getLogger("vote.auth.GoogleTokenAuth")
