package ru.zveron.library.grpc.interceptor.tracing

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
import mu.KLogging
import ru.zveron.library.grpc.interceptor.tracing.TracingHelper.onClose
import ru.zveron.library.grpc.interceptor.tracing.TracingHelper.setExceptionStatus

open class TracingServerInterceptor(
    private val tracer: Tracer,
) : ServerInterceptor {

    companion object : KLogging() {
        private val MESSAGE_TYPE_KEY = AttributeKey.stringKey("message.type")
        private const val MESSAGE_EVENT = "message"
    }

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val parentSpanContext = getParentContextFromHeaders(headers)
        var context = Context.current()

        if (parentSpanContext != null) {
            context = context.with(Span.wrap(parentSpanContext)).also {
                it.makeCurrent()
            }
        }

        val span = tracer
            .spanBuilder("${call.methodDescriptor.serviceName!!}/${call.methodDescriptor.bareMethodName!!}")
            .setSpanKind(SpanKind.SERVER)
            .setParent(context)
            .startSpan()

        return context.with(span).makeCurrent().use {

            object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                try {
                    next.startCall(
                        object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                            override fun sendMessage(message: RespT) {
                                context.makeCurrent().use {
                                    super.sendMessage(message)
                                }

                                span.addEvent(
                                    MESSAGE_EVENT, Attributes.of(
                                        MESSAGE_TYPE_KEY,
                                        "GET RESPONSE"
                                    )
                                )
                            }

                            override fun sendHeaders(headers: Metadata?) {
                                (headers ?: Metadata()).put(TracingHelper.traceIdKey, span.spanContext.traceId)
                                super.sendHeaders(headers)
                            }

                            override fun close(status: Status, trailers: Metadata) {
                                super.close(status, trailers)
                                span.onClose(status)
                            }

                        },
                        headers
                    )
                } catch (ex: Exception) {
                    // catched if get exception in interceptors
                    span.setExceptionStatus(ex)
                    throw ex
                }
            ) {
                override fun onMessage(message: ReqT) {
                    span.addEvent(
                        MESSAGE_EVENT, Attributes.of(
                            MESSAGE_TYPE_KEY,
                            "SEND REQUEST"
                        )
                    )

                    context.makeCurrent().use {
                        super.onMessage(message)
                    }
                }

                override fun onCancel() {
                    super.onCancel()
                    span.setAttribute(TracingHelper.RPC_CODE, SemanticAttributes.RpcConnectRpcErrorCodeValues.CANCELLED)
                    span.setStatus(StatusCode.ERROR)
                    span.end()
                }
            }
        }
    }

    private fun getParentContextFromHeaders(headers: Metadata): SpanContext? {
        val traceParentHeader = headers.get(TracingHelper.traceIdKey)
        val spanParentHeader = headers.get(TracingHelper.spanIdKey)

        if (traceParentHeader != null && spanParentHeader != null) {
            return SpanContext.createFromRemoteParent(
                traceParentHeader,
                spanParentHeader,
                TraceFlags.getSampled(),
                TraceState.getDefault()
            )
        }

        return null
    }
}