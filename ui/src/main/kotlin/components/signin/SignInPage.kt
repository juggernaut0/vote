package components.signin

import components.CollapsibleAlert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import services.FetchException
import services.GoogleSignIn
import services.Page
import services.Router

class SignInPage(private val signIn: GoogleSignIn, private val router: Router<Page>, private val redirect: Page) : Component() {
    private var signingIn = false
    private val alert = CollapsibleAlert("sign-in-failed-alert")

    private fun signIn() {
        GlobalScope.launch {
            signingIn = true
            render()
            try {
                signIn.signIn()
                router.state = redirect
            } catch (e: FetchException) {
                console.error(e)
                alert.show("There was an error signing in. Refresh the page and try again. (${e.status})")
            }
        }
    }

    override fun render() {
        markup().div {
            p { +"Please sign in to access this app" }
            button(Props(classes = listOf("btn", "btn-warning"), click = { signIn() }, disabled = signingIn)) {
                if (signingIn) {
                    +"Signing in..."
                } else {
                    +"Sign in with Google"
                }
            }
            component(alert)
        }
    }
}
