package ru.zveron.library.grpc.interceptor.logging

import com.google.protobuf.GeneratedMessageV3
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.opentelemetry.api.trace.Span
import mu.KLogging
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers
import org.slf4j.MDC
import ru.zveron.library.grpc.interceptor.model.LogstashKey
import ru.zveron.library.grpc.interceptor.model.MethodType
import ru.zveron.library.grpc.interceptor.tracing.TracingHelper
import ru.zveron.library.grpc.util.GrpcUtils.toJson


open class LoggingServerInterceptor : ServerInterceptor {
    companion object : KLogging() {
        const val TYPE_CALL = "server"
    }

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        // Костыль но мне похуй я ебала думать что тут пошло не так и почему нужно ВОТ так делать.
        MDC.put(TracingHelper.traceIdKey.name(), Span.current().spanContext.traceId)

        val wrappedCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                logMessage(MethodType.RESPONSE, call, message)
                super.sendMessage(message)
            }
        }

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
            next.startCall(
                wrappedCall,
                headers
            )
        ) {
            override fun onMessage(message: ReqT) {
                logMessage(MethodType.REQUEST, call, message)
                super.onMessage(message)
            }
        }
    }


    open fun <ReqT : Any?, RespT : Any?> logMessage(
        methodType: MethodType,
        call: ServerCall<ReqT, RespT>,
        message: Any
    ) {
        // Чтобы при рефлексии от апигетвея или постмана не было стремных информаций, нам такое сейчас не нужно
        if (call.methodDescriptor.bareMethodName == "ServerReflectionInfo") {
            return
        }

        val body = (message as GeneratedMessageV3)

        val markers = mutableListOf<LogstashMarker>(
            Markers.append(LogstashKey.ENDPOINT_DIRECTION_KEY, methodType),
            Markers.append(LogstashKey.ENDPOINT_KEY, call.methodDescriptor.bareMethodName),
            Markers.append(LogstashKey.TYPE_CALL_KEY, TYPE_CALL),
            Markers.append(LogstashKey.SERVICE_NAME_KEY, call.methodDescriptor.serviceName),
            Markers.appendRaw(LogstashKey.BODY_KEY, body.toJson())
        )

        if (methodType == MethodType.REQUEST) {
            logger.info(Markers.aggregate(markers)) { "request" }
        } else {
            logger.debug(Markers.aggregate(markers)) { "response" }
        }
    }
}