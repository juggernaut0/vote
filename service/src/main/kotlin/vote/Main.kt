package vote

import auth.token
import com.google.inject.Guice
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.client.*
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import multiplatform.ktor.installWebApplicationExceptionHandler
import org.slf4j.event.Level
import vote.config.VoteConfig
import vote.db.DataSourceConfig
import vote.db.runMigrations
import vote.inject.VoteModule
import vote.resources.VoteResource
import javax.inject.Inject
import javax.inject.Named

fun main() {
    val config = VoteConfig.fromEnv()
    runMigrations(DataSourceConfig(config.data.jdbcUrl, config.data.user, config.data.password))
    val injector = Guice.createInjector(VoteModule(config))
    injector.getInstance(VoteApp::class.java).start()
}

class VoteApp @Inject constructor(
    private val voteResource: VoteResource,
    @Named("authClient") private val authClient: HttpClient,
) {
    fun start() {
        val server = embeddedServer(Jetty, 9003) {
            install(CallLogging) {
                level = Level.INFO
            }
            install(Authentication) {
                token(httpClient = authClient)
            }
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            routing {
                voteResource.register(this)
                get("vote") { call.respondRedirect("vote/", permanent = true) }
                route("vote/") {
                    staticBasePackage = "static"
                    resources()
                    defaultResource("index.html")
                }
            }
        }
        server.start(wait = true)
    }
}
