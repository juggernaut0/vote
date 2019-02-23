package vote.api.v1

interface UsersApi {
    suspend fun signIn(token: String)
}
