package ru.zveron.library.grpc.interceptor.model

import kotlin.coroutines.CoroutineContext

class ApiGatewayElement(val profileId: Long?) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<ApiGatewayElement>

    override val key: CoroutineContext.Key<*>
        get() = Key
}