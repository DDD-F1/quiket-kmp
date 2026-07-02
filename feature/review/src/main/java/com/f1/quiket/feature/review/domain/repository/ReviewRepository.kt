package com.f1.quiket.feature.review.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.review.domain.model.QuizReview
import com.f1.quiket.feature.review.domain.model.QuizReviewFilter

interface ReviewRepository {
    suspend fun getQuizResultReview(
        resultId: String,
        filter: QuizReviewFilter = QuizReviewFilter.All,
    ): NetworkResult<QuizReview>
}
