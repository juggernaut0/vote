import components.create.CreatePage
import components.results.ResultsPage
import components.vote.VotePage
import org.w3c.dom.url.URLSearchParams
import services.Page
import services.Router
import services.VoteApiClient
import services.VoteService
import kotlin.browser.window

fun main() {
    val search = URLSearchParams(window.location.search)
    val initPage = when {
        search.get("vote") != null -> Page.VOTE
        search.get("results") != null -> Page.RESULTS
        else -> Page.CREATE
    }
    window.history.replaceState(initPage.name, "")
    val router = Router(initPage) { (it as? String)?.let { s -> Page.valueOf(s) } }
    val api = VoteApiClient()
    val service = VoteService(router, api)

    kui.mountComponent("app", router.component { page ->
        when (page) {
            Page.CREATE -> CreatePage(service)
            Page.VOTE -> VotePage(service)
            Page.RESULTS -> ResultsPage(service)
        }
    })
}
