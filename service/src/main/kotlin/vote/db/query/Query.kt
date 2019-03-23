package vote.db.query

import org.jooq.DSLContext

interface Query<T> {
    suspend fun execute(dsl: DSLContext): T
}

class QueryExecutor(private val dsl: DSLContext) {
    suspend fun <T> run(query: Query<T>): T = query.execute(dsl)
}

inline fun <T> queryOf(crossinline q: suspend (DSLContext) -> T): Query<T> {
    return object : Query<T> {
        override suspend fun execute(dsl: DSLContext): T = q(dsl)
    }
}
