package com.f1.quiket.feature.login.data.remote

import com.f1.quiket.core.network.model.ApiResponse
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest,
    ): Response<ApiResponse<SignupDataResponse>>

    @GET("auth/emails/availability")
    suspend fun checkEmailAvailability(
        @Query("email") email: String,
    ): Response<ApiResponse<EmailAvailabilityDataResponse>>

    @POST("auth/email-verifications")
    suspend fun resendEmailVerification(
        @Body request: EmailVerificationRequest,
    ): Response<ApiResponse<EmailVerificationSentDataResponse>>

    @POST("auth/email-verifications/confirm")
    suspend fun confirmEmailVerification(
        @Header("X-Device-Id") deviceId: String?,
        @Header("X-Device-Name") deviceName: String?,
        @Body request: EmailVerificationConfirmRequest,
    ): Response<ApiResponse<AuthTokenDataResponse>>

    @POST("auth/login")
    suspend fun login(
        @Header("X-Device-Id") deviceId: String?,
        @Header("X-Device-Name") deviceName: String?,
        @Body request: LoginRequest,
    ): Response<ApiResponse<AuthTokenDataResponse>>

    @POST("auth/token/refresh")
    suspend fun refreshToken(
        @Header("X-Device-Id") deviceId: String?,
        @Header("X-Device-Name") deviceName: String?,
        @Body request: RefreshTokenRequest,
    ): Response<ApiResponse<AuthTokenDataResponse>>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String?,
        @Body request: LogoutRequest,
    ): Response<ApiResponse<Unit>>

    @POST("auth/password-reset/requests")
    suspend fun requestPasswordReset(
        @Body request: PasswordResetRequest,
    ): Response<ApiResponse<PasswordResetRequestedDataResponse>>

    @POST("auth/password-reset/confirm")
    suspend fun confirmPasswordReset(
        @Body request: PasswordResetConfirmRequest,
    ): Response<ApiResponse<Unit>>

    @POST("auth/oauth/kakao/login")
    suspend fun kakaoLogin(
        @Header("X-Device-Id") deviceId: String?,
        @Header("X-Device-Name") deviceName: String?,
        @Body request: KakaoLoginRequest,
    ): Response<ApiResponse<JsonElement>>

    @POST("auth/oauth/kakao/link")
    suspend fun linkKakaoAccount(
        @Header("X-Device-Id") deviceId: String?,
        @Header("X-Device-Name") deviceName: String?,
        @Body request: KakaoAccountLinkRequest,
    ): Response<ApiResponse<AuthTokenDataResponse>>

    @POST("auth/oauth/kakao/nickname")
    suspend fun completeKakaoNickname(
        @Header("X-Device-Id") deviceId: String?,
        @Header("X-Device-Name") deviceName: String?,
        @Body request: KakaoNicknameRequest,
    ): Response<ApiResponse<AuthTokenDataResponse>>

    @GET("auth/me")
    suspend fun getMe(
        @Header("Authorization") authorization: String?,
    ): Response<ApiResponse<AuthUserResponse>>
}
