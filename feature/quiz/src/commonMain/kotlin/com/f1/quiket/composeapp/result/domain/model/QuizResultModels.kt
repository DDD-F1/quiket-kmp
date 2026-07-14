package com.f1.quiket.composeapp.result.domain.model

internal class QuizResultException(
    message: String,
    val isUnauthorized: Boolean = false,
) : Exception(message)

internal data class QuizResult(
    val playSessionId: String,
    val resultId: String?,
    val quizSessionId: String,
    val subjectId: String,
    val subjectName: String?,
    val totalCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skipCount: Int,
    val accuracyPct: Int,
    val elapsedMs: Int,
    val rewards: RewardSummary,
    val reviewItems: List<QuizReviewItem>,
    val retryAvailable: RetryAvailable?,
    val createdAt: String?,
)

internal data class RewardSummary(
    val dotoriEarned: Int,
    val xpEarned: Int,
    val leveledUp: Boolean,
    val newLevel: Int?,
    val currentDotoriBalance: Int?,
    val currentXpTotal: Int?,
)

internal data class RetryAvailable(
    val retryAll: Boolean,
    val retryWrong: Boolean,
    val wrongCount: Int?,
)

internal data class QuizReviewItem(
    val questionId: String,
    val displayOrder: Int,
    val summary: String?,
    val body: String,
    val options: List<QuestionOption>,
    val selectedOptionId: String?,
    val selectedValue: String?,
    val answerValue: String?,
    val correctServer: Boolean,
    val skipped: Boolean,
    val correctExplanation: String?,
    val incorrectExplanation: String?,
    val sourcePart: ResultPartSummary?,
)

internal data class QuestionOption(
    val id: String,
    val optionNumber: Int,
    val content: String,
)

internal data class ResultPartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)
