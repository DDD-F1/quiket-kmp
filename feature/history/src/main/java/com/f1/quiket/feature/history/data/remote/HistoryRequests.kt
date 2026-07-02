package com.f1.quiket.feature.history.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class QuizResultSubmitRequest(
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: String,
    val parentPlaySessionId: String? = null,
    val elapsedMs: Int,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = true,
    val shuffleSeed: String? = null,
    val answers: List<QuizAnswerSubmitItemRequest>,
)

@Serializable
data class QuizAnswerSubmitItemRequest(
    val questionId: String,
    val selectedOptionId: String? = null,
    val selectedValue: String? = null,
    val correctClient: Boolean? = null,
    val skipped: Boolean,
    val answerElapsedMs: Int? = null,
    val marked: Boolean = false,
)

@Serializable
data class QuizRetryRequest(
    val clientSessionId: String,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = true,
    val shuffleSeed: String? = null,
)
