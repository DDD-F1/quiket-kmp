package com.f1.quiket.feature.review.data.remote

import com.f1.quiket.core.network.auth.AuthenticatedRequest
import com.f1.quiket.core.network.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {
    @Headers(AuthenticatedRequest.HEADER)
    @GET("quiz-results/{resultId}/review")
    suspend fun getQuizResultReview(
        @Path("resultId") resultId: String,
        @Query("filter") filter: String = "all",
    ): Response<ApiResponse<QuizReviewDataResponse>>
}
