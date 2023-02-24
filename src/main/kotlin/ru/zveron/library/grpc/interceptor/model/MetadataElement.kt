package ru.zveron.library.grpc.interceptor.model

import kotlin.coroutines.CoroutineContext

class MetadataElement(val profileId: Long?) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<MetadataElement>

    override val key: CoroutineContext.Key<*>
        get() = Key
}