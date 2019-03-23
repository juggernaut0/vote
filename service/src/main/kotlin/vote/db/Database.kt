package vote.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import vote.config.VoteConfig
import vote.db.query.QueryExecutor
import javax.inject.Inject
import javax.sql.DataSource

class Database @Inject constructor(private val config: VoteConfig, private val dataSource: DataSource) {
    suspend fun <R> transaction(block: suspend CoroutineScope.(QueryExecutor) -> R): R {
        return try {
            dataSource.connection.use { conn ->
                val dsl = DSL.using(conn, config.data.sqlDialect)
                dsl.transactionResultAsync { config ->
                    runBlocking { block(QueryExecutor(DSL.using(config))) }
                }.await()
            }
        } catch (dae: DataAccessException) {
            log.warn("Caught: $dae")
            throw (dae.cause ?: dae)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Database::class.java)
    }
}
