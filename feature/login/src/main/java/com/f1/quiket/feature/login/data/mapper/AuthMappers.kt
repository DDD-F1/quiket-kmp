package com.f1.quiket.feature.login.data.mapper

import com.f1.quiket.core.network.auth.TokenPair
import com.f1.quiket.feature.login.data.remote.AuthTokenDataResponse
import com.f1.quiket.feature.login.data.remote.AuthUserResponse
import com.f1.quiket.feature.login.data.remote.EmailAvailabilityDataResponse
import com.f1.quiket.feature.login.data.remote.EmailVerificationSentDataResponse
import com.f1.quiket.feature.login.data.remote.FieldErrorResponse
import com.f1.quiket.feature.login.data.remote.KakaoAccountLinkRequiredDataResponse
import com.f1.quiket.feature.login.data.remote.KakaoNicknameRequiredDataResponse
import com.f1.quiket.feature.login.data.remote.PasswordResetRequestedDataResponse
import com.f1.quiket.feature.login.data.remote.SignupDataResponse
import com.f1.quiket.feature.login.domain.model.AuthProvider
import com.f1.quiket.feature.login.domain.model.AuthTokenData
import com.f1.quiket.feature.login.domain.model.AuthUser
import com.f1.quiket.feature.login.domain.model.EmailAvailability
import com.f1.quiket.feature.login.domain.model.EmailVerificationSent
import com.f1.quiket.feature.login.domain.model.FieldError
import com.f1.quiket.feature.login.domain.model.KakaoAccountLinkRequired
import com.f1.quiket.feature.login.domain.model.KakaoNicknameRequired
import com.f1.quiket.feature.login.domain.model.PasswordResetRequested
import com.f1.quiket.feature.login.domain.model.SignupData
import com.f1.quiket.feature.login.domain.model.UserStatus

internal fun AuthTokenDataResponse.toDomain(): AuthTokenData = AuthTokenData(
    tokenPair = toTokenPair(),
    user = user.toDomain(),
)

internal fun AuthTokenDataResponse.toTokenPair(): TokenPair = TokenPair(
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType,
    accessTokenExpiresIn = accessTokenExpiresIn,
    refreshTokenExpiresIn = refreshTokenExpiresIn,
)

internal fun AuthUserResponse.toDomain(): AuthUser = AuthUser(
    id = id,
    email = email.orEmpty(),
    nickname = nickname,
    dotoriBalance = dotoriBalance,
    emailVerified = emailVerified,
    status = status.toUserStatus(),
    providers = providers.map { provider -> provider.toAuthProvider() },
)

internal fun SignupDataResponse.toDomain(): SignupData = SignupData(
    userId = userId,
    email = email,
    nickname = nickname,
    emailVerificationRequired = emailVerificationRequired,
    emailVerificationSent = emailVerificationSent,
)

internal fun EmailAvailabilityDataResponse.toDomain(): EmailAvailability = EmailAvailability(
    email = email,
    available = available,
)

internal fun EmailVerificationSentDataResponse.toDomain(): EmailVerificationSent = EmailVerificationSent(
    email = email,
    expiresInSeconds = expiresInSeconds,
)

internal fun PasswordResetRequestedDataResponse.toDomain(): PasswordResetRequested = PasswordResetRequested(
    email = email,
    expiresInSeconds = expiresInSeconds,
)

internal fun KakaoAccountLinkRequiredDataResponse.toDomain(): KakaoAccountLinkRequired =
    KakaoAccountLinkRequired(
        email = email,
        provider = provider.toAuthProvider(),
        linkToken = linkToken,
        expiresInSeconds = expiresInSeconds,
    )

internal fun KakaoNicknameRequiredDataResponse.toDomain(): KakaoNicknameRequired =
    KakaoNicknameRequired(
        signupToken = signupToken,
        provider = provider.toAuthProvider(),
        suggestedNickname = suggestedNickname,
    )

internal fun FieldErrorResponse.toDomain(): FieldError? {
    val field = field ?: return null
    val reason = reason ?: return null

    return FieldError(
        field = field,
        reason = reason,
    )
}

private fun String.toUserStatus(): UserStatus = when (lowercase()) {
    "active" -> UserStatus.Active
    "locked" -> UserStatus.Locked
    "inactive" -> UserStatus.Inactive
    else -> UserStatus.Unknown
}

private fun String.toAuthProvider(): AuthProvider = when (lowercase()) {
    "local" -> AuthProvider.Local
    "kakao" -> AuthProvider.Kakao
    "apple" -> AuthProvider.Apple
    else -> AuthProvider.Unknown
}
