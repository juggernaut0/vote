package services

import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.serializer
import vote.api.v1.UsersApi

class UsersApiClient : UsersApi {
    override suspend fun signIn(token: String) {
        fetch("POST", "/api/v1/users", token, String.serializer(), UnitSerializer)
    }
}
