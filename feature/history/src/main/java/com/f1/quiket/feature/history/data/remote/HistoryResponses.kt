package com.f1.quiket.feature.history.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RecentActivityPageResponse(
    val content: List<RecentActivityResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
)

@Serializable
data class RecentActivityResponse(
    val activityId: String,
    val activityType: String,
    val quizSessionId: String? = null,
    val playSessionId: String? = null,
    val resultId: String? = null,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val status: String? = null,
    val progressPct: Int? = null,
    val scoreText: String? = null,
    val createdAt: String,
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
    val sourcePart: PartSummaryResponse? = null,
)

@Serializable
data class QuestionOptionResponse(
    val id: String,
    val optionNumber: Int,
    val content: String,
)

@Serializable
data class PartSummaryResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
)

@Serializable
data class QuizPlaySessionDataResponse(
    val playSessionId: String,
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: String,
    val status: String,
    val quizSession: RetryQuizSessionResponse? = null,
)

@Serializable
data class RetryQuizSessionResponse(
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
    val questions: List<RetryQuestionResponse> = emptyList(),
)

@Serializable
data class RetryQuestionResponse(
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
    val answer: RetryQuestionAnswerResponse,
)

@Serializable
data class RetryQuestionAnswerResponse(
    val answerValue: String,
)
