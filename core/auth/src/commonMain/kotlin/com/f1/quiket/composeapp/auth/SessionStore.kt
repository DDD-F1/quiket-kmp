package com.f1.quiket.composeapp.auth

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class SessionSnapshot(
    val onboardingCompleted: Boolean,
    val homeGuideCompleted: Boolean,
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val nickname: String?,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
) {
    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
}

internal class SessionStorageException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

@Serializable
internal data class PersistedAuthPayload(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val nickname: String? = null,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
)

internal sealed interface LegacyAuthState {
    data object Empty : LegacyAuthState

    data object Invalid : LegacyAuthState

    data class Migratable(
        val payload: PersistedAuthPayload,
    ) : LegacyAuthState
}

internal val SessionStorageJson = Json {
    ignoreUnknownKeys = true
}

internal fun emptySessionSnapshot(
    onboardingCompleted: Boolean,
    homeGuideCompleted: Boolean,
): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = onboardingCompleted,
    homeGuideCompleted = homeGuideCompleted,
    accessToken = null,
    refreshToken = null,
    tokenType = null,
    nickname = null,
    accessTokenExpiresIn = 0L,
    refreshTokenExpiresIn = 0L,
)

internal fun PersistedAuthPayload.toSessionSnapshot(
    onboardingCompleted: Boolean,
    homeGuideCompleted: Boolean,
): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = onboardingCompleted,
    homeGuideCompleted = homeGuideCompleted,
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType,
    nickname = nickname,
    accessTokenExpiresIn = accessTokenExpiresIn,
    refreshTokenExpiresIn = refreshTokenExpiresIn,
)

internal fun AuthTokenData.toPersistedAuthPayload(existingNickname: String?): PersistedAuthPayload =
    PersistedAuthPayload(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        nickname = user?.nickname ?: existingNickname,
        accessTokenExpiresIn = accessTokenExpiresIn,
        refreshTokenExpiresIn = refreshTokenExpiresIn,
    )

internal fun resolveLegacyAuthState(
    accessToken: String?,
    refreshToken: String?,
    tokenType: String?,
    nickname: String?,
    accessTokenExpiresIn: Long?,
    refreshTokenExpiresIn: Long?,
): LegacyAuthState {
    val normalizedAccessToken = accessToken?.trim().takeUnless { it.isNullOrEmpty() }
    val normalizedRefreshToken = refreshToken?.trim().takeUnless { it.isNullOrEmpty() }
    val normalizedTokenType = tokenType?.trim().takeUnless { it.isNullOrEmpty() }
    val normalizedNickname = nickname?.trim().takeUnless { it.isNullOrEmpty() }
    val hasAnyLegacyAuth = normalizedAccessToken != null ||
        normalizedRefreshToken != null ||
        normalizedTokenType != null ||
        normalizedNickname != null ||
        accessTokenExpiresIn != null ||
        refreshTokenExpiresIn != null
    if (!hasAnyLegacyAuth) {
        return LegacyAuthState.Empty
    }
    if (normalizedAccessToken == null || normalizedRefreshToken == null || normalizedTokenType == null) {
        return LegacyAuthState.Invalid
    }
    return LegacyAuthState.Migratable(
        payload = PersistedAuthPayload(
            accessToken = normalizedAccessToken,
            refreshToken = normalizedRefreshToken,
            tokenType = normalizedTokenType,
            nickname = normalizedNickname,
            accessTokenExpiresIn = accessTokenExpiresIn ?: 0L,
            refreshTokenExpiresIn = refreshTokenExpiresIn ?: 0L,
        ),
    )
}

internal expect object SessionStore {
    suspend fun read(): SessionSnapshot
    suspend fun saveOnboardingCompleted()
    suspend fun saveHomeGuideCompleted()
    suspend fun saveAuth(tokenData: AuthTokenData)
    suspend fun clearAuth()
}
