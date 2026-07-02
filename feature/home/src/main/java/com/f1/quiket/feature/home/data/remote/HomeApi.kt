package com.f1.quiket.feature.home.data.remote

import com.f1.quiket.core.network.auth.AuthenticatedRequest
import com.f1.quiket.core.network.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface HomeApi {
    @Headers(AuthenticatedRequest.HEADER)
    @GET("home")
    suspend fun getHome(): Response<ApiResponse<HomeDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("home/recent-activities")
    suspend fun getRecentActivities(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<ApiResponse<PageResponse<RecentActivityResponse>>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("subjects")
    suspend fun getSubjects(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<ApiResponse<PageResponse<SubjectSummaryResponse>>>
}
