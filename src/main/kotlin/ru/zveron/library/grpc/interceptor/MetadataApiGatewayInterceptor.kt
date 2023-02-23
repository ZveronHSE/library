package ru.zveron.library.grpc.interceptor

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import ru.zveron.library.grpc.interceptor.model.ApiGatewayElement
import kotlin.coroutines.CoroutineContext

@GrpcGlobalServerInterceptor
@ConditionalOnProperty("platform.grpc.apigateway.metadata", havingValue = "true", matchIfMissing = true)
class MetadataApiGatewayInterceptor : CoroutineContextServerInterceptor() {
    companion object {
        private val profileIdKey = Metadata.Key.of("profile_id", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        val profileId = headers.get(profileIdKey)?.toLong()

        return ApiGatewayElement(profileId = profileId)
    }
}