package vote.config

import org.jooq.SQLDialect

class VoteConfig(val data: DataConfig)

class DataConfig(
        val user: String,
        val password: String,
        val jdbcUrl: String,

        val dataSourceClassName: String,
        val sqlDialect: SQLDialect
)
