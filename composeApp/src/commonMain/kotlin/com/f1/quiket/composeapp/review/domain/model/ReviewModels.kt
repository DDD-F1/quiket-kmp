package com.f1.quiket.composeapp.review.domain.model

internal class ReviewException(
    message: String,
    val isUnauthorized: Boolean = false,
) : Exception(message)

internal enum class ReviewFilter(
    val wireValue: String,
    val label: String,
) {
    All("all", "전체"),
    Wrong("wrong", "오답만"),
}

internal data class QuizReview(
    val playSessionId: String,
    val items: List<QuizReviewItem>,
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
    val sourcePart: ReviewPartSummary?,
)

internal data class QuestionOption(
    val id: String,
    val optionNumber: Int,
    val content: String,
)

internal data class ReviewPartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)
