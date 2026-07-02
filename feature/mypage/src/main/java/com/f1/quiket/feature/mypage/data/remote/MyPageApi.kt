package com.f1.quiket.feature.mypage.data.remote

import com.f1.quiket.core.network.auth.AuthenticatedRequest
import com.f1.quiket.core.network.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface MyPageApi {
    @Headers(AuthenticatedRequest.HEADER)
    @GET("gamification/me")
    suspend fun getMyGamification(): Response<ApiResponse<GamificationDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("my/profile")
    suspend fun getMyProfile(): Response<ApiResponse<MyProfileDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PATCH("my/profile/nickname")
    suspend fun updateMyNickname(
        @Body request: NicknameUpdateRequest,
    ): Response<ApiResponse<MyProfileDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("my/email/change-requests")
    suspend fun requestMyEmailChange(
        @Body request: MyEmailChangeRequest,
    ): Response<ApiResponse<EmailVerificationSentDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("my/email/change-confirm")
    suspend fun confirmMyEmailChange(
        @Body request: MyEmailChangeConfirmRequest,
    ): Response<ApiResponse<MyProfileDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PATCH("my/password")
    suspend fun updateMyPassword(
        @Body request: PasswordChangeRequest,
    ): Response<ApiResponse<Unit>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("my/account/deletion")
    suspend fun deleteMyAccount(
        @Body request: AccountDeleteRequest,
    ): Response<ApiResponse<Unit>>

    @Headers(AuthenticatedRequest.HEADER)
    @GET("my/notifications")
    suspend fun getNotificationSettings(): Response<ApiResponse<NotificationSettingsDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PUT("my/notifications")
    suspend fun updateNotificationSettings(
        @Body request: NotificationSettingsUpdateRequest,
    ): Response<ApiResponse<NotificationSettingsDataResponse>>

    @Headers(AuthenticatedRequest.HEADER)
    @PUT("my/fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenUpdateRequest,
    ): Response<ApiResponse<Unit>>

    @Headers(AuthenticatedRequest.HEADER)
    @POST("feedbacks")
    suspend fun createFeedback(
        @Body request: FeedbackCreateRequest,
    ): Response<ApiResponse<FeedbackDataResponse>>
}
