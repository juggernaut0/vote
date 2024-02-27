package vote.db.query

import org.jooq.DSLContext

interface Query<T> {
    fun execute(dsl: DSLContext): T
}

class QueryExecutor(private val dsl: DSLContext) {
    fun <T> run(query: Query<T>): T = query.execute(dsl)
}

inline fun <T> queryOf(crossinline q: (DSLContext) -> T): Query<T> {
    return object : Query<T> {
        override fun execute(dsl: DSLContext): T = q(dsl)
    }
}
