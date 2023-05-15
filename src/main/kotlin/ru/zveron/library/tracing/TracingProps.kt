package ru.zveron.library.tracing

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "platform.tracing")
data class TracingProps(
    var jdbc: Boolean?,
)