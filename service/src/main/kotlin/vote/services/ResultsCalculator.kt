package vote.services

import org.slf4j.LoggerFactory
import vote.api.v1.*
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

class ResultsCalculator @Inject constructor() {
    fun calculateResults(poll: Poll, responses: List<PollResponse>): PollResults {
        val results = mutableListOf<Result>()
        for ((i, q) in poll.questions.withIndex()) {
            val resps = responses.map { it.responses[i] }
            val r = when (q.type) {
                QuestionType.FREEFORM -> summarizeFreeform(q, resps)
                QuestionType.SELECT -> summarizeSelect(q, resps)
                QuestionType.RANKED -> summarizeRanked(q, resps, poll.id)
                QuestionType.RANGE -> summarizeRange(q, resps)
                else -> {
                    log.warn("Unknown question type ${q.type}")
                    Result(0)
                }
            }
            results.add(r)
        }
        return PollResults(results)
    }

    private fun summarizeFreeform(question: Question, responses: List<Response>): Result {
        return when (question.subtype) {
            FreeformSubtype.MULTI -> {
                val rs = responses.mapNotNull { r -> r.multiFreeform?.takeUnless { it.isEmpty() } }
                Result(rs.size, freeform = rs.flatten())
            }
            else -> {
                val rs = responses.mapNotNull { r -> r.freeform.takeUnless { it.isNullOrBlank() } }
                Result(rs.size, freeform = rs)
            }
        }

    }

    private fun summarizeSelect(question: Question, responses: List<Response>): Result {
        val allSelections = responses.mapNotNull { r -> r.selections.takeUnless { it.isNullOrEmpty() } }
        val votes = IntArray(question.options.size) { 0 }
        for (s in allSelections.flatten()) {
            votes[s]++
        }
        return Result(allSelections.size, votes = votes.asList())
    }

    private fun summarizeRanked(question: Question, responses: List<Response>, pollId: UUID): Result {
        val size = question.options.size
        if (responses.isEmpty()) return Result(0, votes = List(size) { 0 })
        return when (question.subtype) {
            RankedSubtype.INSTANT_RUNOFF -> instantRunoff(size, responses, pollId.hilo())
            RankedSubtype.BORDA_COUNT -> bordaCount(size, responses)
            else -> {
                log.warn("Unknown ranked subtype ${question.subtype}")
                Result(0, votes = emptyList())
            }
        }
    }

    private fun instantRunoff(size: Int, responses: List<Response>, seed: Long): Result {
        val selections = responses.mapNotNull { r -> r.selections?.takeIf { it.size == size } }
        val votes = IntArray(size) { 0 }
        val remaining = (0 until size).toMutableSet()
        val random = Random(seed)
        while (remaining.size > 2) {
            for (s in selections) {
                val vote = s.find { it in remaining }!!
                votes[vote]++
            }
            val lowest = votes
                    .withIndex()
                    .filter { (i, _) -> i in remaining }
                    .minSetBy { (_, v) -> v }
                    .map { it.index }
                    .getRandom(random)
            remaining.remove(lowest)
            votes.indices.forEach { votes[it] = 0 }
        }

        for (s in selections) {
            val vote = s.find { it in remaining }!!
            votes[vote]++
        }

        return Result(selections.size, votes = votes.toList())
    }

    private fun bordaCount(size: Int, responses: List<Response>): Result {
        val selections = responses.mapNotNull { r -> r.selections?.takeIf { it.size == size } }
        val votes = IntArray(size) { 0 }
        for (s in selections) {
            for ((i, v) in s.zip(size downTo 1)) {
                votes[i] += v
            }
        }
        return Result(selections.size, votes = votes.asList())
    }

    private fun summarizeRange(question: Question, responses: List<Response>): Result {
        val size = question.options.size
        val validResponses = responses.mapNotNull { r -> r.selections?.takeIf { it.size == size } }
        val results = List(size) { i -> validResponses.map { it[i] }.average().roundToInt() }
        return Result(validResponses.size, votes = results)
    }

    private inline fun <T, R : Comparable<R>> Iterable<T>.minSetBy(selector: (T) -> R): Set<T> {
        var min: R? = null
        lateinit var result: MutableSet<T>
        for (e in this) {
            val k = selector(e)
            if (min == null || k < min) {
                min = k
                result = mutableSetOf(e)
            } else if (k == min) {
                result.add(e)
            }
        }
        return result
    }

    private fun <T> List<T>.getRandom(random: Random): T? = if (isEmpty()) null else get(random.nextInt(size))

    private fun UUID.hilo() = mostSignificantBits xor leastSignificantBits

    companion object {
        private val log = LoggerFactory.getLogger(ResultsCalculator::class.java)
    }
}