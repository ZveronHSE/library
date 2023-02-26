package ru.zveron.library.grpc.interceptor.model

import ru.zveron.library.grpc.model.Metadata
import kotlin.coroutines.CoroutineContext

class MetadataElement(val metadata: Metadata) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<MetadataElement>

    override val key: CoroutineContext.Key<*> = Key
}