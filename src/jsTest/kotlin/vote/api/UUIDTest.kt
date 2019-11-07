package vote.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals

class UUIDTest {
    val json = Json(JsonConfiguration.Stable)

    @Test
    fun serialize() {
        val repr = "5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"
        val uuid = UUID(repr)

        assertEquals("\"$repr\"", json.stringify(UUIDSerializer, uuid))
    }

    @Test
    fun deserialize() {
        val repr = "5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"

        val uuid = json.parse(UUIDSerializer, "\"$repr\"")

        assertEquals(UUID(repr), uuid)
    }
}
