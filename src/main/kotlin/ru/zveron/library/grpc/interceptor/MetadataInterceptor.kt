package ru.zveron.library.grpc.interceptor

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import kotlin.coroutines.CoroutineContext

class MetadataInterceptor : CoroutineContextServerInterceptor() {
    companion object {
        private val profileIdKey = Metadata.Key.of("profile_id", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: Metadata): CoroutineContext {
        val profileId = headers.get(profileIdKey)?.toLong()

        return MetadataElement(profileId = profileId)
    }
}