import components.create.CreatePage
import components.history.HistoryPage
import components.results.ResultsPage
import components.signin.SignInPage
import components.vote.VotePage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import services.*
import kotlin.browser.window

fun main() {
    val search = URLSearchParams(window.location.search)
    val initPage = when {
        search.get("vote") != null -> Page.VOTE
        search.get("results") != null -> Page.RESULTS
        else -> Page.HISTORY
    }
    window.history.replaceState(initPage.name, "")

    GlobalScope.launch {
        // NOTE: this will fail build if not provided
        val clientId = gapiClientId

        val signIn = GoogleSignIn(UsersApiClient())

        signIn.load()
        signIn.init(clientId, "email")

        val page = if (signIn.isSignedIn()) initPage else Page.SIGNIN

        val router = Router(page) { (it as? String)?.let { s -> Page.valueOf(s) } }
        val api = VoteApiClient { signIn.getUser().getAuthResponse().id_token }
        val service = VoteService(router, api)

        kui.mountComponent("app", router.component { p ->
            when (p) {
                Page.SIGNIN -> SignInPage(signIn, router, initPage)
                Page.HISTORY -> HistoryPage(service)
                Page.CREATE -> CreatePage(service)
                Page.VOTE -> VotePage(service)
                Page.RESULTS -> ResultsPage(service)
            }
        })
    }
}
