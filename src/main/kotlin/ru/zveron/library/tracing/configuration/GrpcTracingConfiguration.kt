package ru.zveron.library.tracing.configuration

import io.grpc.ClientInterceptor
import io.grpc.ServerInterceptor
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(OpenTelemetry::class)
@ConditionalOnProperty("platform.tracing.grpc", havingValue = "true", matchIfMissing = true)
class GrpcTracingConfiguration {
    @Bean
    @ConditionalOnMissingBean(GrpcTelemetry::class)
    fun grpcOpenTelemetry(openTelemetry: OpenTelemetry): GrpcTelemetry =
        GrpcTelemetry.create(openTelemetry)

    @GrpcGlobalServerInterceptor
    fun grpcServerTracing(grpcTelemetry: GrpcTelemetry): ServerInterceptor =
        grpcTelemetry.newServerInterceptor()

    @GrpcGlobalClientInterceptor
    fun grpcClientTracing(grpcTelemetry: GrpcTelemetry): ClientInterceptor =
        grpcTelemetry.newClientInterceptor()
}
