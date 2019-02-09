import kotlinx.serialization.json.Json
import vote.api.Poll

fun main() {
    println(Poll("test"))
    println(Json.stringify(Poll.serializer(), Poll("test")))
}
