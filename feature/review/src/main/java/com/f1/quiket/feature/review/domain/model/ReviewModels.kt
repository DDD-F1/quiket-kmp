package com.f1.quiket.feature.review.domain.model

data class QuizReview(
    val playSessionId: String,
    val items: List<QuizReviewItem>,
)

data class QuizReviewItem(
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
    val sourcePart: PartSummary?,
)

data class QuestionOption(
    val id: String,
    val optionNumber: Int,
    val content: String,
)

data class PartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)

enum class QuizReviewFilter(
    val wireValue: String,
) {
    All("all"),
    Wrong("wrong"),
}
