package ru.zveron.library.grpc.exception

import io.grpc.Status

class PlatformException(val status: Status, message: String) : RuntimeException(message)