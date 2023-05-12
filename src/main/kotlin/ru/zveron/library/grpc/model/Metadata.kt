package ru.zveron.library.grpc.model

data class Metadata(
    val profileId: Long? = null
) {
    override fun toString(): String {
        return "Resolved user. ProfileId=$profileId"
    }
}