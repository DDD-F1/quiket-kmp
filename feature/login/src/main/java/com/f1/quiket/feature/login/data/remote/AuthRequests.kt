package com.f1.quiket.feature.login.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String? = null,
)

@Serializable
data class EmailVerificationRequest(
    val email: String,
)

@Serializable
data class EmailVerificationConfirmRequest(
    val email: String,
    val verificationCode: String? = null,
    val verificationToken: String? = null,
)

@Serializable
data class PasswordResetRequest(
    val email: String,
)

@Serializable
data class PasswordResetConfirmRequest(
    val email: String,
    val resetToken: String? = null,
    val verificationCode: String? = null,
    val newPassword: String,
    val newPasswordConfirm: String,
)

@Serializable
data class KakaoLoginRequest(
    val kakaoAccessToken: String,
    val agreedToTerms: Boolean = true,
)

@Serializable
data class KakaoAccountLinkRequest(
    val linkToken: String,
    val email: String,
    val password: String,
    val agreedToLink: Boolean,
)

@Serializable
data class KakaoNicknameRequest(
    val signupToken: String,
    val nickname: String,
)
