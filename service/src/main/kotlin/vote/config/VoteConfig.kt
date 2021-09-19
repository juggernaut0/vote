package vote.config

import org.jooq.SQLDialect
import java.net.URI

class VoteConfig(val auth: AuthConfig, val data: DataConfig) {
    companion object {
        fun fromEnv(): VoteConfig {
            return VoteConfig(
                auth = AuthConfig("localhost", 9001),
                data = DataConfig.fromEnv(),
            )
        }
    }
}

class AuthConfig(val host: String, val port: Int?)

class DataConfig(
        val user: String,
        val password: String,
        val jdbcUrl: String,

        val dataSourceClassName: String,
        val sqlDialect: SQLDialect
) {
    companion object {
        fun fromEnv(): DataConfig {
            val env: String? = System.getenv("DATABASE_URL")
            return if (env != null) {
                val uri = URI(env)
                val (user, pass) = uri.userInfo.split(":")
                val url = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?sslmode=require"
                DataConfig(
                        user = user,
                        password = pass,
                        jdbcUrl = url,
                        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource",
                        sqlDialect = SQLDialect.POSTGRES
                )
            } else {
                DataConfig(
                        user = "vote",
                        password = "vote",
                        jdbcUrl = "jdbc:postgresql://localhost:6432/vote",
                        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource",
                        sqlDialect = SQLDialect.POSTGRES
                )
            }
        }
    }
}
