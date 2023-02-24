package ru.zveron.library.grpc.interceptor

import com.google.protobuf.GeneratedMessageV3
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.MethodDescriptor
import mu.KLogging
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers
import ru.zveron.library.grpc.interceptor.model.LogstashKey
import ru.zveron.library.grpc.interceptor.model.MethodType
import ru.zveron.library.grpc.util.GrpcUtils.toJson

open class LoggingClientInterceptor : ClientInterceptor {
    companion object : KLogging() {
        const val TYPE_CALL = "client"
    }

    final override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        channel: Channel
    ): ClientCall<ReqT, RespT> {
        return object :
            ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, callOptions)) {

            override fun sendMessage(message: ReqT) {
                logMessage(MethodType.REQUEST, method, message)
                super.sendMessage(message)

            }

            override fun start(responseListener: Listener<RespT>?, headers: io.grpc.Metadata?) {
                val listener =
                    object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onMessage(message: RespT) {
                            logMessage(MethodType.RESPONSE, method, message)
                            super.onMessage(message)
                        }
                    }

                super.start(listener, headers)
            }
        }
    }

    open fun <ReqT, RespT> logMessage(
        methodType: MethodType,
        method: MethodDescriptor<ReqT, RespT>,
        message: Any
    ) {
        val body = (message as GeneratedMessageV3)

        val markers = mutableListOf<LogstashMarker>(
            Markers.append(LogstashKey.ENDPOINT_DIRECTION_KEY, methodType),
            Markers.append(LogstashKey.ENDPOINT_KEY, method.bareMethodName),
            Markers.append(LogstashKey.TYPE_CALL_KEY, TYPE_CALL),
            Markers.appendRaw(LogstashKey.BODY_KEY, body.toJson())
        )

        if (methodType == MethodType.REQUEST) {
            logger.info(Markers.aggregate(markers)) { "$TYPE_CALL: ${method.serviceName}" }
        } else {
            logger.debug(Markers.aggregate(markers)) { "$TYPE_CALL: ${method.serviceName}" }
        }
    }
}