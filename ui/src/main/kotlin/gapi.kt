import kotlin.js.Promise

external val gapi: GApi

external class GApi {
    fun load(auth2: String, callback: () -> Unit)
    val auth2: Auth2
}

external class Auth2 {
    fun init(params: Any): Promise<GoogleAuth>

    class GoogleAuth {
        val isSignedIn: GetOrListen<Boolean>
        val currentUser: GetOrListen<GoogleUser>
        fun signIn(): Promise<GoogleUser>
        fun signOut(): Promise<Nothing?>
    }

    class GetOrListen<T> {
        fun get(): T
    }

    class GoogleUser {
        fun getId(): String
        fun getBasicProfile(): BasicProfile
        fun getAuthResponse(includeAuthorizationData: Boolean = definedExternally): AuthResponse
    }

    class BasicProfile {
        fun getEmail(): String
    }

    class AuthResponse {
        val id_token: String
    }
}
