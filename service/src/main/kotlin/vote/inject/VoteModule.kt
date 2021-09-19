package vote.inject

import com.google.inject.AbstractModule
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import kotlinx.serialization.json.Json
import vote.config.VoteConfig
import javax.sql.DataSource

class VoteModule(private val config: VoteConfig) : AbstractModule() {
    override fun configure() {
        bindInstance(config)
        provide { dataSource() }.asEagerSingleton()
        provide { Json { ignoreUnknownKeys = true; encodeDefaults = false } }.asEagerSingleton()
        bindInstance("authClient", authClient())
    }

    private fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            dataSourceClassName = config.data.dataSourceClassName

            addDataSourceProperty("user", config.data.user)
            addDataSourceProperty("password", config.data.password)
            addDataSourceProperty("url", config.data.jdbcUrl)
        }
        return HikariDataSource(config)
    }

    private fun authClient(): HttpClient {
        return HttpClient(Apache) {
            defaultRequest {
                url.host = config.auth.host
                config.auth.port?.let { url.port = it }
            }
        }
    }

    private inline fun <reified T> bindInstance(instance: T, typeLiteral: Boolean = false) = bindInstance(null, instance, typeLiteral)
    private inline fun <reified T> bindInstance(name: String?, instance: T, typeLiteral: Boolean = false) =
            let { if (typeLiteral) bind(typeLiteral()) else  bind(T::class.java) }
                    .let { if (name != null) it.annotatedWith(Names.named(name)) else it }
                    .toInstance(instance)
    private inline fun <reified T> provide(crossinline provider: () -> T) =
            bind(T::class.java)
                    .toProvider(Provider<T> { provider() })

    private inline fun <reified T> Multibinder<in T>.addBindingTo() = addBinding().to(T::class.java)

    private inline fun <reified T> typeLiteral() = object : TypeLiteral<T>(){}
}