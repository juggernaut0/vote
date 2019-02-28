// cannot use this because JS will not compile with it
//@file:UseSerializers(UUIDSerializer::class)

package vote.api.v1

import kotlinx.serialization.Serializable
import vote.api.UUID
import vote.api.UUIDSerializer

@Serializable
data class Poll(
        @Serializable(with = UUIDSerializer::class) val id: UUID,
        val title: String,
        val questions: List<Question>
)

@Serializable
data class PollCreateRequest(
        val title: String,
        val questions: List<Question>
)

@Serializable
data class Question(
        val question: String,
        val type: String,
        val subtype: String,
        val options: List<String>
)

object QuestionType {
    const val FREEFORM = "FREEFORM"
    const val SELECT = "SELECT"
    const val RANKED = "RANKED"
}

object SelectSubtype {
    const val SELECT_ONE = "SELECT_ONE"
    const val SELECT_MANY = "SELECT_MANY"
}

object RankedSubtype {
    const val INSTANT_RUNOFF = "INSTANT_RUNOFF"
    const val BORDA_COUNT = "BORDA_COUNT"
}

@Serializable
data class PollResponse(
        val responses: List<Response>
)

@Serializable
data class Response internal constructor(
        val freeform: String?,
        val selections: List<Int>?
) {
    companion object {
        fun freeform(response: String) = Response(response, null)
        fun selections(selections: List<Int>) = Response(null, selections)
    }
}

@Serializable
data class PollResults(
        val results: List<Result>
)

@Serializable
data class Result(
        val responseCount: Int,
        val freeform: List<String>? = null,
        val votes: List<Int>? = null
)