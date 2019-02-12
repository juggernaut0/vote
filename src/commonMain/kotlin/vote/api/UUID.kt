package vote.api

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

expect class UUID

@Serializer(forClass = UUID::class)
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("UUID")

    override fun serialize(encoder: Encoder, obj: UUID) {
        encoder.encodeString(obj.toString())
    }

    override fun deserialize(decoder: Decoder): UUID = fromString(decoder.decodeString())
}

internal expect fun fromString(str: String): UUID
