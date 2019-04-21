package vote.util

import io.ktor.http.HttpStatusCode

open class WebApplicationException(
        val status: HttpStatusCode = HttpStatusCode.InternalServerError,
        message: String? = null,
        cause: Throwable? = null
) : RuntimeException(message ?: status.toString(), cause)

class BadRequestException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.BadRequest, message, cause)

class UnauthorizedException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.Unauthorized, message, cause)