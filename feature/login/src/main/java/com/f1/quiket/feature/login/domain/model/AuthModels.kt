package com.f1.quiket.feature.login.domain.model

import com.f1.quiket.core.network.auth.TokenPair

data class AuthTokenData(
    val tokenPair: TokenPair,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val email: String,
    val nickname: String,
    val dotoriBalance: Int,
    val emailVerified: Boolean,
    val status: UserStatus,
    val providers: List<AuthProvider>,
)

data class SignupData(
    val userId: String,
    val email: String,
    val nickname: String,
    val emailVerificationRequired: Boolean,
    val emailVerificationSent: Boolean,
)

data class EmailAvailability(
    val email: String,
    val available: Boolean,
)

data class EmailVerificationSent(
    val email: String,
    val expiresInSeconds: Long,
)

data class PasswordResetRequested(
    val email: String,
    val expiresInSeconds: Long,
)

data class KakaoAccountLinkRequired(
    val email: String,
    val provider: AuthProvider,
    val linkToken: String,
    val expiresInSeconds: Long,
)

data class KakaoNicknameRequired(
    val signupToken: String,
    val provider: AuthProvider,
    val suggestedNickname: String?,
)

enum class UserStatus {
    Active,
    Locked,
    Inactive,
    Unknown,
}

enum class AuthProvider {
    Local,
    Kakao,
    Apple,
    Unknown,
}
