package vote.util

import io.ktor.http.HttpStatusCode
import multiplatform.ktor.WebApplicationException

class UnauthorizedException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.Unauthorized, message, cause)