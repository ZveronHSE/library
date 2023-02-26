package ru.zveron.library.grpc.interceptor

import io.grpc.ServerCall
import io.grpc.kotlin.CoroutineContextServerInterceptor
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import kotlin.coroutines.CoroutineContext
import io.grpc.Metadata as MetadataGRPC

class MetadataInterceptor : CoroutineContextServerInterceptor() {
    companion object {
        private val profileIdKey = MetadataGRPC.Key.of("profile_id", MetadataGRPC.ASCII_STRING_MARSHALLER)
    }

    override fun coroutineContext(call: ServerCall<*, *>, headers: MetadataGRPC): CoroutineContext {
        val profileId = headers.get(profileIdKey)?.toLong()

        return MetadataElement(Metadata(profileId = profileId))
    }
}