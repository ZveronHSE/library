package ru.zveron.library.grpc.interceptor.tracing

import io.grpc.Metadata
import io.grpc.Status
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

    fun Span.setExceptionStatus(exception: java.lang.Exception) {
        this.addEvent(
            EXCEPTION_EVENT, Attributes.of(
                AttributeKey.stringKey("exception.detail"), exception.cause!!.message.toString(),
                SemanticAttributes.EXCEPTION_STACKTRACE, exception.stackTraceToString(),
                SemanticAttributes.EXCEPTION_MESSAGE, exception.message.toString(),
            )
        )

        this.setStatus(StatusCode.ERROR)
    }

    fun Span.onClose(status: Status) {
        if (!status.isOk && status.cause != null) {
            val exception = status.asException()
            this.setExceptionStatus(exception)
        }

        this.setAttribute(RPC_CODE, status.code.name)
        this.end()
    }
}