package ru.zveron.library.grpc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "platform.grpc")
data class GrpcProps(
    var server: GrpcServerProps = GrpcServerProps(),
    var client: GrpcClientProps = GrpcClientProps(),
    var apigateway: ApiGatewayProps = ApiGatewayProps(),
) {
    data class GrpcClientProps(
        var loggingEnabled: Boolean = false,
        var tracingEnabled: Boolean = false,
    )

    data class GrpcServerProps(
        var loggingEnabled: Boolean = false,
        var tracingEnabled: Boolean = false,
    )

    data class ApiGatewayProps(
        var metadata: Boolean = false
    )
}