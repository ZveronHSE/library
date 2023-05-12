package ru.zveron.library.tracing.configuration

import com.zaxxer.hikari.HikariDataSource
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.OpenTelemetryDataSource
import io.opentelemetry.instrumentation.r2dbc.v1_0.R2dbcTelemetry
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import javax.sql.DataSource


@Configuration
class DataSourceTracingConfiguration {
    @Value("\${spring.datasource.url}")
    private lateinit var jdbcUrl: String

    @Value("\${spring.r2dbc.url}")
    private lateinit var r2dbcUrl: String

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    @Bean
    @ConditionalOnProperty("platform.tracing.jdbc", havingValue = "true", matchIfMissing = true)
    fun dataSource(
        openTelemetry: OpenTelemetry,
    ): DataSource {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = jdbcUrl
        dataSource.username = username
        dataSource.password = password

        return OpenTelemetryDataSource(dataSource, openTelemetry)
    }

    @Bean
    @ConditionalOnProperty("platform.tracing.r2dbc", havingValue = "true", matchIfMissing = true)
    fun initializer(
        openTelemetry: OpenTelemetry,
        connectionFactory: ConnectionFactory
    ): ConnectionFactoryInitializer {
        val factoryOptions = ConnectionFactoryOptions.parse(r2dbcUrl)

        return ConnectionFactoryInitializer().also {
            it.setConnectionFactory(
                R2dbcTelemetry
                    .create(openTelemetry)
                    .wrapConnectionFactory(connectionFactory, factoryOptions)
            )
        }
    }
}