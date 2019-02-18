package vote.inject

import com.google.inject.AbstractModule
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import vote.config.VoteConfig
import vote.db.DaoProvider
import vote.db.PollDao
import vote.db.ResponseDao
import javax.sql.DataSource

class VoteModule(private val config: VoteConfig) : AbstractModule() {
    override fun configure() {
        bindInstance(config)
        bindInstance(dataSource())
        bind(typeLiteral<DaoProvider<PollDao>>()).toInstance(PollDao)
        bind(typeLiteral<DaoProvider<ResponseDao>>()).toInstance(ResponseDao)
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

    private inline fun <reified T> bindInstance(instance: T) = bindInstance(null, instance)
    private inline fun <reified T> bindInstance(name: String?, instance: T) =
            bind(T::class.java)
                    .let { if (name != null) it.annotatedWith(Names.named(name)) else it }
                    .toInstance(instance)
    private inline fun <reified T> provide(crossinline provider: () -> T) =
            bind(T::class.java)
                    .toProvider(Provider<T> { provider() })

    private inline fun <reified T> Multibinder<in T>.addBindingTo() = addBinding().to(T::class.java)

    private inline fun <reified T> typeLiteral() = object : TypeLiteral<T>(){}
}