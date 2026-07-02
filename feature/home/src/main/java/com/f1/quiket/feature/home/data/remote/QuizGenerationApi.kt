package com.f1.quiket.feature.home.data.remote

import com.f1.quiket.core.network.auth.AuthenticatedRequest
import com.f1.quiket.core.network.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizGenerationApi {
    @Headers(AuthenticatedRequest.HEADER)
    @GET("subjects/{subjectId}/quiz-scope")
    suspend fun getQuizScope(
        @Path("subjectId") subjectId: String,
    ): Response<ApiResponse<QuizScopeDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("quiz-sessions")
    suspend fun createQuizSession(
        @Body request: QuizCreateRequest,
    ): Response<ApiResponse<QuizGenerationAcceptedDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("quiz-sessions/{quizSessionId}/generation-status")
    suspend fun getQuizGenerationStatus(
        @Path("quizSessionId") quizSessionId: String,
    ): Response<ApiResponse<QuizGenerationStatusDataResponse>>
}
