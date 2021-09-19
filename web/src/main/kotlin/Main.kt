import asynclite.async
import auth.AuthPanel
import auth.AuthorizedClient
import auth.api.v1.LookupParams
import components.create.CreatePage
import components.details.DetailsPage
import components.history.HistoryPage
import components.results.ResultsPage
import components.vote.VotePage
import org.w3c.dom.url.URLSearchParams
import services.*
import kotlinx.browser.window
import multiplatform.api.FetchClient

fun main() {
    val search = URLSearchParams(window.location.search)
    val initPage = when {
        search.get("vote") != null -> Page.VOTE
        search.get("results") != null -> Page.RESULTS
        else -> Page.HISTORY
    }
    window.history.replaceState(initPage.name, "")

    async {
        val page = if (auth.isSignedIn()) {
            val user = runCatching {
                AuthorizedClient(FetchClient()).callApi(auth.api.v1.lookup, LookupParams())
            }.getOrNull()
            if (user == null) {
                auth.signOut()
                window.location.reload()
                return@async
            }
            initPage
        } else {
            Page.SIGNIN
        }

        val router = Router(page) { (it as? String)?.let { s -> Page.valueOf(s) } }
        val api = VoteApiClient { auth.getToken()!! }
        val service = VoteService(router, api)

        kui.mountComponent("app", router.component { p ->
            when (p) {
                Page.SIGNIN -> AuthPanel()
                Page.HISTORY -> HistoryPage(service)
                Page.CREATE -> CreatePage(service)
                Page.VOTE -> VotePage(service)
                Page.RESULTS -> ResultsPage(service)
                Page.DETAILS -> DetailsPage(service)
            }
        })
    }
}
