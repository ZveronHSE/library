package ru.zveron.library.grpc.configuration

import io.grpc.ClientInterceptor
import io.grpc.ServerInterceptor
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import ru.zveron.library.grpc.interceptor.LoggingClientInterceptor
import ru.zveron.library.grpc.interceptor.LoggingServerInterceptor
import ru.zveron.library.grpc.interceptor.MetadataInterceptor

@Configuration(proxyBeanMethods = false)
class GrpcConfiguration {

    @Order(1)
    @GrpcGlobalServerInterceptor
    @ConditionalOnProperty("platform.grpc.apigateway.metadata", havingValue = "true", matchIfMissing = true)
    fun metadataInterceptor(): ServerInterceptor {
        return MetadataInterceptor()
    }

    @Order(2)
    @GrpcGlobalClientInterceptor
    @ConditionalOnMissingBean(LoggingClientInterceptor::class)
    @ConditionalOnProperty("platform.grpc.client.logging", havingValue = "true", matchIfMissing = true)
    fun loggingClientInterceptor(): ClientInterceptor {
        return LoggingClientInterceptor()
    }

    @Order(2)
    @GrpcGlobalServerInterceptor
    @ConditionalOnMissingBean(LoggingServerInterceptor::class)
    @ConditionalOnProperty("platform.grpc.server.logging", havingValue = "true", matchIfMissing = true)
    fun loggingServerInterceptor(): ServerInterceptor {
        return LoggingServerInterceptor()
    }
}