import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `maven-publish`

    id("io.spring.dependency-management")

    kotlin("jvm")
    kotlin("plugin.spring")
}

val grpcVersion: String by project
val grpcKotlinVersion: String by project
val protobufVersion: String by project

val opentelemetryVersion: String = "1.25.0"
val opentelemetryApiVersion: String = "$opentelemetryVersion-alpha"

group = "ru.zveron"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly("org.springframework.boot:spring-boot-dependencies:2.7.4")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")


    // Grpc
    implementation("net.devh:grpc-spring-boot-starter:2.14.0.RELEASE")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    compileOnly("com.google.protobuf:protobuf-java-util:$protobufVersion")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")

    // Tracing
    api("io.opentelemetry:opentelemetry-api:$opentelemetryVersion")
    api("io.opentelemetry:opentelemetry-sdk:$opentelemetryVersion")
    api("io.opentelemetry:opentelemetry-context:$opentelemetryVersion")
    api("io.opentelemetry:opentelemetry-exporter-jaeger:$opentelemetryVersion")
    api("io.opentelemetry:opentelemetry-semconv:$opentelemetryApiVersion")

    // tracing database
    api("io.opentelemetry.instrumentation:opentelemetry-jdbc:$opentelemetryApiVersion")
    api("io.opentelemetry.instrumentation:opentelemetry-r2dbc-1.0:$opentelemetryApiVersion")
    // it needs for creating instance database pooling
    implementation("com.zaxxer:HikariCP:5.0.1")

    // tracing grpc
    api("io.opentelemetry.instrumentation:opentelemetry-grpc-1.6:$opentelemetryApiVersion")

    // add trace id to logback
    api("io.opentelemetry.instrumentation:opentelemetry-logback-1.0:1.9.2-alpha")
    api("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$opentelemetryApiVersion")

    // Compiling project
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()

            from(components["java"])
        }
    }
}