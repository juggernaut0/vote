package vote.services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import vote.domain.VerifiedToken
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class GoogleTokenVerifier @Inject constructor(private val tokenVerifier: GoogleIdTokenVerifier) {
    suspend fun verify(token: String): VerifiedToken? {
        val idToken = verifyToken(token) ?: run {
            log.warn("Failed to verify token")
            return null
        }
        val payload = idToken.payload
        return VerifiedToken(payload.subject, payload.email)
    }

    private suspend fun verifyToken(token: String): GoogleIdToken? {
        return withContext(Dispatchers.IO) {
            suspendCoroutine<GoogleIdToken?> { cont ->
                cont.resumeWith(runCatching { tokenVerifier.verify(token) })
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GoogleTokenVerifier::class.java)
    }
}
