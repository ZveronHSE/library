package ru.zveron.library.grpc.util

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.util.JsonFormat

object GrpcUtils {
    private val protoJsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace()

    fun <T : GeneratedMessageV3> T.toJson(): String = protoJsonPrinter.print(this)
}