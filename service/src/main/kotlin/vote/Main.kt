package vote

import com.google.inject.Guice
import com.google.inject.Injector
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.util.KtorExperimentalAPI
import org.jooq.SQLDialect
import org.slf4j.event.Level
import vote.config.DataConfig
import vote.config.VoteConfig
import vote.inject.VoteModule
import vote.resources.Resource
import vote.resources.VoteResource
import java.net.URI

@KtorExperimentalAPI
fun main() {
    val config = VoteConfig(
            data = dataConfigFromEnv()
    )
    val injector = Guice.createInjector(VoteModule(config))
    val server = embeddedServer(Jetty, 9000) {
        install(CallLogging) {
            level = Level.INFO
        }
        routing {
            route("/api/v1") {
                registerRoutes<VoteResource>(injector)
            }
        }
    }
    server.start(wait = true)
}

inline fun <reified T : Resource> Route.registerRoutes(injector: Injector) {
    injector.getInstance(T::class.java).register(this)
}

fun dataConfigFromEnv(): DataConfig {
    val env: String? = System.getenv("DATABASE_URL")
    return if (env != null) {
        val uri = URI(env)
        val (user, pass) = uri.userInfo.split(":")
        val url = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?sslmode=require"
        DataConfig(
                user = user,
                password = pass,
                jdbcUrl = url,
                dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource",
                sqlDialect = SQLDialect.POSTGRES_10
        )
    } else {
        DataConfig(
                user = "vote",
                password = "vote",
                jdbcUrl = "jdbc:postgresql://localhost:5432/vote",
                dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource",
                sqlDialect = SQLDialect.POSTGRES_10
        )
    }
}
