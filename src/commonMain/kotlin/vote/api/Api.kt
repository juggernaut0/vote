// cannot use this because JS will not compile with it
//@file:UseSerializers(UUIDSerializer::class)

package vote.api

import kotlinx.serialization.Serializable

@Serializable
data class Poll(
        @Serializable(with = UUIDSerializer::class) val id: UUID,
        val name: String
)
