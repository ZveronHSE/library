package ru.zveron.library

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlatformLibraryApplication

fun main(args: Array<String>) {
    runApplication<PlatformLibraryApplication>(*args)
}
