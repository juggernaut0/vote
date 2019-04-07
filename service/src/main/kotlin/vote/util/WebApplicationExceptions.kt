package vote.util

import io.ktor.http.HttpStatusCode

open class WebApplicationException(val status: HttpStatusCode = HttpStatusCode.InternalServerError, message: String? = null)
    : RuntimeException(message?.let { "$status - $it" } ?: status.toString())
class UnauthorizedException(message: String? = null) : WebApplicationException(HttpStatusCode.Unauthorized, message)