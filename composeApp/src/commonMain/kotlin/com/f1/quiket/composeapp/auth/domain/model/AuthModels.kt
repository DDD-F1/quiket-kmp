package com.f1.quiket.composeapp.auth.domain.model

import kotlinx.serialization.Serializable

internal class AuthException(
    message: String,
    val isUnauthorized: Boolean = false,
    val code: String? = null,
    val email: String? = null,
    val failedLoginCount: Int? = null,
    val resetCodeSent: Boolean? = null,
) : Exception(message)

internal data class AuthTokenData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val user: AuthUser? = null,
)

internal data class AuthUser(
    val id: String,
    val email: String?,
    val nickname: String,
)

internal sealed interface KakaoLoginResult {
    data class LoggedIn(
        val tokenData: AuthTokenData,
    ) : KakaoLoginResult

    data class NicknameRequired(
        val data: KakaoNicknameRequired,
    ) : KakaoLoginResult

    data class AccountLinkRequired(
        val data: KakaoAccountLinkRequired,
    ) : KakaoLoginResult

    data class Failure(
        val message: String,
    ) : KakaoLoginResult
}

internal sealed interface AppleLoginResult {
    data class LoggedIn(
        val tokenData: AuthTokenData,
    ) : AppleLoginResult

    data class NicknameRequired(
        val data: AppleNicknameRequired,
    ) : AppleLoginResult

    data class AccountLinkRequired(
        val data: AppleAccountLinkRequired,
    ) : AppleLoginResult

    data class Failure(
        val message: String,
    ) : AppleLoginResult
}

@Serializable
internal data class KakaoAccountLinkRequired(
    val email: String,
    val provider: String,
    val linkToken: String,
    val expiresInSeconds: Long,
)

@Serializable
internal data class KakaoNicknameRequired(
    val signupToken: String,
    val provider: String,
    val suggestedNickname: String? = null,
)

@Serializable
internal data class AppleAccountLinkRequired(
    val email: String,
    val provider: String,
    val linkToken: String,
    val expiresInSeconds: Long,
)

@Serializable
internal data class AppleNicknameRequired(
    val signupToken: String,
    val provider: String,
    val suggestedNickname: String? = null,
)

@Serializable
internal data class EmailAvailability(
    val email: String,
    val available: Boolean,
)

@Serializable
internal data class SignupData(
    val userId: String,
    val email: String,
    val nickname: String,
    val emailVerificationRequired: Boolean,
    val emailVerificationSent: Boolean,
)

@Serializable
internal data class EmailVerificationSent(
    val email: String,
    val expiresInSeconds: Long,
)

@Serializable
internal data class PasswordResetRequested(
    val email: String,
    val expiresInSeconds: Long,
)
