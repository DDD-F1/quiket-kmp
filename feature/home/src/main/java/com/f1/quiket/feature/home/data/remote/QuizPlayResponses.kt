package com.f1.quiket.feature.home.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class QuizSessionDataResponse(
    val id: String,
    val subjectId: String,
    val subjectName: String? = null,
    val quizType: String,
    val choiceCount: Int? = null,
    val questionCount: Int,
    val playMode: String,
    val timerEnabled: Boolean = false,
    val timerScope: String? = null,
    val timerSeconds: Int? = null,
    val difficulty: String,
    val status: String,
    val questions: List<QuestionResponse> = emptyList(),
)

@Serializable
data class QuestionResponse(
    val id: String,
    val subjectId: String? = null,
    val chapterId: String? = null,
    val partId: String? = null,
    val partName: String? = null,
    val questionType: String,
    val difficulty: String,
    val summary: String? = null,
    val body: String,
    val correctExplanation: String? = null,
    val incorrectExplanation: String? = null,
    val displayOrder: Int,
    val options: List<QuestionOptionResponse> = emptyList(),
    val answer: QuestionAnswerResponse,
)

@Serializable
data class QuestionOptionResponse(
    val id: String,
    val optionNumber: Int,
    val content: String,
)

@Serializable
data class QuestionAnswerResponse(
    val answerValue: String,
)

@Serializable
data class QuizPlayStartRequest(
    val clientSessionId: String,
    val playType: String,
    val parentPlaySessionId: String? = null,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = true,
    val shuffleSeed: String? = null,
)

@Serializable
data class QuizRetryRequest(
    val clientSessionId: String,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = false,
    val shuffleSeed: String? = null,
)

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
data class QuizPlaySessionDataResponse(
    val playSessionId: String,
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: String,
    val status: String,
    val quizSession: QuizSessionDataResponse? = null,
)

@Serializable
data class QuizResultDataResponse(
    val playSessionId: String,
    val resultId: String? = null,
    val quizSessionId: String,
    val subjectId: String,
    val subjectName: String? = null,
    val totalCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skipCount: Int,
    val accuracyPct: Int,
    val elapsedMs: Int,
    val scoreMatched: Boolean? = null,
    val abuseFlagged: Boolean? = null,
    val rewards: RewardSummaryResponse,
    val reviewItems: List<QuizReviewItemResponse> = emptyList(),
    val retryAvailable: RetryAvailableResponse? = null,
    val createdAt: String? = null,
)

@Serializable
data class RewardSummaryResponse(
    val dotoriEarned: Int,
    val xpEarned: Int,
    val leveledUp: Boolean,
    val newLevel: Int? = null,
    val currentDotoriBalance: Int? = null,
    val currentXpTotal: Int? = null,
)

@Serializable
data class RetryAvailableResponse(
    val retryAll: Boolean = false,
    val retryWrong: Boolean = false,
    val wrongCount: Int? = null,
)

@Serializable
data class QuizReviewItemResponse(
    val questionId: String,
    val displayOrder: Int,
    val summary: String? = null,
    val body: String,
    val options: List<QuestionOptionResponse> = emptyList(),
    val selectedOptionId: String? = null,
    val selectedValue: String? = null,
    val answerValue: String? = null,
    val correctServer: Boolean,
    val skipped: Boolean = false,
    val correctExplanation: String? = null,
    val incorrectExplanation: String? = null,
    val sourcePart: ResultPartSummaryResponse? = null,
)

@Serializable
data class ResultPartSummaryResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
)
