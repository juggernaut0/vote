package services

import vote.api.v1.signIn

class UsersApiClient {
    suspend fun signIn(token: String) {
        return signIn.call(token)
    }
}
