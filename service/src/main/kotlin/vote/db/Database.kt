package vote.db

import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import vote.config.VoteConfig
import vote.db.query.QueryExecutor
import javax.inject.Inject
import javax.sql.DataSource

class Database @Inject constructor(private val config: VoteConfig, private val dataSource: DataSource) {
    fun <R> transaction(block: (QueryExecutor) -> R): R {
        return try {
            val dsl = DSL.using(dataSource, config.data.sqlDialect)
            dsl.transactionResult { config ->
                block(QueryExecutor(config.dsl()))
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
