package ru.zveron.library.grpc.configuration

import io.grpc.ClientInterceptor
import io.grpc.ServerInterceptor
import io.opentelemetry.api.OpenTelemetry
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import ru.zveron.library.grpc.interceptor.LoggingClientInterceptor
import ru.zveron.library.grpc.interceptor.LoggingServerInterceptor
import ru.zveron.library.grpc.interceptor.MetadataInterceptor
import ru.zveron.library.grpc.interceptor.tracing.TracingClientInterceptor
import ru.zveron.library.grpc.interceptor.tracing.TracingServerInterceptor

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OpenTelemetry::class)
class GrpcConfiguration {
    @Order(1)
    @GrpcGlobalServerInterceptor
    @ConditionalOnProperty("platform.grpc.server.tracing-enabled", havingValue = "true", matchIfMissing = true)
    fun tracingServerInterceptor(openTelemetry: OpenTelemetry): ServerInterceptor {
        val tracing = openTelemetry.tracerProvider
            .tracerBuilder("TracingServerInterceptor")
            .build()

        return TracingServerInterceptor(tracing)
    }

    @Order(2)
    @GrpcGlobalClientInterceptor
    @ConditionalOnProperty("platform.grpc.client.tracing-enabled", havingValue = "true", matchIfMissing = true)
    fun tracingClientInterceptor(openTelemetry: OpenTelemetry): ClientInterceptor {
        val tracing = openTelemetry.tracerProvider
            .tracerBuilder("TracingClientInterceptor")
            .build()

        return TracingClientInterceptor(tracing)
    }

    @Order(3)
    @GrpcGlobalServerInterceptor
    @ConditionalOnProperty("platform.grpc.apigateway.metadata", havingValue = "true", matchIfMissing = true)
    fun metadataInterceptor(): ServerInterceptor {
        return MetadataInterceptor()
    }

    @Order(4)
    @GrpcGlobalServerInterceptor
    @ConditionalOnMissingBean(LoggingServerInterceptor::class)
    @ConditionalOnProperty("platform.grpc.server.logging-enabled", havingValue = "true", matchIfMissing = true)
    fun loggingServerInterceptor(): ServerInterceptor {
        return LoggingServerInterceptor()
    }

    @Order(5)
    @GrpcGlobalClientInterceptor
    @ConditionalOnMissingBean(LoggingClientInterceptor::class)
    @ConditionalOnProperty("platform.grpc.client.logging-enabled", havingValue = "true", matchIfMissing = true)
    fun loggingClientInterceptor(): ClientInterceptor {
        return LoggingClientInterceptor()
    }
}