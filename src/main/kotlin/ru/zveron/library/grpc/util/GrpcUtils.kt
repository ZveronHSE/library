package ru.zveron.library.grpc.util

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.util.JsonFormat
import io.grpc.Status
import mu.KLogging
import ru.zveron.library.grpc.exception.PlatformException
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import kotlin.coroutines.CoroutineContext

object GrpcUtils : KLogging() {
    private val protoJsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace()

    fun <T : GeneratedMessageV3> T.toJson(): String = protoJsonPrinter.print(this)

    /**
     * Возвращает все метаданные от апигетвея
     * @param coroutineContext текущий короутиновый контекст
     * @param requiredAuthorized требуется ли авторизация пользователя
     */
    fun getMetadata(coroutineContext: CoroutineContext, requiredAuthorized: Boolean = false): Metadata {
        val metadata = coroutineContext[MetadataElement]?.metadata

        if (requiredAuthorized && metadata?.profileId == null) {
            throw PlatformException(Status.UNAUTHENTICATED, "user should be authorized for this endpoint")
        }

        return (metadata ?: Metadata()).also {
            logger.info { "Metadata: $it" }
        }
    }
}