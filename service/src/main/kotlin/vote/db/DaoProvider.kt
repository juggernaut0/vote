package vote.db

import org.jooq.DSLContext

interface DaoProvider<T> {
    fun get(dsl: DSLContext): T
}

inline fun <T> daoProviderOf(crossinline fn: (DSLContext) -> T) = object : DaoProvider<T> {
    override fun get(dsl: DSLContext): T = fn(dsl)
}
