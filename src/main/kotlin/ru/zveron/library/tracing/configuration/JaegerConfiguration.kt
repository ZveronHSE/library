package ru.zveron.library.tracing.configuration

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import mu.KLogging
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class JaegerConfiguration {

    @Value("\${spring.application.name}")
    private val serviceName: String = ""

    companion object : KLogging()

    @Bean
    fun jaegerSpanExporter(): JaegerGrpcSpanExporter {
        return JaegerGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:14250")
            .setTimeout(Duration.ofSeconds(5))
            .build()
    }

    /**
     * This method creates an instance of SdkTracerProvider, which is responsible for creating and managing spans.
     * It adds a BatchSpanProcessor to the provider, which batches spans before sending them to the exporter.
     * It also sets the service name as an attribute on the resource.
     */
    @Bean
    fun sdkTracerProvider(
        jaegerSpanExporter: JaegerGrpcSpanExporter,
    ): SdkTracerProvider {
        return SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(jaegerSpanExporter).build())
            .setResource(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)))
            .build()
    }

    /**
     * This method creates an instance of OpenTelemetry, which is the main entry point for using OpenTelemetry APIs.
     * It uses the SdkTracerProvider created by the sdkTracerProvider() method and the ContextPropagators provided
     * by the propagatorsProvider parameter. It sets the OpenTelemetry instance as the global instance
     */
    @Bean
    fun openTelemetry(
        propagatorsProvider: ObjectProvider<ContextPropagators>,
        sdkTracerProvider: SdkTracerProvider
    ): OpenTelemetry {
        val propagators = propagatorsProvider.getIfAvailable { ContextPropagators.noop() }
        val openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(propagators)
            .build()

        GlobalOpenTelemetry.set(openTelemetry)
        logger.info("Open Telemetry successfully initialized")

        return openTelemetry
    }
}