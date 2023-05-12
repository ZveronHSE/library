package ru.zveron.library.tracing

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "platform.tracing")
data class TracingProps(
    var grpc: Boolean?,
    var r2dbc: Boolean?,
    var jdbc: Boolean?,
)