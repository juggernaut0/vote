package vote

import auth.javalin.MockAuthHandler
import auth.javalin.TokenAuthProvider
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.javalin.Javalin
import multiplatform.api.BlockingApiClient
import multiplatform.javalin.AuthenticationPlugin
import org.slf4j.LoggerFactory
import vote.config.VoteConfig
import vote.db.DbMigration.runMigrations
import vote.inject.DaggerVoteInjector
import vote.inject.VoteModule
import vote.resources.IndexResource
import vote.resources.VoteResource
import vote.resources.registerResource
import javax.inject.Inject
import javax.inject.Named

fun main() {
    val config = ConfigFactory.load().extract<VoteConfig>()
    runMigrations(config.data.jdbcUrl, config.data.user, config.data.password)
    val injector = DaggerVoteInjector.builder().voteModule(VoteModule(config)).build()
    injector.app().app().start(config.app.port)
}

class VoteApp @Inject constructor(
    private val config: VoteConfig,
    private val indexResource: IndexResource,
    private val voteResource: VoteResource,
    @Named("authClient") private val authClient: BlockingApiClient,
) {
    private val log = LoggerFactory.getLogger(VoteApp::class.java)

    fun app(): Javalin {
        return Javalin
            .create { javalinConfig ->
                javalinConfig.useVirtualThreads = true
                javalinConfig.requestLogger.http { ctx, dur ->
                    val pathWithQuery = ctx.path() + ctx.queryString()?.let { "?$it" }.orEmpty()
                    log.info("${ctx.status()}: ${ctx.method()} - $pathWithQuery in ${dur.toInt()}ms")
                }
                javalinConfig.registerPlugin(AuthenticationPlugin {
                    register(TokenAuthProvider(authClient))
                })
                javalinConfig.staticFiles.add { staticFiles ->
                    staticFiles.directory = "/static"
                    staticFiles.hostedPath = "/vote"
                }
            }
            .registerResource(voteResource)
            .registerResource(indexResource)
            .also {
                if (config.auth.mock) {
                    MockAuthHandler().registerRoutes(it)
                }
            }
    }
}
