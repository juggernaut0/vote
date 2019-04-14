package vote.util

import io.ktor.http.HttpStatusCode

open class WebApplicationException(val status: HttpStatusCode = HttpStatusCode.InternalServerError, message: String? = null)
    : RuntimeException(message ?: status.toString())
class BadRequestException(message: String? = null) : WebApplicationException(HttpStatusCode.BadRequest, message)
class UnauthorizedException(message: String? = null) : WebApplicationException(HttpStatusCode.Unauthorized, message)