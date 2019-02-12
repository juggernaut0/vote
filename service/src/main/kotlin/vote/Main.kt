package vote

import kotlinx.serialization.json.Json
import vote.api.Poll
import vote.api.UUIDSerializer
import java.util.*

fun main() {
    println("Hello World")
    println(Json.stringify(UUIDSerializer, UUID.randomUUID()))
    println(Poll(UUID.randomUUID(), "test"))
    println(Json.stringify(Poll.serializer(), Poll(UUID.randomUUID(), "test")))
}