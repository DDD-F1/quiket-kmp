package com.f1.quiket.feature.floating.data.remote

import com.f1.quiket.core.network.auth.AuthenticatedRequest
import com.f1.quiket.core.network.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SubjectApi {
    @Headers(AuthenticatedRequest.HEADER)
    @GET("subjects")
    suspend fun getSubjects(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<ApiResponse<PageResponse<SubjectSummaryResponse>>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("subjects")
    suspend fun createSubject(
        @Body request: SubjectCreateRequest,
    ): Response<ApiResponse<SubjectResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("subjects/{subjectId}")
    suspend fun getSubject(
        @Path("subjectId") subjectId: String,
    ): Response<ApiResponse<SubjectDetailResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @DELETE("subjects/{subjectId}")
    suspend fun deleteSubject(
        @Path("subjectId") subjectId: String,
    ): Response<ApiResponse<Unit>>

    @Headers(AuthenticatedRequest.HEADER)
    @PATCH("subjects/{subjectId}/name")
    suspend fun updateSubjectName(
        @Path("subjectId") subjectId: String,
        @Body request: SubjectNameUpdateRequest,
    ): Response<ApiResponse<SubjectResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PUT("subjects/{subjectId}/details")
    suspend fun updateSubjectDetails(
        @Path("subjectId") subjectId: String,
        @Body request: SubjectCreateRequest,
    ): Response<ApiResponse<SubjectResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PUT("subjects/{subjectId}/exam-schedule")
    suspend fun upsertSubjectExamSchedule(
        @Path("subjectId") subjectId: String,
        @Body request: SubjectExamScheduleUpsertRequest,
    ): Response<ApiResponse<SubjectExamScheduleResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @DELETE("subjects/{subjectId}/exam-schedule")
    suspend fun deleteSubjectExamSchedule(
        @Path("subjectId") subjectId: String,
    ): Response<ApiResponse<Unit>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("certificates")
    suspend fun getCertificates(): Response<ApiResponse<List<CertificateResponse>>>

    @Headers(AuthenticatedRequest.HEADER)
    @DELETE("chapters/{chapterId}")
    suspend fun deleteChapter(
        @Path("chapterId") chapterId: String,
    ): Response<ApiResponse<Unit>>

    @Headers(AuthenticatedRequest.HEADER)
    @PATCH("chapters/{chapterId}/name")
    suspend fun updateChapterName(
        @Path("chapterId") chapterId: String,
        @Body request: ChapterNameUpdateRequest,
    ): Response<ApiResponse<ChapterResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("parts/{partId}")
    suspend fun getPart(
        @Path("partId") partId: String,
    ): Response<ApiResponse<PartResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PATCH("parts/{partId}")
    suspend fun updatePart(
        @Path("partId") partId: String,
        @Body request: PartUpdateRequest,
    ): Response<ApiResponse<PartResponse>>
}
