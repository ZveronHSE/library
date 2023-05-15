package ru.zveron.library.grpc.interceptor.tracing

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import mu.KLogging

open class TracingClientInterceptor(
    private val tracer: Tracer
) : ClientInterceptor {
    companion object : KLogging()

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        channel: Channel
    ): ClientCall<ReqT, RespT> {
        var context = Context.current()

        val span = tracer.spanBuilder(method.fullMethodName)
            .setSpanKind(SpanKind.CLIENT)
            .setParent(context)
            .startSpan()

        context = context.with(span)

        return context.makeCurrent().use {
            object :
                ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, callOptions)) {


                override fun sendMessage(message: ReqT) {
                    super.sendMessage(message)

                }

                override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                    val spanContext = Span.fromContext(context).spanContext
                    headers.put(TracingHelper.traceIdKey, spanContext.traceId)
                    headers.put(TracingHelper.spanIdKey, spanContext.spanId)

                    val listener =
                        object :
                            ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                            override fun onClose(status: Status, trailers: io.grpc.Metadata) {
                                if (!status.isOk && status.cause != null) {
                                    val exception = status.asException()
                                    TracingHelper.setExceptionStatus(span, exception)
                                }

                                span.setAttribute(TracingHelper.RPC_CODE, status.code.name)
                                span.end()
                                super.onClose(status, trailers)
                            }
                        }

                    super.start(listener, headers)
                }
            }
        }
    }

}