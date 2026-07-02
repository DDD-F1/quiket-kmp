package com.f1.quiket.feature.login.domain.repository

import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.model.AuthTokenData
import com.f1.quiket.feature.login.domain.model.AuthUser
import com.f1.quiket.feature.login.domain.model.EmailAvailability
import com.f1.quiket.feature.login.domain.model.EmailVerificationSent
import com.f1.quiket.feature.login.domain.model.KakaoLoginResult
import com.f1.quiket.feature.login.domain.model.PasswordResetRequested
import com.f1.quiket.feature.login.domain.model.SignupData

interface AuthRepository {
    suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): AuthResult<SignupData>

    suspend fun checkEmailAvailability(email: String): AuthResult<EmailAvailability>

    suspend fun resendEmailVerification(email: String): AuthResult<EmailVerificationSent>

    suspend fun confirmEmailVerification(
        email: String,
        verificationCode: String? = null,
        verificationToken: String? = null,
        deviceId: String? = null,
        deviceName: String? = null,
    ): AuthResult<AuthTokenData>

    suspend fun login(
        email: String,
        password: String,
        deviceId: String? = null,
        deviceName: String? = null,
    ): AuthResult<AuthTokenData>

    suspend fun refreshToken(
        refreshToken: String,
        deviceId: String? = null,
        deviceName: String? = null,
    ): AuthResult<AuthTokenData>

    suspend fun logout(refreshToken: String? = null): AuthResult<Unit>

    suspend fun requestPasswordReset(email: String): AuthResult<PasswordResetRequested>

    suspend fun confirmPasswordReset(
        email: String,
        newPassword: String,
        newPasswordConfirm: String,
        resetToken: String? = null,
        verificationCode: String? = null,
    ): AuthResult<Unit>

    suspend fun kakaoLogin(
        kakaoAccessToken: String,
        agreedToTerms: Boolean = true,
        deviceId: String? = null,
        deviceName: String? = null,
    ): KakaoLoginResult

    suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
        agreedToLink: Boolean,
        deviceId: String? = null,
        deviceName: String? = null,
    ): AuthResult<AuthTokenData>

    suspend fun completeKakaoNickname(
        signupToken: String,
        nickname: String,
        deviceId: String? = null,
        deviceName: String? = null,
    ): AuthResult<AuthTokenData>

    suspend fun getMe(): AuthResult<AuthUser>
}
