package com.f1.quiket.composeapp.auth.domain.repository

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData

interface AuthRepository {
    suspend fun checkEmailAvailability(email: String): EmailAvailability
    suspend fun signup(email: String, password: String, passwordConfirm: String, nickname: String): SignupData
    suspend fun resendEmailVerification(email: String): EmailVerificationSent
    suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData
    suspend fun login(email: String, password: String): AuthTokenData
    suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult
    suspend fun appleLogin(identityToken: String, authorizationCode: String?, fullName: String?): AppleLoginResult
    suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData
    suspend fun linkKakaoAccount(linkToken: String, email: String, password: String): AuthTokenData
    suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData
    suspend fun linkAppleAccount(linkToken: String, email: String, password: String): AuthTokenData
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
