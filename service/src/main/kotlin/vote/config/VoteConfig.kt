package vote.config

import org.jooq.SQLDialect

class VoteConfig(val app: AppConfig, val auth: AuthConfig, val data: DataConfig)

class AppConfig(val port: Int)

class AuthConfig(val host: String, val port: Int?, val mock: Boolean)

class DataConfig(
    val user: String,
    val password: String,
    val jdbcUrl: String,

    val dataSourceClassName: String,
    val sqlDialect: SQLDialect,
)
