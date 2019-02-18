package vote.services

import org.slf4j.LoggerFactory
import vote.api.v1.*

class ResultsCalculator {
    fun calculateResults(poll: Poll, responses: List<PollResponse>): PollResults {
        val results = mutableListOf<Result>()
        for ((i, q) in poll.questions.withIndex()) {
            val resps = responses.map { it.responses[i] }
            val r = when (q.type) {
                QuestionType.FREEFORM -> summarizeFreeform(resps)
                QuestionType.SELECT -> summarizeSelect(q, resps)
                QuestionType.RANKED -> summarizeRanked(q, resps)
                else -> {
                    log.warn("Unknown question type ${q.type}")
                    Result()
                }
            }
            results.add(r)
        }
        return PollResults(results)
    }

    private fun summarizeFreeform(responses: List<Response>): Result {
        return Result(freeform = responses.mapNotNull { it.freeform })
    }

    private fun summarizeSelect(question: Question, responses: List<Response>): Result {
        val votes = IntArray(question.options.size) { 0 }
        for (r in responses) {
            for (s in r.selections ?: emptyList()) {
                votes[s]++
            }
        }
        return Result(votes = votes.asList())
    }

    private fun summarizeRanked(question: Question, responses: List<Response>): Result {
        val size = question.options.size
        if (responses.isEmpty()) return Result(votes = List(size) { 0 })
        return when (question.subtype) {
            RankedSubtype.INSTANT_RUNOFF -> instantRunoff(size, responses)
            RankedSubtype.BORDA_COUNT -> bordaCount(size, responses)
            else -> {
                log.warn("Unknown ranked subtype ${question.subtype}")
                Result()
            }
        }
    }

    private fun instantRunoff(size: Int, responses: List<Response>): Result {
        val selections = responses.mapNotNull { r -> r.selections?.takeIf { it.size == size } }
        var votes = IntArray(size) { 0 }
        val remaining = (0 until size).toMutableSet()
        while (remaining.size > 1) {
            for (s in selections) {
                val vote = s.find { it in remaining }!!
                votes[vote]++
            }
            val lowest = votes.withIndex().filter { (i, _) -> i in remaining }.minBy { (_, v) -> v }!!.index
            remaining.remove(lowest)
            votes = IntArray(size) { 0 }
        }
        return Result(votes = List(size) { i -> if (i in remaining) selections.size else 0 })
    }

    private fun bordaCount(size: Int, responses: List<Response>): Result {
        val selections = responses.mapNotNull { r -> r.selections?.takeIf { it.size == size } }
        val votes = IntArray(size) { 0 }
        for (s in selections) {
            for ((i, v) in s.zip(size downTo 1)) {
                votes[i] += v
            }
        }
        return Result(votes = votes.asList())
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResultsCalculator::class.java)
    }
}