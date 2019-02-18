package vote.db

import org.jooq.DSLContext

interface DaoProvider<T> {
    fun get(dsl: DSLContext): T
}
