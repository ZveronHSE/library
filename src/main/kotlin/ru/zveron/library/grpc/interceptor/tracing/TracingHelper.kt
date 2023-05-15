package ru.zveron.library.grpc.interceptor.tracing

import io.grpc.Metadata
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes

object TracingHelper {
    private const val EXCEPTION_EVENT = "exception"
    const val RPC_CODE = "rpc.grpc.status_code"
    val traceIdKey: Metadata.Key<String> = Metadata.Key.of("trace_id", Metadata.ASCII_STRING_MARSHALLER)
    val spanIdKey: Metadata.Key<String> = Metadata.Key.of("span_id", Metadata.ASCII_STRING_MARSHALLER)

    fun setExceptionStatus(span: Span, exception: java.lang.Exception) {
        span.addEvent(
            EXCEPTION_EVENT, Attributes.of(
                AttributeKey.stringKey("exception.detail"), exception.cause!!.message.toString(),
                SemanticAttributes.EXCEPTION_STACKTRACE, exception.stackTraceToString(),
                SemanticAttributes.EXCEPTION_MESSAGE, exception.message.toString(),
            )
        )

        span.setStatus(StatusCode.ERROR)
    }
}