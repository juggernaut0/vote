package vote.resources

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.http.Parameters
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.withContext
import vote.api.UUID
import vote.auth.AuthContext
import vote.util.BadRequestException
import java.lang.IllegalArgumentException

fun Parameters.getUUID(name: String): UUID {
    return try {
        UUID.fromString(get(name))
    } catch (e: IllegalArgumentException) {
        throw BadRequestException(e.message ?: "", e)
    } ?: throw BadRequestException()
}

suspend fun <R> PipelineContext<Unit, ApplicationCall>.withAuthContext(fn: suspend () -> R): R {
    return withContext(AuthContext(call.authentication.principal()!!)) {
        fn()
    }
}
