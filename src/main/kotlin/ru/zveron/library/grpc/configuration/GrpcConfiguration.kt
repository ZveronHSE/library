package ru.zveron.library.grpc.configuration

import io.grpc.ClientInterceptor
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import ru.zveron.library.grpc.interceptor.LoggingClientInterceptor

@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
class GrpcConfiguration {
    @GrpcGlobalClientInterceptor
    @ConditionalOnMissingBean(LoggingClientInterceptor::class)
    @ConditionalOnProperty("platform.grpc.client.logging", havingValue = "true", matchIfMissing = true)
    fun loggingClientInterceptor(): ClientInterceptor {
        return LoggingClientInterceptor()
    }
}