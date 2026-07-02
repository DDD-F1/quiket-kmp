package com.f1.quiket.feature.review.data.mapper

import com.f1.quiket.feature.review.data.remote.PartSummaryResponse
import com.f1.quiket.feature.review.data.remote.QuestionOptionResponse
import com.f1.quiket.feature.review.data.remote.QuizReviewDataResponse
import com.f1.quiket.feature.review.data.remote.QuizReviewItemResponse
import com.f1.quiket.feature.review.domain.model.PartSummary
import com.f1.quiket.feature.review.domain.model.QuestionOption
import com.f1.quiket.feature.review.domain.model.QuizReview
import com.f1.quiket.feature.review.domain.model.QuizReviewItem

fun QuizReviewDataResponse.toDomain(): QuizReview = QuizReview(
    playSessionId = playSessionId,
    items = items.map { item -> item.toDomain() },
)

fun QuizReviewItemResponse.toDomain(): QuizReviewItem = QuizReviewItem(
    questionId = questionId,
    displayOrder = displayOrder,
    summary = summary,
    body = body,
    options = options.map { option -> option.toDomain() },
    selectedOptionId = selectedOptionId,
    selectedValue = selectedValue,
    answerValue = answerValue,
    correctServer = correctServer,
    skipped = skipped,
    correctExplanation = correctExplanation,
    incorrectExplanation = incorrectExplanation,
    sourcePart = sourcePart?.toDomain(),
)

fun QuestionOptionResponse.toDomain(): QuestionOption = QuestionOption(
    id = id,
    optionNumber = optionNumber,
    content = content,
)

fun PartSummaryResponse.toDomain(): PartSummary = PartSummary(
    id = id,
    chapterId = chapterId,
    name = name,
    partNumber = partNumber,
    contentPreview = contentPreview,
)
