package vote.api

actual data class UUID(private val str: String) {
    override fun toString(): String = str
}

internal actual fun fromString(str: String) = UUID(str)
