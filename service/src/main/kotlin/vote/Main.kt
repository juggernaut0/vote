package vote

import com.google.inject.Guice
import com.google.inject.Injector
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.event.Level
import vote.auth.googleToken
import vote.config.VoteConfig
import vote.inject.VoteModule
import vote.resources.Resource
import vote.resources.UsersResource
import vote.resources.VoteResource

@KtorExperimentalAPI
fun main() {
    val config = VoteConfig.fromEnv()
    val injector = Guice.createInjector(VoteModule(config))
    val server = embeddedServer(Jetty, 9000) {
        install(CallLogging) {
            level = Level.INFO
        }
        install(Authentication) {
            googleToken(injector)
        }
        routing {
            route("/api/v1") {
                registerRoutes<VoteResource>(injector)
                registerRoutes<UsersResource>(injector)
            }
        }
    }
    server.start(wait = true)
}

inline fun <reified T : Resource> Route.registerRoutes(injector: Injector) {
    injector.getInstance(T::class.java).register(this)
}
