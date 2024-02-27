package vote.inject

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import multiplatform.api.BlockingApiClient
import multiplatform.api.ZeroDepApiClient
import vote.config.VoteConfig
import javax.inject.Named
import javax.inject.Singleton
import javax.sql.DataSource

@Module
class VoteModule(private val config: VoteConfig) {
    @Provides
    fun config(): VoteConfig = config

    @Provides
    @Singleton
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            dataSourceClassName = config.data.dataSourceClassName

            addDataSourceProperty("user", config.data.user)
            addDataSourceProperty("password", config.data.password)
            addDataSourceProperty("url", config.data.jdbcUrl)
        }
        return HikariDataSource(config)
    }

    @Provides
    @Singleton
    @Named("authClient")
    fun authClient(): BlockingApiClient {
        return ZeroDepApiClient(baseUrl = "http://${config.auth.host}:${config.auth.port}")
    }

    @Provides
    @Singleton
    fun json(): Json {
        return Json { ignoreUnknownKeys = true; encodeDefaults = false }
    }
}
