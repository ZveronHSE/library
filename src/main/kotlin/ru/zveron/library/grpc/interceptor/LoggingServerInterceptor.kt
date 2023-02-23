package ru.zveron.library.grpc.interceptor

import com.google.protobuf.GeneratedMessageV3
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import mu.KLogging
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import ru.zveron.library.grpc.interceptor.model.LogstashKey
import ru.zveron.library.grpc.interceptor.model.MethodType
import ru.zveron.library.grpc.util.GrpcUtils.toJson


@GrpcGlobalServerInterceptor
@ConditionalOnProperty("platform.grpc.server.logging", havingValue = "true", matchIfMissing = true)
class LoggingServerInterceptor : ServerInterceptor {
    companion object : KLogging() {
        const val TYPE_CALL = "server"
    }

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val wrappedCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun sendMessage(message: RespT) {
                logMessage(MethodType.RESPONSE, call, message)
                super.sendMessage(message)
            }
        }

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(wrappedCall, headers)) {
            override fun onMessage(message: ReqT) {
                logMessage(MethodType.REQUEST, call, message)
                super.onMessage(message)
            }
        }
    }


    fun <ReqT : Any?, RespT : Any?> logMessage(
        methodType: MethodType,
        call: ServerCall<ReqT, RespT>,
        message: Any
    ) {
        val body = (message as GeneratedMessageV3)

        val markers = mutableListOf<LogstashMarker>(
            Markers.append(LogstashKey.ENDPOINT_DIRECTION_KEY, methodType),
            Markers.append(LogstashKey.ENDPOINT_KEY, call.methodDescriptor.bareMethodName),
            Markers.append(LogstashKey.TYPE_CALL_KEY, TYPE_CALL),
            Markers.appendRaw(LogstashKey.BODY_KEY, body.toJson())
        )

        if (methodType == MethodType.REQUEST) {
            logger.info(Markers.aggregate(markers)) { "$TYPE_CALL: ${call.methodDescriptor.serviceName}" }
        } else if (logger.isDebugEnabled) {
            logger.debug(Markers.aggregate(markers)) { "$TYPE_CALL: ${call.methodDescriptor.serviceName}" }
        }
    }
}