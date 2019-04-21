package services

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import models.PollHistoryItem
import models.ResponseDetailsQuestion
import models.ResponseDetailsView
import org.w3c.dom.url.URLSearchParams
import vote.api.*
import vote.api.v1.*
import kotlin.browser.window

class VoteService(private val router: Router<Page>, private val api: VoteApiClient) {
    private fun changePage(newPage: Page, id: UUID? = null) {
        val url = when (newPage) {
            Page.RESULTS, Page.DETAILS -> "?results=$id"
            Page.VOTE -> "?vote=$id"
            else -> ""
        }
        window.history.pushState(newPage.name, "", url)
        router.state = newPage
    }

    fun goToCreatePage() = changePage(Page.CREATE)
    fun goToResultsPage() = changePage(Page.RESULTS, getCurrentPollId())
    fun goToDetailsPage() = changePage(Page.DETAILS, getCurrentPollId())

    suspend fun createPoll(req: PollCreateRequest) {
        val poll = api.createPoll(req)
        changePage(Page.VOTE, poll.id)
    }

    suspend fun isCreator(pollId: UUID): Boolean {
        return api.isCreator(pollId)
    }

    suspend fun getCurrentPoll(): Pair<Poll?, PollResponse?> {
        val id = getCurrentPollId()
        return coroutineScope {
            val poll = async { api.getPoll(id) }
            val resp = async { api.getResponse(id) }
            poll.await() to resp.await()
        }
    }

    suspend fun getResponseDetails(): List<ResponseDetailsView> {
        val id = getCurrentPollId()
        return coroutineScope {
            val poll = api.getPoll(id) ?: throw IllegalStateException("No poll returned for current poll id {$id}")
            val resps = api.getResponses(id)
            resps.map {
                ResponseDetailsView(
                        id = it.id,
                        email = it.email,
                        active = it.active,
                        questions = poll.questions.zip(it.responses) { q, r -> getSelections(q, r) }
                )
            }
        }
    }

    private fun getSelections(question: Question, response: Response): ResponseDetailsQuestion {
        val resps = when {
            !response.freeform.isNullOrBlank() -> listOf(response.freeform!!)
            !response.multiFreeform.isNullOrEmpty() -> response.multiFreeform!!
            !response.selections.isNullOrEmpty() -> response.selections!!.map { i -> question.options[i] }
            else -> emptyList()
        }
        return ResponseDetailsQuestion(
                title = question.question,
                responses = resps
        )
    }

    suspend fun submitResponse(id: UUID, response: PollResponse) {
        api.submitResponse(id, response)
        changePage(Page.RESULTS, id)
    }

    suspend fun getResults(): Pair<Poll, PollResults>? {
        val id = getCurrentPollId()
        return coroutineScope {
            val poll = async { api.getPoll(id) }
            val res = async { api.getResults(id) }
            poll.await() to res.await()
        }.flattenNulls()
    }

    suspend fun getPollHistory(): Pair<List<PollHistoryItem>, List<PollHistoryItem>> {
        val history = api.getPollHistory()
        val createdIds = history.created.mapTo(mutableSetOf()) { it.id }
        val respondedIds = history.responded.mapTo(mutableSetOf()) { it.id }
        val created = history.created.map { PollHistoryItem(it.id, it.title, it.id in respondedIds) }
        val responded = history.responded.mapNotNull { PollHistoryItem(it.id, it.title).takeUnless { phi -> phi.id in createdIds } }
        return created to responded
    }

    suspend fun deactivateResponse(responseId: UUID) {
        api.deactivateResponse(getCurrentPollId(), responseId)
    }

    private fun getCurrentPollId(): UUID {
        val search = URLSearchParams(window.location.search)
        val id = search.get("vote") ?: search.get("results") ?: throw IllegalStateException("No current poll")
        return UUID(id)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Any, B : Any> Pair<A?, B?>.flattenNulls(): Pair<A, B>? {
        return if (first != null && second != null) this as Pair<A, B>
        else null
    }
}
