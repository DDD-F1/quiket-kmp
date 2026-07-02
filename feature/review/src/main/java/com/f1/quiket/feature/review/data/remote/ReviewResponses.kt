package com.f1.quiket.feature.review.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class QuizReviewDataResponse(
    val playSessionId: String,
    val items: List<QuizReviewItemResponse> = emptyList(),
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
