package services

import juggernaut0.multiplatform.call
import vote.api.v1.signIn

class UsersApiClient {
    suspend fun signIn(token: String) {
        return signIn.call(token)
    }
}
