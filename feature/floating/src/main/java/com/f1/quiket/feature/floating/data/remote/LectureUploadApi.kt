package com.f1.quiket.feature.floating.data.remote

import com.f1.quiket.core.network.auth.AuthenticatedRequest
import com.f1.quiket.core.network.model.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface LectureUploadApi {
    @Headers(AuthenticatedRequest.HEADER)
    @POST("lecture-uploads")
    suspend fun createTextLectureUpload(
        @Body request: LectureTextUploadRequest,
    ): Response<ApiResponse<LectureUploadAcceptedDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @Multipart
    @POST("lecture-uploads")
    suspend fun createFileLectureUpload(
        @Part("subjectId") subjectId: RequestBody,
        @Part("chapterName") chapterName: RequestBody? = null,
        @Part("uploadType") uploadType: RequestBody,
        @Part("partSplitMethod") partSplitMethod: RequestBody,
        @Part("partSplitPlansJson") partSplitPlansJson: RequestBody? = null,
        @Part files: List<MultipartBody.Part>,
    ): Response<ApiResponse<LectureUploadAcceptedDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("chapters/{chapterId}/parts")
    suspend fun addTextPartToChapter(
        @Path("chapterId") chapterId: String,
        @Body request: PartTextAddRequest,
    ): Response<ApiResponse<LectureUploadAcceptedDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @Multipart
    @POST("chapters/{chapterId}/parts")
    suspend fun addFilePartToChapter(
        @Path("chapterId") chapterId: String,
        @Part("partName") partName: RequestBody,
        @Part("uploadType") uploadType: RequestBody,
        @Part files: List<MultipartBody.Part>,
    ): Response<ApiResponse<LectureUploadAcceptedDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("lecture-uploads/{lectureUploadId}/status")
    suspend fun getLectureUploadStatus(
        @Path("lectureUploadId") lectureUploadId: String,
    ): Response<ApiResponse<LectureUploadStatusDataResponse>>
}
