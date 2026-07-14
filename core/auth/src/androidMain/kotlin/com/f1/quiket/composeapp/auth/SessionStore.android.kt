package com.f1.quiket.composeapp.auth

import android.content.SharedPreferences
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData

internal actual object SessionStore {
    actual suspend fun read(): SessionSnapshot {
        val generalPreferences = AndroidSessionContext.generalPreferences()
        val securePreferences = AndroidSessionContext.securePreferences()
        val onboardingCompleted = generalPreferences.getBoolean(KeyOnboardingCompleted, false)
        val homeGuideCompleted = generalPreferences.getBoolean(KeyHomeGuideCompleted, false)

        if (isAuthInvalidated(generalPreferences)) {
            attemptAuthCleanup(
                generalPreferences = generalPreferences,
                securePreferences = securePreferences,
                clearInvalidationMarkerOnSuccess = true,
            )
            return emptySessionSnapshot(
                onboardingCompleted = onboardingCompleted,
                homeGuideCompleted = homeGuideCompleted,
            )
        }

        val payload = readPersistedAuthPayload(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
            recoverOnFailure = true,
        )
        return payload?.toSessionSnapshot(
            onboardingCompleted = onboardingCompleted,
            homeGuideCompleted = homeGuideCompleted,
        ) ?: emptySessionSnapshot(
            onboardingCompleted = onboardingCompleted,
            homeGuideCompleted = homeGuideCompleted,
        )
    }

    actual suspend fun saveOnboardingCompleted() {
        writeGeneralPreferences(
            message = "Failed to persist onboarding completion state.",
        ) {
            putBoolean(KeyOnboardingCompleted, true)
        }
    }

    actual suspend fun saveHomeGuideCompleted() {
        writeGeneralPreferences(
            message = "Failed to persist home-guide completion state.",
        ) {
            putBoolean(KeyHomeGuideCompleted, true)
        }
    }

    actual suspend fun saveAuth(tokenData: AuthTokenData) {
        val generalPreferences = AndroidSessionContext.generalPreferences()
        val securePreferences = AndroidSessionContext.securePreferences()

        ensureAuthCleanupCompleteBeforeWrite(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
        )

        val existingNickname = readPersistedAuthPayload(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
            recoverOnFailure = false,
        )?.nickname ?: generalPreferences.getString(KeyNickname, null)
        val payload = tokenData.toPersistedAuthPayload(existingNickname = existingNickname)
        val encryptedPayload = try {
            AndroidSessionCipher.encrypt(
                SessionStorageJson.encodeToString(
                    PersistedAuthPayload.serializer(),
                    payload,
                ),
            )
        } catch (cause: Throwable) {
            handleWriteFailure(
                generalPreferences = generalPreferences,
                securePreferences = securePreferences,
                message = "Failed to encrypt Android auth state.",
                cause = cause,
            )
        }

        beginAuthInvalidation(generalPreferences)
        try {
            writeGeneralPreferences(
                preferences = generalPreferences,
                message = "Failed to persist Android auth metadata.",
            ) {
                putBoolean(KeyOnboardingCompleted, true)
                removeLegacyAuthKeys()
            }
            writeSecurePreferences(
                preferences = securePreferences,
                message = "Failed to persist Android auth state.",
            ) {
                putString(KeyEncryptedAuthPayload, encryptedPayload)
            }
            clearAuthInvalidation(generalPreferences)
        } catch (cause: Throwable) {
            handleWriteFailure(
                generalPreferences = generalPreferences,
                securePreferences = securePreferences,
                message = "Failed to persist Android auth state.",
                cause = cause,
            )
        }
    }

    actual suspend fun clearAuth() {
        val generalPreferences = AndroidSessionContext.generalPreferences()
        val securePreferences = AndroidSessionContext.securePreferences()
        beginAuthInvalidation(generalPreferences)
        try {
            writeGeneralPreferences(
                preferences = generalPreferences,
                message = "Failed to clear Android auth metadata.",
            ) {
                removeLegacyAuthKeys()
            }
            writeSecurePreferences(
                preferences = securePreferences,
                message = "Failed to clear Android auth state.",
                onFailure = {
                    AndroidSessionCipher.resetKey()
                },
            ) {
                remove(KeyEncryptedAuthPayload)
            }
            clearAuthInvalidation(generalPreferences)
        } catch (cause: Throwable) {
            AndroidSessionCipher.resetKey()
            throw SessionStorageException("Failed to clear Android auth state.", cause)
        }
    }

    private fun readPersistedAuthPayload(
        generalPreferences: SharedPreferences,
        securePreferences: SharedPreferences,
        recoverOnFailure: Boolean,
    ): PersistedAuthPayload? {
        val encryptedPayload = securePreferences.getString(KeyEncryptedAuthPayload, null)
        if (encryptedPayload != null) {
            val payload = try {
                SessionStorageJson.decodeFromString(
                    PersistedAuthPayload.serializer(),
                    AndroidSessionCipher.decrypt(encryptedPayload),
                )
            } catch (cause: Throwable) {
                return recoverAuthReadFailure(
                    generalPreferences = generalPreferences,
                    securePreferences = securePreferences,
                    message = "Failed to read Android auth state.",
                    cause = cause,
                    recoverOnFailure = recoverOnFailure,
                )
            }
            if (hasLegacyAuth(generalPreferences)) {
                return try {
                    beginAuthInvalidation(generalPreferences)
                    writeGeneralPreferences(
                        preferences = generalPreferences,
                        message = "Failed to remove legacy Android auth state.",
                    ) {
                        removeLegacyAuthKeys()
                    }
                    clearAuthInvalidation(generalPreferences)
                    payload
                } catch (cause: Throwable) {
                    recoverAuthReadFailure(
                        generalPreferences = generalPreferences,
                        securePreferences = securePreferences,
                        message = "Failed to remove legacy Android auth state.",
                        cause = cause,
                        recoverOnFailure = recoverOnFailure,
                    )
                }
            }
            return payload
        }

        return when (
            val legacyAuthState = resolveLegacyAuthState(
                accessToken = generalPreferences.getString(KeyAccessToken, null),
                refreshToken = generalPreferences.getString(KeyRefreshToken, null),
                tokenType = generalPreferences.getString(KeyTokenType, null),
                nickname = generalPreferences.getString(KeyNickname, null),
                accessTokenExpiresIn = generalPreferences.longOrNull(KeyAccessTokenExpiresIn),
                refreshTokenExpiresIn = generalPreferences.longOrNull(KeyRefreshTokenExpiresIn),
            )
        ) {
            LegacyAuthState.Empty -> null
            LegacyAuthState.Invalid -> {
                try {
                    beginAuthInvalidation(generalPreferences)
                    writeGeneralPreferences(
                        preferences = generalPreferences,
                        message = "Failed to clear invalid legacy Android auth state.",
                    ) {
                        removeLegacyAuthKeys()
                    }
                    clearAuthInvalidation(generalPreferences)
                    null
                } catch (cause: Throwable) {
                    recoverAuthReadFailure(
                        generalPreferences = generalPreferences,
                        securePreferences = securePreferences,
                        message = "Failed to clear invalid legacy Android auth state.",
                        cause = cause,
                        recoverOnFailure = recoverOnFailure,
                    )
                }
            }
            is LegacyAuthState.Migratable -> {
                val migratedPayload = legacyAuthState.payload
                val encryptedLegacyPayload = try {
                    AndroidSessionCipher.encrypt(
                        SessionStorageJson.encodeToString(
                            PersistedAuthPayload.serializer(),
                            migratedPayload,
                        ),
                    )
                } catch (cause: Throwable) {
                    return recoverAuthReadFailure(
                        generalPreferences = generalPreferences,
                        securePreferences = securePreferences,
                        message = "Failed to encrypt migrated Android auth state.",
                        cause = cause,
                        recoverOnFailure = recoverOnFailure,
                    )
                }
                try {
                    beginAuthInvalidation(generalPreferences)
                    writeGeneralPreferences(
                        preferences = generalPreferences,
                        message = "Failed to clear legacy Android auth state before migration.",
                    ) {
                        removeLegacyAuthKeys()
                    }
                    writeSecurePreferences(
                        preferences = securePreferences,
                        message = "Failed to migrate Android auth state.",
                    ) {
                        putString(KeyEncryptedAuthPayload, encryptedLegacyPayload)
                    }
                    clearAuthInvalidation(generalPreferences)
                    migratedPayload
                } catch (cause: Throwable) {
                    recoverAuthReadFailure(
                        generalPreferences = generalPreferences,
                        securePreferences = securePreferences,
                        message = "Failed to migrate Android auth state.",
                        cause = cause,
                        recoverOnFailure = recoverOnFailure,
                    )
                }
            }
        }
    }

    private fun ensureAuthCleanupCompleteBeforeWrite(
        generalPreferences: SharedPreferences,
        securePreferences: SharedPreferences,
    ) {
        if (!isAuthInvalidated(generalPreferences)) {
            return
        }
        val cleaned = attemptAuthCleanup(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
            clearInvalidationMarkerOnSuccess = true,
        )
        if (!cleaned) {
            throw SessionStorageException("Android auth cleanup is still pending.")
        }
    }

    private fun recoverAuthReadFailure(
        generalPreferences: SharedPreferences,
        securePreferences: SharedPreferences,
        message: String,
        cause: Throwable,
        recoverOnFailure: Boolean,
    ): PersistedAuthPayload? {
        invalidateAndCleanupAuthState(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
        )
        if (recoverOnFailure) {
            return null
        }
        throw SessionStorageException(message, cause)
    }

    private fun handleWriteFailure(
        generalPreferences: SharedPreferences,
        securePreferences: SharedPreferences,
        message: String,
        cause: Throwable,
    ): Nothing {
        invalidateAndCleanupAuthState(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
        )
        throw SessionStorageException(message, cause)
    }

    private fun invalidateAndCleanupAuthState(
        generalPreferences: SharedPreferences,
        securePreferences: SharedPreferences,
    ) {
        runCatching { beginAuthInvalidation(generalPreferences) }
        attemptAuthCleanup(
            generalPreferences = generalPreferences,
            securePreferences = securePreferences,
            clearInvalidationMarkerOnSuccess = false,
        )
    }

    private fun attemptAuthCleanup(
        generalPreferences: SharedPreferences,
        securePreferences: SharedPreferences,
        clearInvalidationMarkerOnSuccess: Boolean,
    ): Boolean {
        val secureCleared = runCatching {
            writeSecurePreferences(
                preferences = securePreferences,
                message = "Failed to clear Android auth state.",
                onFailure = {
                    AndroidSessionCipher.resetKey()
                },
            ) {
                remove(KeyEncryptedAuthPayload)
            }
        }.isSuccess
        val legacyCleared = runCatching {
            writeGeneralPreferences(
                preferences = generalPreferences,
                message = "Failed to clear Android legacy auth state.",
            ) {
                removeLegacyAuthKeys()
            }
        }.isSuccess
        if (!secureCleared || !legacyCleared) {
            AndroidSessionCipher.resetKey()
            return false
        }
        if (clearInvalidationMarkerOnSuccess) {
            return runCatching {
                clearAuthInvalidation(generalPreferences)
            }.isSuccess
        }
        return true
    }

    private fun beginAuthInvalidation(preferences: SharedPreferences) {
        writeGeneralPreferences(
            preferences = preferences,
            message = "Failed to invalidate Android auth state.",
        ) {
            putBoolean(KeyAuthInvalidationPending, true)
        }
    }

    private fun clearAuthInvalidation(preferences: SharedPreferences) {
        writeGeneralPreferences(
            preferences = preferences,
            message = "Failed to clear Android auth invalidation marker.",
        ) {
            remove(KeyAuthInvalidationPending)
        }
    }

    private fun isAuthInvalidated(preferences: SharedPreferences): Boolean =
        preferences.getBoolean(KeyAuthInvalidationPending, false)

    private fun hasLegacyAuth(preferences: SharedPreferences): Boolean =
        resolveLegacyAuthState(
            accessToken = preferences.getString(KeyAccessToken, null),
            refreshToken = preferences.getString(KeyRefreshToken, null),
            tokenType = preferences.getString(KeyTokenType, null),
            nickname = preferences.getString(KeyNickname, null),
            accessTokenExpiresIn = preferences.longOrNull(KeyAccessTokenExpiresIn),
            refreshTokenExpiresIn = preferences.longOrNull(KeyRefreshTokenExpiresIn),
        ) != LegacyAuthState.Empty

    private fun SharedPreferences.longOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    private fun SharedPreferences.Editor.removeLegacyAuthKeys() {
        remove(KeyAccessToken)
        remove(KeyRefreshToken)
        remove(KeyTokenType)
        remove(KeyNickname)
        remove(KeyAccessTokenExpiresIn)
        remove(KeyRefreshTokenExpiresIn)
    }

    private fun writeGeneralPreferences(
        preferences: SharedPreferences = AndroidSessionContext.generalPreferences(),
        message: String,
        onFailure: (() -> Unit)? = null,
        block: SharedPreferences.Editor.() -> Unit,
    ) {
        writePreferences(
            preferences = preferences,
            message = message,
            onFailure = onFailure,
            block = block,
        )
    }

    private fun writeSecurePreferences(
        preferences: SharedPreferences = AndroidSessionContext.securePreferences(),
        message: String,
        onFailure: (() -> Unit)? = null,
        block: SharedPreferences.Editor.() -> Unit,
    ) {
        writePreferences(
            preferences = preferences,
            message = message,
            onFailure = onFailure,
            block = block,
        )
    }

    private fun writePreferences(
        preferences: SharedPreferences,
        message: String,
        onFailure: (() -> Unit)? = null,
        block: SharedPreferences.Editor.() -> Unit,
    ) {
        val committed = runCatching {
            preferences.edit().apply(block).commit()
        }.getOrElse { cause ->
            onFailure?.invoke()
            throw SessionStorageException(message, cause)
        }
        if (!committed) {
            onFailure?.invoke()
            throw SessionStorageException(message)
        }
    }

    private const val KeyOnboardingCompleted = "onboarding_completed"
    private const val KeyHomeGuideCompleted = "home_guide_completed"
    private const val KeyAuthInvalidationPending = "auth_invalidation_pending"
    private const val KeyEncryptedAuthPayload = "encrypted_auth_payload"
    private const val KeyAccessToken = "access_token"
    private const val KeyRefreshToken = "refresh_token"
    private const val KeyTokenType = "token_type"
    private const val KeyNickname = "nickname"
    private const val KeyAccessTokenExpiresIn = "access_token_expires_in"
    private const val KeyRefreshTokenExpiresIn = "refresh_token_expires_in"
}
