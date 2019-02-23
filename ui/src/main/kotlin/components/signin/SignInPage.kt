package components.signin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kui.*
import services.GoogleSignIn
import services.Page
import services.Router

class SignInPage(private val signIn: GoogleSignIn, private val router: Router<Page>, private val redirect: Page) : Component() {
    private var signingIn = false

    private fun signIn() {
        GlobalScope.launch {
            signingIn = true
            render()
            signIn.signIn()
            router.state = redirect
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
        }
    }
}
