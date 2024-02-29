package vote.resources

import com.samskivert.mustache.Mustache
import io.javalin.Javalin
import io.javalin.http.ContentType
import io.javalin.http.Context
import multiplatform.UUID
import vote.db.Database
import vote.db.jooq.tables.records.PollRecord
import vote.db.query.PollQueries
import javax.inject.Inject

class IndexResource @Inject constructor(
    private val db: Database,
    private val pollQueries: PollQueries,
) : Resource {
    private val template = this::class.java.getResourceAsStream("/templates/index.html.mustache")!!.bufferedReader().use {
        Mustache.compiler().compile(it)
    }

    override fun register(app: Javalin) {
        app.get("/vote") { ctx ->
            val poll = getPoll(ctx)
            val params = if (poll != null) {
                val description = if (ctx.queryParam("vote") != null) {
                    "Vote on the '${poll.title}' poll."
                } else {
                    "Results of the '${poll.title}' poll."
                }
                mapOf(
                    "og" to mapOf(
                        "title" to poll.title,
                        "url" to ctx.fullUrl(),
                        "description" to description,
                    )
                )
            } else {
                emptyMap()
            }
            ctx.result(template.execute(params)).contentType(ContentType.TEXT_HTML)
        }
    }

    private fun getPoll(ctx: Context): PollRecord? {
        val pollIdStr = ctx.queryParam("vote") ?: ctx.queryParam("results") ?: return null
        val pollId = try {
            UUID.fromString(pollIdStr)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return db.transaction {
            it.run(pollQueries.getPoll(pollId))
        }
    }
}
