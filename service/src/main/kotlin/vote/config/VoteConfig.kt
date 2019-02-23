package vote.config

import org.jooq.SQLDialect
import java.lang.RuntimeException
import java.net.URI

class VoteConfig(val data: DataConfig, val signIn: SignInConfig) {
    companion object {
        fun fromEnv(): VoteConfig {
            return VoteConfig(
                    data = DataConfig.fromEnv(),
                    signIn = SignInConfig.fromEnv()
            )
        }
    }
}

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
                        sqlDialect = SQLDialect.POSTGRES_10
                )
            } else {
                DataConfig(
                        user = "vote",
                        password = "vote",
                        jdbcUrl = "jdbc:postgresql://localhost:5432/vote",
                        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource",
                        sqlDialect = SQLDialect.POSTGRES_10
                )
            }
        }
    }
}

class SignInConfig(
        val clientId: String
) {
    companion object {
        fun fromEnv(): SignInConfig {
            val env: String = System.getenv("GOOGLE_SIGNIN_CLIENT_ID")
                    ?: throw RuntimeException("No google signin client id set")
            return SignInConfig(env)
        }
    }
}
