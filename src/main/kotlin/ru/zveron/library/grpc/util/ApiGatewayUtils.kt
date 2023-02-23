package ru.zveron.library.grpc.util

import io.grpc.Status
import ru.zveron.library.grpc.entity.ApiGatewayEntity
import ru.zveron.library.grpc.exception.PlatformException
import ru.zveron.library.grpc.interceptor.model.ApiGatewayElement
import kotlin.coroutines.CoroutineContext

class ApiGatewayUtils {
    fun getApiGatewayEntity(coroutineContext: CoroutineContext, requiredAuthorized: Boolean = false): ApiGatewayEntity {
        val apiGatewayElement = coroutineContext[ApiGatewayElement]
        val apiGatewayEntity = ApiGatewayEntity(
            profileId =  apiGatewayElement?.profileId
        )

        if (requiredAuthorized && apiGatewayEntity.profileId == null) {
            throw PlatformException(Status.UNAUTHENTICATED, "user should be authorized for this endpoint")
        }

        return apiGatewayEntity
    }
}