import kotlinx.serialization.json.Json
import vote.api.Poll
import vote.api.UUID

fun main() {
    val x = UUID("5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6")
    println(Poll(x, "test"))
    println(Json.stringify(Poll.serializer(), Poll(UUID("5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"), "test")))
}
