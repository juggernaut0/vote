package vote.db

import org.flywaydb.core.Flyway
import java.net.URI

class DataSourceConfig(val url: String, val user: String, val pass: String)

//expects format: postgres://user:pass@host:port/path
fun fromCmdLine(args: Array<String>): DataSourceConfig? {
    val uri = URI(args.getOrNull(0) ?: return null)
    val (user, pass) = uri.userInfo.split(":")
    val url = URI("jdbc:postgresql", null, uri.host, uri.port, uri.path, uri.rawQuery, uri.rawFragment).toString()
    return DataSourceConfig(url, user, pass)
}

fun fromEnv(): DataSourceConfig? {
    val url = System.getenv("DB_JDBC_URL") ?: return null
    val user = System.getenv("DB_USER") ?: return null
    val pass = System.getenv("DB_PASSWORD") ?: return null
    return DataSourceConfig(url, user, pass)
}

fun runMigrations(config: DataSourceConfig) {
    Flyway.configure()
        .dataSource(config.url, config.user, config.pass)
        .load()
        .migrate()
}

fun main(args: Array<String>) {
    runMigrations(fromCmdLine(args) ?: fromEnv() ?: throw RuntimeException("Must provide database URL"))
}
