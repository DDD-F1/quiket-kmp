package com.f1.quiket.feature.review.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.review.data.mapper.toDomain
import com.f1.quiket.feature.review.data.remote.ReviewApi
import com.f1.quiket.feature.review.domain.model.QuizReview
import com.f1.quiket.feature.review.domain.model.QuizReviewFilter
import com.f1.quiket.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val api: ReviewApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : ReviewRepository {
    override suspend fun getQuizResultReview(
        resultId: String,
        filter: QuizReviewFilter,
    ): NetworkResult<QuizReview> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = {
                api.getQuizResultReview(
                    resultId = resultId,
                    filter = filter.wireValue,
                )
            },
            mapper = { response -> response.toDomain() },
        )
    }
}
