// cannot use this because JS will not compile with it
//@file:UseSerializers(UUIDSerializer::class)

package vote.api.v1

import kotlinx.serialization.Optional
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

object FreeformSubtype {
    const val SINGLE = "SINGLE"
    const val MUTLI = "MULTI"
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
        @Optional val freeform: String? = null,
        @Optional val multiFreeform: List<String>? = null,
        @Optional val selections: List<Int>? = null
) {
    companion object {
        fun freeform(response: String) = Response(freeform = response)
        fun multiFreeform(responses: List<String>) = Response(multiFreeform = responses)
        fun selections(selections: List<Int>) = Response(selections = selections)
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

@Serializable
data class PollHistory(
        val created: List<Poll>,
        val responded: List<Poll>
)
