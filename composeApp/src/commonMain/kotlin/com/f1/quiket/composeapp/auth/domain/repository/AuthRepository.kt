package com.f1.quiket.composeapp.auth.domain.repository

import com.f1.quiket.composeapp.auth.AuthTokenData
import com.f1.quiket.composeapp.auth.AuthUser
import com.f1.quiket.composeapp.auth.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.EmailAvailability
import com.f1.quiket.composeapp.auth.EmailVerificationSent
import com.f1.quiket.composeapp.auth.PasswordResetRequested
import com.f1.quiket.composeapp.auth.SignupData

internal interface AuthRepository {
    suspend fun checkEmailAvailability(email: String): EmailAvailability
    suspend fun signup(email: String, password: String, passwordConfirm: String, nickname: String): SignupData
    suspend fun resendEmailVerification(email: String): EmailVerificationSent
    suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData
    suspend fun login(email: String, password: String): AuthTokenData
    suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult
    suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData
    suspend fun linkKakaoAccount(linkToken: String, email: String, password: String): AuthTokenData
    suspend fun refreshToken(refreshToken: String): AuthTokenData
    suspend fun logout(session: SessionSnapshot)
    suspend fun getMe(session: SessionSnapshot): AuthUser
    suspend fun requestPasswordReset(email: String): PasswordResetRequested
    suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    )
}
