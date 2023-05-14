package ru.zveron.library.grpc.interceptor

import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes.EXCEPTION_MESSAGE
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes.EXCEPTION_STACKTRACE
import mu.KLogging

open class TracingServerInterceptor(
    private val tracer: Tracer,
) : ServerInterceptor {

    companion object : KLogging() {
        private val traceIdKey = Metadata.Key.of("trace_id", Metadata.ASCII_STRING_MARSHALLER)
        private val spanIdKey = Metadata.Key.of("span_id", Metadata.ASCII_STRING_MARSHALLER)
        private val MESSAGE_TYPE = AttributeKey.stringKey("message.type")
        private const val RPC_CODE = "rpc.grpc.status_code"
        private const val MESSAGE_EVENT = "message"
        private const val EXCEPTION_EVENT = "exception"
    }

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        var spanBuilder = tracer
            .spanBuilder("${call.methodDescriptor.serviceName!!}/${call.methodDescriptor.bareMethodName!!}")
            .setSpanKind(SpanKind.SERVER)

        val parentSpanContext = getParentContextFromHeaders(headers)
        var context = Context.current()

        if (parentSpanContext != null) {
            context = context.with(Span.wrap(parentSpanContext))
            spanBuilder = spanBuilder.setParent(context)
        }

        val span = spanBuilder.startSpan()
        context = context.with(span)

        val wrappedCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                super.sendMessage(message)
                span.addEvent(
                    MESSAGE_EVENT, Attributes.of(
                        MESSAGE_TYPE,
                        "GET RESPONSE"
                    )
                )
            }

            override fun close(status: Status, trailers: Metadata) {
                super.close(status, trailers)
                if (!status.isOk && status.cause != null) {
                    val exception = status.asException()
                    setExceptionStatus(span, exception)
                }

                span.setAttribute(RPC_CODE, status.code.name)
                span.end()
            }

        }

        return context.makeCurrent().use {
            object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                try {
                    next.startCall(wrappedCall, headers)
                } catch (ex: Exception) {
                    // catched if get exception in interceptors
                    setExceptionStatus(span, ex)
                    throw ex
                }
            ) {
                override fun onMessage(message: ReqT) {
                    span.addEvent(
                        MESSAGE_EVENT, Attributes.of(
                            MESSAGE_TYPE,
                            "SEND REQUEST"
                        )
                    )

                    super.onMessage(message)
                }

                override fun onCancel() {
                    super.onCancel()
                    span.setAttribute(RPC_CODE, SemanticAttributes.RpcConnectRpcErrorCodeValues.CANCELLED)
                    span.setStatus(StatusCode.ERROR)
                    span.end()
                }
            }
        }
    }

    private fun setExceptionStatus(span: Span, exception: java.lang.Exception) {
        span.addEvent(
            EXCEPTION_EVENT, Attributes.of(
                AttributeKey.stringKey("exception.detail"), exception.cause!!.message.toString(),
                EXCEPTION_STACKTRACE, exception.stackTraceToString(),
                EXCEPTION_MESSAGE, exception.message.toString(),
            )
        )

        span.setStatus(StatusCode.ERROR)
    }

    private fun getParentContextFromHeaders(headers: Metadata): SpanContext? {
        val traceParentHeader = headers.get(traceIdKey)
        val spanParentHeader = headers.get(spanIdKey)

        if (traceParentHeader != null && spanParentHeader != null) {
            return SpanContext.createFromRemoteParent(
                traceParentHeader,
                spanParentHeader,
                TraceFlags.getDefault(),
                TraceState.getDefault()
            )
        }

        return null
    }
}