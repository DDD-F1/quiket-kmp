package com.f1.quiket.composeapp.auth

internal data class SessionSnapshot(
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

internal expect object SessionStore {
    suspend fun read(): SessionSnapshot
    suspend fun saveOnboardingCompleted()
    suspend fun saveHomeGuideCompleted()
    suspend fun saveAuth(tokenData: AuthTokenData)
    suspend fun clearAuth()
}
