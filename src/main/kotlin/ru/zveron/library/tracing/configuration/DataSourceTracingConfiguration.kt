package ru.zveron.library.tracing.configuration

import com.zaxxer.hikari.HikariDataSource
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.OpenTelemetryDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


@Configuration
@AutoConfigureAfter(OpenTelemetry::class)
class DataSourceTracingConfiguration {
    @Value("\${spring.datasource.url:}")
    private lateinit var jdbcUrl: String

    @Value("\${spring.datasource.username:}")
    private lateinit var username: String

    @Value("\${spring.datasource.password:}")
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
}