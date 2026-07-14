package com.f1.quiket.composeapp.network

fun Throwable.toUserFacingMessage(
    fallback: String,
    networkMessage: String = fallback.withNetworkRetryGuide(),
): String {
    val normalized = message.orEmpty().trim()
    if (normalized.isBlank()) return fallback

    return if (hasNetworkSignal()) {
        networkMessage
    } else {
        normalized
    }
}

fun String.withNetworkRetryGuide(): String =
    "$this 네트워크 상태를 확인한 뒤 다시 시도해주세요."

private fun Throwable.hasNetworkSignal(): Boolean =
    generateSequence(this) { it.cause }
        .any { throwable ->
            throwable::class.simpleName.orEmpty().isNetworkExceptionName() ||
                throwable.message.orEmpty().isLowLevelNetworkMessage()
        }

private fun String.isNetworkExceptionName(): Boolean {
    val normalized = lowercase()
    return NetworkExceptionNameSignals.any { signal -> normalized.contains(signal) }
}

private fun String.isLowLevelNetworkMessage(): Boolean {
    val normalized = lowercase()
    return LowLevelNetworkSignals.any { signal -> normalized.contains(signal) }
}

private val NetworkExceptionNameSignals = listOf(
    "timeout",
    "socket",
    "network",
    "connect",
    "unresolved",
    "unknownhost",
    "ioexception",
)

private val LowLevelNetworkSignals = listOf(
    "timeout",
    "timed out",
    "socket",
    "connection refused",
    "failed to connect",
    "network is unreachable",
    "no route to host",
    "unable to resolve",
    "unresolved address",
    "unknown host",
    "could not connect",
    "internet",
)
