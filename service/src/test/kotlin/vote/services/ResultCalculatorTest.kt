package vote.services

import vote.api.v1.*
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ResultCalculatorTest {
    private fun pollResponseOf(vararg response: Response): PollResponse {
        return PollResponse(listOf(*response))
    }

    @Test
    fun freeform() {
        val poll = Poll(UUID.randomUUID(), "", listOf(
                Question("", QuestionType.FREEFORM, "", emptyList())
        ))
        val responses = listOf(
                pollResponseOf(Response.freeform("A")),
                pollResponseOf(Response.freeform("B")),
                pollResponseOf(Response.freeform("C"))
        )

        val rc = ResultsCalculator()

        val results = rc.calculateResults(poll, responses)

        assertEquals(1, results.results.size)
        assertNotNull(results.results[0].freeform)
        assertEquals(listOf("A", "B", "C"), results.results[0].freeform!!.sorted())
    }

    @Test
    fun selectOne() {
        val poll = Poll(UUID.randomUUID(), "", listOf(
                Question("", QuestionType.SELECT, SelectSubtype.SELECT_ONE, listOf("A", "B", "C"))
        ))
        val responses = listOf(
                pollResponseOf(Response.selections(listOf(2))),
                pollResponseOf(Response.selections(listOf(0))),
                pollResponseOf(Response.selections(listOf(1))),
                pollResponseOf(Response.selections(listOf(2))),
                pollResponseOf(Response.selections(listOf(0))),
                pollResponseOf(Response.selections(listOf(2)))
        )

        val rc = ResultsCalculator()

        val results = rc.calculateResults(poll, responses)

        assertEquals(1, results.results.size)
        assertNotNull(results.results[0].votes)
        assertEquals(listOf(2, 1, 3), results.results[0].votes!!)
    }

    @Test
    fun selectMany() {
        val poll = Poll(UUID.randomUUID(), "", listOf(
                Question("", QuestionType.SELECT, SelectSubtype.SELECT_MANY, listOf("A", "B", "C"))
        ))
        val responses = listOf(
                pollResponseOf(Response.selections(listOf(2, 0))),
                pollResponseOf(Response.selections(listOf(0, 1, 2))),
                pollResponseOf(Response.selections(listOf(1))),
                pollResponseOf(Response.selections(listOf(2, 1))),
                pollResponseOf(Response.selections(listOf(0))),
                pollResponseOf(Response.selections(listOf(2)))
        )

        val rc = ResultsCalculator()

        val results = rc.calculateResults(poll, responses)

        assertEquals(1, results.results.size)
        assertNotNull(results.results[0].votes)
        assertEquals(listOf(3, 3, 4), results.results[0].votes!!)
    }

    @Test
    fun instantRunoff() {
        val poll = Poll(UUID.randomUUID(), "", listOf(
                Question("", QuestionType.RANKED, RankedSubtype.INSTANT_RUNOFF, listOf("A", "B", "C"))
        ))
        val responses = listOf(
                pollResponseOf(Response.selections(listOf(2, 0, 1))),
                pollResponseOf(Response.selections(listOf(0, 1, 2))),
                pollResponseOf(Response.selections(listOf(1, 0, 2))),
                pollResponseOf(Response.selections(listOf(2, 1, 0))),
                pollResponseOf(Response.selections(listOf(0, 2, 1))),
                pollResponseOf(Response.selections(listOf(2, 1, 0))),
                pollResponseOf(Response.selections(listOf(0, 1, 2))),
                pollResponseOf(Response.selections(listOf(1, 0, 2))),
                pollResponseOf(Response.selections(listOf(2, 1, 0)))
        )

        val rc = ResultsCalculator()

        val results = rc.calculateResults(poll, responses)

        assertEquals(1, results.results.size)
        assertNotNull(results.results[0].votes)
        assertEquals(listOf(9, 0, 0), results.results[0].votes!!)
    }

    @Test
    fun bordaCount() {
        val poll = Poll(UUID.randomUUID(), "", listOf(
                Question("", QuestionType.RANKED, RankedSubtype.BORDA_COUNT, listOf("A", "B", "C"))
        ))
        val responses = listOf(
                pollResponseOf(Response.selections(listOf(2, 0, 1))),
                pollResponseOf(Response.selections(listOf(0, 1, 2))),
                pollResponseOf(Response.selections(listOf(1, 0, 2))),
                pollResponseOf(Response.selections(listOf(2, 1, 0))),
                pollResponseOf(Response.selections(listOf(0, 2, 1))),
                pollResponseOf(Response.selections(listOf(2, 1, 0)))
        )

        val rc = ResultsCalculator()

        val results = rc.calculateResults(poll, responses)

        assertEquals(1, results.results.size)
        assertNotNull(results.results[0].votes)
        assertEquals(listOf(12, 11, 13), results.results[0].votes!!)
    }
}