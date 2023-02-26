package ru.zveron.library

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan
@Configuration
@ConfigurationPropertiesScan
@EnableConfigurationProperties
class PlatformConfiguration