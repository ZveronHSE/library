package ru.zveron.library.grpc.configuration

import io.grpc.ClientInterceptor
import io.grpc.ServerInterceptor
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
import ru.zveron.library.tracing.configuration.GrpcTracingConfiguration

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(GrpcTracingConfiguration::class)
class GrpcConfiguration {

    @Order(2)
    @GrpcGlobalServerInterceptor
    @ConditionalOnProperty("platform.grpc.apigateway.metadata", havingValue = "true", matchIfMissing = true)
    fun metadataInterceptor(): ServerInterceptor {
        return MetadataInterceptor()
    }

    @Order(3)
    @GrpcGlobalClientInterceptor
    @ConditionalOnMissingBean(LoggingClientInterceptor::class)
    @ConditionalOnProperty("platform.grpc.client.logging-enabled", havingValue = "true", matchIfMissing = true)
    fun loggingClientInterceptor(): ClientInterceptor {
        return LoggingClientInterceptor()
    }

    @Order(4)
    @GrpcGlobalServerInterceptor
    @ConditionalOnMissingBean(LoggingServerInterceptor::class)
    @ConditionalOnProperty("platform.grpc.server.logging-enabled", havingValue = "true", matchIfMissing = true)
    fun loggingServerInterceptor(): ServerInterceptor {
        return LoggingServerInterceptor()
    }
}