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

group = "ru.zveron"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly("org.springframework.boot:spring-boot-dependencies")
    // Grpc
    implementation("net.devh:grpc-spring-boot-starter:2.14.0.RELEASE")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    compileOnly("com.google.protobuf:protobuf-java-util:$protobufVersion")

    // Логгирование
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")

    // Для компиляции проекта
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