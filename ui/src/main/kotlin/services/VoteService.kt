package services

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.w3c.dom.url.URLSearchParams
import vote.api.*
import vote.api.v1.*
import kotlin.browser.window

class VoteService(private val router: Router<Page>, private val api: VoteApi) {
    private fun changePage(newPage: Page, id: UUID) {
        val url = when (newPage) {
            Page.RESULTS -> "?results=$id"
            Page.VOTE -> "?vote=$id"
            else -> ""
        }
        window.history.pushState(newPage.name, "", url)
        router.state = newPage
    }

    suspend fun createPoll(req: PollCreateRequest) {
        val poll = api.createPoll(req)
        changePage(Page.VOTE, poll.id)
    }

    suspend fun getCurrentPoll(): Pair<Poll?, PollResponse?> {
        val id = getCurrentPollId()
        return coroutineScope {
            val poll = async { api.getPoll(id) }
            val resp = async { api.getResponse(id) }
            poll.await() to resp.await()
        }
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
