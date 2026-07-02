package com.f1.quiket.composeapp.auth

internal actual object SessionStore {
    actual suspend fun read(): SessionSnapshot {
        val preferences = AndroidSessionContext.preferences()
        return SessionSnapshot(
            onboardingCompleted = preferences.getBoolean(KeyOnboardingCompleted, false),
            homeGuideCompleted = preferences.getBoolean(KeyHomeGuideCompleted, false),
            accessToken = preferences.getString(KeyAccessToken, null),
            refreshToken = preferences.getString(KeyRefreshToken, null),
            tokenType = preferences.getString(KeyTokenType, null),
            nickname = preferences.getString(KeyNickname, null),
            accessTokenExpiresIn = preferences.getLong(KeyAccessTokenExpiresIn, 0L),
            refreshTokenExpiresIn = preferences.getLong(KeyRefreshTokenExpiresIn, 0L),
        )
    }

    actual suspend fun saveOnboardingCompleted() {
        AndroidSessionContext.preferences()
            .edit()
            .putBoolean(KeyOnboardingCompleted, true)
            .apply()
    }

    actual suspend fun saveHomeGuideCompleted() {
        AndroidSessionContext.preferences()
            .edit()
            .putBoolean(KeyHomeGuideCompleted, true)
            .apply()
    }

    actual suspend fun saveAuth(tokenData: AuthTokenData) {
        val preferences = AndroidSessionContext.preferences()
        val nickname = tokenData.user?.nickname
            ?: preferences.getString(KeyNickname, null)
        AndroidSessionContext.preferences()
            .edit()
            .putBoolean(KeyOnboardingCompleted, true)
            .putString(KeyAccessToken, tokenData.accessToken)
            .putString(KeyRefreshToken, tokenData.refreshToken)
            .putString(KeyTokenType, tokenData.tokenType)
            .putString(KeyNickname, nickname)
            .putLong(KeyAccessTokenExpiresIn, tokenData.accessTokenExpiresIn)
            .putLong(KeyRefreshTokenExpiresIn, tokenData.refreshTokenExpiresIn)
            .apply()
    }

    actual suspend fun clearAuth() {
        AndroidSessionContext.preferences()
            .edit()
            .remove(KeyAccessToken)
            .remove(KeyRefreshToken)
            .remove(KeyTokenType)
            .remove(KeyNickname)
            .remove(KeyAccessTokenExpiresIn)
            .remove(KeyRefreshTokenExpiresIn)
            .apply()
    }

    private const val KeyOnboardingCompleted = "onboarding_completed"
    private const val KeyHomeGuideCompleted = "home_guide_completed"
    private const val KeyAccessToken = "access_token"
    private const val KeyRefreshToken = "refresh_token"
    private const val KeyTokenType = "token_type"
    private const val KeyNickname = "nickname"
    private const val KeyAccessTokenExpiresIn = "access_token_expires_in"
    private const val KeyRefreshTokenExpiresIn = "refresh_token_expires_in"
}
