package services

import gapi
import kotlinx.coroutines.await
import vote.api.v1.UsersApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleSignIn(private val usersApi: UsersApi) {
    private var auth: Auth2.GoogleAuth? = null

    fun isSignedIn() = auth?.isSignedIn?.get() ?: false

    fun getUser() = if (isSignedIn()) auth!!.currentUser.get() else throw IllegalStateException("Must sign in before accessing user")

    suspend fun load() {
        suspendCoroutine<Unit> { cont ->
            try {
                gapi.load("auth2") { cont.resume(Unit) }
            } catch (e: Throwable) {
                cont.resumeWithException(e)
            }
        }
    }

    suspend fun init(clientId: String, scope: String) {
        auth = gapi.auth2.init(object {
            val client_id = clientId
            val scope = scope
        }).await()
    }

    suspend fun signIn(): Auth2.GoogleUser {
        val auth = auth ?: throw IllegalStateException("Must call init before calling signIn")
        val user = auth.signIn().await()
        usersApi.signIn(user.getAuthResponse(true).id_token)
        return user
    }

    suspend fun signOut() {
        auth?.signOut()?.await()
    }
}
