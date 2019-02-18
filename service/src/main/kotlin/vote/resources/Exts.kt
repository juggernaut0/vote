package vote.resources

import io.ktor.application.ApplicationCall
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import vote.api.UUID
import java.lang.IllegalArgumentException

suspend fun <T : Any> ApplicationCall.receiveJson(des: DeserializationStrategy<T>): T {
    return Json.parse(des, receiveText())
}

suspend fun <T> ApplicationCall.respondJson(ser: SerializationStrategy<T>, response: T) {
    respondText(Json.stringify(ser, response), ContentType.Application.Json)
}

@KtorExperimentalAPI
fun Parameters.getUUID(name: String): UUID {
    return try {
        UUID.fromString(get(name))
    } catch (e: IllegalArgumentException) {
        throw BadRequestException(e.message ?: "", e)
    } ?: throw BadRequestException("")
}
