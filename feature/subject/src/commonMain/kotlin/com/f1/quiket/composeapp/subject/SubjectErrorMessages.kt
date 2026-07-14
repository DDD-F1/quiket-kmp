package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.network.withNetworkRetryGuide

internal fun Throwable.toSubjectNetworkAwareMessage(
    fallback: String,
    timeoutMessage: String = fallback,
): String = toUserFacingMessage(
    fallback = fallback,
    networkMessage = timeoutMessage,
)

internal fun String.withSubjectNetworkRetryGuide(): String =
    withNetworkRetryGuide()
