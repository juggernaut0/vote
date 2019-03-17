package vote.inject

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.ApacheHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.inject.AbstractModule
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.http.impl.client.HttpClients
import vote.config.VoteConfig
import vote.db.*
import javax.sql.DataSource

class VoteModule(private val config: VoteConfig) : AbstractModule() {
    override fun configure() {
        bindInstance(config)
        provide { dataSource() }.asEagerSingleton()

        bindInstance(daoProviderOf { PollDao(it) }, typeLiteral = true)
        bindInstance(daoProviderOf { ResponseDao(it) }, typeLiteral = true)
        bindInstance(daoProviderOf { VoteUserDao(it) }, typeLiteral = true)

        provide { googleIdTokenVerifier() }.asEagerSingleton()
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

    private fun googleIdTokenVerifier(): GoogleIdTokenVerifier {
        return GoogleIdTokenVerifier.Builder(
                ApacheHttpTransport(HttpClients.createDefault()),
                JacksonFactory()
        )
                .setAudience(listOf(config.signIn.clientId))
                .build()
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