rootProject.name = "platform-library"

pluginManagement {
    val kotlinVersion: String by settings
    val springVersion: String by settings
    val springDependencyManagementPluginVersion = "1.0.14.RELEASE"

    plugins {
        id("org.springframework.boot") version springVersion
        id("io.spring.dependency-management") version springDependencyManagementPluginVersion

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
    }
}
