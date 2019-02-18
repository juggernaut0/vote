package vote.db

import org.flywaydb.core.Flyway
import java.net.URI

fun runMigrationsFromEnv(env: String) {
    val uri = URI(env)
    val (user, pass) = uri.userInfo.split(":")
    val url = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?sslmode=require"
    runMigrations(url, user, pass)
}

fun runMigrationsLocal() {
    runMigrations("jdbc:postgresql://localhost:5432/vote", "vote", "vote")
}

fun runMigrations(url: String, user: String, pass: String) {
    Flyway.configure()
        .dataSource(url, user, pass)
        .load()
        .migrate()
}

fun main() {
    val env: String? = System.getenv("DATABASE_URL")
    if (env != null) {
        runMigrationsFromEnv(env)
    } else {
        runMigrationsLocal()
    }
}