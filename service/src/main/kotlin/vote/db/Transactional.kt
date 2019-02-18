package vote.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import vote.config.VoteConfig
import javax.inject.Inject
import javax.sql.DataSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Transactional @Inject constructor(private val config: VoteConfig, private val dataSource: DataSource) {
    private suspend inline fun <T> tx(crossinline block: suspend CoroutineScope.(DSLContext) -> T): T {
        return suspendCoroutine { cont ->
            try {
                dataSource.connection.use { conn ->
                    val dsl = DSL.using(conn, config.data.sqlDialect)
                    dsl.transactionResult { config ->
                        runBlocking { cont.resume(block(DSL.using(config))) }
                    }
                }
            } catch (dae: DataAccessException) {
                log.warn("Caught: $dae")
                cont.resumeWithException(dae.cause ?: dae)
            } catch (e: Throwable) {
                cont.resumeWithException(e)
            }
        }
    }

    suspend fun <TDao, R> withDao(provider: DaoProvider<TDao>, block: suspend CoroutineScope.(TDao) -> R): R {
        return tx { block(provider.get(it)) }
    }

    suspend fun <TDao1, TDao2, R> withDaos(
            p1: DaoProvider<TDao1>,
            p2: DaoProvider<TDao2>,
            block: suspend CoroutineScope.(TDao1, TDao2) -> R): R {
        return tx { block(p1.get(it), p2.get(it)) }
    }

    suspend fun <TDao1, TDao2, TDao3, R> withDaos(
            p1: DaoProvider<TDao1>,
            p2: DaoProvider<TDao2>,
            p3: DaoProvider<TDao3>,
            block: suspend CoroutineScope.(TDao1, TDao2, TDao3) -> R): R {
        return tx { block(p1.get(it), p2.get(it), p3.get(it)) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Transactional::class.java)
    }
}
