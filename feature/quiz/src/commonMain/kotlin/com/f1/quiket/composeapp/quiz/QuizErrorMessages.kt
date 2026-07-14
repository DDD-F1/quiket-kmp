package com.f1.quiket.composeapp.quiz

import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.network.withNetworkRetryGuide

internal fun Throwable.toQuizNetworkAwareMessage(
    fallback: String,
    timeoutMessage: String = fallback,
): String = toUserFacingMessage(
    fallback = fallback,
    networkMessage = timeoutMessage,
)

internal fun String.withQuizNetworkRetryGuide(): String =
    withNetworkRetryGuide()
