package vote.util

import kotlinx.serialization.*
import kotlinx.serialization.internal.makeNullable

val <T : Any> KSerializer<T>.nullable: KSerializer<T?> get() = makeNullable(this)
