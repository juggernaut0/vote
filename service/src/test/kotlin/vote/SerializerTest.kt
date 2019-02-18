package vote

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import vote.util.nullable
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SerializerTest {
    @Test
    fun nullTest() {
        assertNull(Json.parse(String.serializer().nullable, "null"))
        assertNotNull(Json.parse(String.serializer().nullable, "\"hello\""))
    }
}