package vote.resources

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import vote.api.ApiRoute
import vote.api.Method
import vote.api.UUID
import vote.auth.AuthContext
import vote.util.BadRequestException
import java.lang.IllegalArgumentException

suspend fun <T : Any> ApplicationCall.receiveJson(des: DeserializationStrategy<T>): T {
    return Json.parse(des, receiveText())
}

suspend fun <T> ApplicationCall.respondJson(ser: SerializationStrategy<T>, response: T) {
    respondText(Json.stringify(ser, response), ContentType.Application.Json)
}

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

private fun Method.toHttpMethod() = when (this) {
    Method.GET -> HttpMethod.Get
    Method.POST -> HttpMethod.Post
    Method.PUT -> HttpMethod.Put
    Method.DELETE -> HttpMethod.Delete
}

fun <T : Any, R> Route.handleApi(apiRoute: ApiRoute<T, R>, withAuth: Boolean = false, handler: suspend (T, Parameters) -> R) {
    route(apiRoute.path.toString(), apiRoute.method.toHttpMethod()) {
        handle {
            val body = apiRoute.requestSer!!.let { call.receiveJson(it) }
            val resp = if (withAuth) {
                withAuthContext { handler(body, call.parameters) }
            } else {
                handler(body, call.parameters)
            }
            call.respondJson(apiRoute.responseSer, resp)
        }
    }
}

fun <R> Route.handleApi(apiRoute: ApiRoute<Nothing, R>, withAuth: Boolean = false, handler: suspend (Parameters) -> R) {
    route(apiRoute.path.toString(), apiRoute.method.toHttpMethod()) {
        handle {
            val resp = if (withAuth) {
                withAuthContext { handler(call.parameters) }
            } else {
                handler(call.parameters)
            }
            call.respondJson(apiRoute.responseSer, resp)
        }
    }
}
