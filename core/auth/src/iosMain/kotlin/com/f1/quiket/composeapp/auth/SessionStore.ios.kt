package com.f1.quiket.composeapp.auth

import cnames.structs.__CFData
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

internal actual object SessionStore {
    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    actual suspend fun read(): SessionSnapshot {
        val onboardingCompleted = defaults.boolForKey(KeyOnboardingCompleted)
        val homeGuideCompleted = defaults.boolForKey(KeyHomeGuideCompleted)
        if (!ensureInstallCleanupReadyForRead()) {
            return emptySessionSnapshot(
                onboardingCompleted = onboardingCompleted,
                homeGuideCompleted = homeGuideCompleted,
            )
        }
        if (isAuthInvalidated()) {
            attemptAuthCleanup(clearInvalidationMarkerOnSuccess = true)
            return emptySessionSnapshot(
                onboardingCompleted = onboardingCompleted,
                homeGuideCompleted = homeGuideCompleted,
            )
        }
        val payload = readPersistedAuthPayload(recoverOnFailure = true)
        return payload?.toSessionSnapshot(
            onboardingCompleted = onboardingCompleted,
            homeGuideCompleted = homeGuideCompleted,
        ) ?: emptySessionSnapshot(
            onboardingCompleted = onboardingCompleted,
            homeGuideCompleted = homeGuideCompleted,
        )
    }

    actual suspend fun saveOnboardingCompleted() {
        writeDefaults(message = "Failed to persist onboarding completion state.") {
            setBool(true, KeyOnboardingCompleted)
        }
    }

    actual suspend fun saveHomeGuideCompleted() {
        writeDefaults(message = "Failed to persist home-guide completion state.") {
            setBool(true, KeyHomeGuideCompleted)
        }
    }

    actual suspend fun saveAuth(tokenData: AuthTokenData) {
        if (!ensureInstallCleanupReadyForWrite()) {
            throw SessionStorageException("iOS auth cleanup is still pending after reinstall.")
        }
        if (isAuthInvalidated() && !attemptAuthCleanup(clearInvalidationMarkerOnSuccess = true)) {
            throw SessionStorageException("iOS auth invalidation cleanup is still pending.")
        }

        val existingNickname = readCurrentPersistedNickname()
        val serializedPayload = try {
            SessionStorageJson.encodeToString(
                PersistedAuthPayload.serializer(),
                tokenData.toPersistedAuthPayload(existingNickname = existingNickname),
            )
        } catch (cause: Throwable) {
            throw SessionStorageException("Failed to encode iOS auth state.", cause)
        }

        beginAuthInvalidation()
        if (!IosKeychain.saveString(KeyAuthPayload, serializedPayload)) {
            handleWriteFailure("Failed to save iOS auth state to Keychain.")
        }
        if (!clearLegacySecureAuth()) {
            handleWriteFailure("Failed to remove legacy iOS Keychain auth state.")
        }
        try {
            writeDefaults(message = "Failed to persist iOS auth session state.") {
                setBool(true, KeyOnboardingCompleted)
                setBool(true, KeyInstallMarker)
                removeLegacyDefaultsAuth()
                removeObjectForKey(KeyAuthInvalidationPending)
            }
        } catch (cause: Throwable) {
            handleWriteFailure(
                message = "Failed to persist iOS auth session state.",
                cause = cause,
            )
        }
    }

    actual suspend fun clearAuth() {
        beginAuthInvalidation()
        attemptAuthCleanup(clearInvalidationMarkerOnSuccess = true)
    }

    private fun ensureInstallCleanupReadyForRead(): Boolean {
        if (defaults.boolForKey(KeyInstallMarker)) {
            return true
        }
        if (hasLocalInstallState()) {
            return runCatching {
                writeDefaults(message = "Failed to persist iOS install marker.") {
                    setBool(true, KeyInstallMarker)
                }
            }.isSuccess
        }
        val cleaned = attemptAuthCleanup(clearInvalidationMarkerOnSuccess = false)
        if (!cleaned) {
            return false
        }
        return runCatching {
            writeDefaults(message = "Failed to persist iOS install marker.") {
                setBool(true, KeyInstallMarker)
            }
        }.isSuccess
    }

    private fun ensureInstallCleanupReadyForWrite(): Boolean =
        ensureInstallCleanupReadyForRead()

    private fun hasLocalInstallState(): Boolean =
        defaults.boolForKey(KeyOnboardingCompleted) ||
            defaults.boolForKey(KeyHomeGuideCompleted) ||
            defaults.stringForKey(KeyNickname) != null ||
            defaults.stringForKey(KeyAccessToken) != null ||
            defaults.stringForKey(KeyRefreshToken) != null ||
            defaults.boolForKey(KeyAuthInvalidationPending)

    private fun readCurrentPersistedNickname(): String? =
        readPersistedAuthPayload(recoverOnFailure = false)?.nickname ?: defaults.stringForKey(KeyNickname)

    private fun readPersistedAuthPayload(recoverOnFailure: Boolean): PersistedAuthPayload? {
        val storedPayload = IosKeychain.readString(KeyAuthPayload)
        if (storedPayload != null) {
            val payload = try {
                SessionStorageJson.decodeFromString(PersistedAuthPayload.serializer(), storedPayload)
            } catch (cause: Throwable) {
                return recoverAuthReadFailure(
                    message = "Failed to decode iOS auth state.",
                    cause = cause,
                    recoverOnFailure = recoverOnFailure,
                )
            }
            if (hasLegacyDefaultsAuth() || hasLegacySecureAuth()) {
                return try {
                    beginAuthInvalidation()
                    if (!clearLegacySecureAuth()) {
                        throw SessionStorageException("Failed to remove legacy iOS Keychain auth state.")
                    }
                    writeDefaults(message = "Failed to remove legacy iOS auth state.") {
                        removeLegacyDefaultsAuth()
                        removeObjectForKey(KeyAuthInvalidationPending)
                    }
                    payload
                } catch (cause: Throwable) {
                    recoverAuthReadFailure(
                        message = "Failed to remove legacy iOS auth state.",
                        cause = cause,
                        recoverOnFailure = recoverOnFailure,
                    )
                }
            }
            return payload
        }

        val legacyKeychainState = resolveLegacyAuthState(
            accessToken = IosKeychain.readString(KeyAccessToken),
            refreshToken = IosKeychain.readString(KeyRefreshToken),
            tokenType = IosKeychain.readString(KeyTokenType),
            nickname = IosKeychain.readString(KeyNickname) ?: defaults.stringForKey(KeyNickname),
            accessTokenExpiresIn = IosKeychain.readString(KeyAccessTokenExpiresIn)?.toLongOrNull(),
            refreshTokenExpiresIn = IosKeychain.readString(KeyRefreshTokenExpiresIn)?.toLongOrNull(),
        )
        if (legacyKeychainState != LegacyAuthState.Empty) {
            return migrateLegacyAuthState(
                state = legacyKeychainState,
                recoverOnFailure = recoverOnFailure,
            )
        }

        val legacyDefaultsState = resolveLegacyAuthState(
            accessToken = defaults.stringForKey(KeyAccessToken),
            refreshToken = defaults.stringForKey(KeyRefreshToken),
            tokenType = defaults.stringForKey(KeyTokenType),
            nickname = defaults.stringForKey(KeyNickname),
            accessTokenExpiresIn = defaults.longOrNull(KeyAccessTokenExpiresIn),
            refreshTokenExpiresIn = defaults.longOrNull(KeyRefreshTokenExpiresIn),
        )
        return migrateLegacyAuthState(
            state = legacyDefaultsState,
            recoverOnFailure = recoverOnFailure,
        )
    }

    private fun migrateLegacyAuthState(
        state: LegacyAuthState,
        recoverOnFailure: Boolean,
    ): PersistedAuthPayload? = when (state) {
        LegacyAuthState.Empty -> null
        LegacyAuthState.Invalid -> {
            try {
                beginAuthInvalidation()
                val cleaned = attemptAuthCleanup(clearInvalidationMarkerOnSuccess = true)
                if (!cleaned) {
                    throw SessionStorageException("Failed to clear invalid legacy iOS auth state.")
                }
                null
            } catch (cause: Throwable) {
                recoverAuthReadFailure(
                    message = "Failed to clear invalid legacy iOS auth state.",
                    cause = cause,
                    recoverOnFailure = recoverOnFailure,
                )
            }
        }
        is LegacyAuthState.Migratable -> {
            val payload = state.payload
            val serializedPayload = try {
                SessionStorageJson.encodeToString(PersistedAuthPayload.serializer(), payload)
            } catch (cause: Throwable) {
                return recoverAuthReadFailure(
                    message = "Failed to encode migrated iOS auth state.",
                    cause = cause,
                    recoverOnFailure = recoverOnFailure,
                )
            }
            beginAuthInvalidation()
            if (!IosKeychain.saveString(KeyAuthPayload, serializedPayload)) {
                return recoverAuthReadFailure(
                    message = "Failed to migrate iOS auth state to Keychain.",
                    cause = IllegalStateException("Keychain write returned false."),
                    recoverOnFailure = recoverOnFailure,
                )
            }
            return try {
                if (!clearLegacySecureAuth()) {
                    throw SessionStorageException("Failed to remove legacy iOS Keychain auth state.")
                }
                writeDefaults(message = "Failed to remove legacy iOS auth state.") {
                    removeLegacyDefaultsAuth()
                    removeObjectForKey(KeyAuthInvalidationPending)
                }
                payload
            } catch (cause: Throwable) {
                recoverAuthReadFailure(
                    message = "Failed to remove legacy iOS auth state.",
                    cause = cause,
                    recoverOnFailure = recoverOnFailure,
                )
            }
        }
    }

    private fun recoverAuthReadFailure(
        message: String,
        cause: Throwable,
        recoverOnFailure: Boolean,
    ): PersistedAuthPayload? {
        runCatching { beginAuthInvalidation() }
        attemptAuthCleanup(clearInvalidationMarkerOnSuccess = false)
        if (recoverOnFailure) {
            return null
        }
        throw SessionStorageException(message, cause)
    }

    private fun handleWriteFailure(
        message: String,
        cause: Throwable? = null,
    ): Nothing {
        attemptAuthCleanup(clearInvalidationMarkerOnSuccess = false)
        throw SessionStorageException(message, cause)
    }

    private fun beginAuthInvalidation() {
        writeDefaults(message = "Failed to invalidate iOS auth state.") {
            setBool(true, KeyAuthInvalidationPending)
        }
    }

    private fun isAuthInvalidated(): Boolean =
        defaults.boolForKey(KeyAuthInvalidationPending)

    private fun attemptAuthCleanup(clearInvalidationMarkerOnSuccess: Boolean): Boolean {
        val secureCleared = IosKeychain.delete(KeyAuthPayload) && clearLegacySecureAuth()
        defaults.removeLegacyDefaultsAuth()
        val defaultsCleared = defaults.synchronize()
        val markerCleared = if (clearInvalidationMarkerOnSuccess && secureCleared && defaultsCleared) {
            defaults.removeObjectForKey(KeyAuthInvalidationPending)
            defaults.synchronize()
        } else {
            true
        }
        return secureCleared && defaultsCleared && markerCleared
    }

    private fun clearLegacySecureAuth(): Boolean =
        IosKeychain.delete(KeyAccessToken) &&
            IosKeychain.delete(KeyRefreshToken) &&
            IosKeychain.delete(KeyTokenType) &&
            IosKeychain.delete(KeyNickname) &&
            IosKeychain.delete(KeyAccessTokenExpiresIn) &&
            IosKeychain.delete(KeyRefreshTokenExpiresIn)

    private fun hasLegacySecureAuth(): Boolean =
        IosKeychain.hasAny(
            KeyAccessToken,
            KeyRefreshToken,
            KeyTokenType,
            KeyNickname,
            KeyAccessTokenExpiresIn,
            KeyRefreshTokenExpiresIn,
        )

    private fun hasLegacyDefaultsAuth(): Boolean =
        resolveLegacyAuthState(
            accessToken = defaults.stringForKey(KeyAccessToken),
            refreshToken = defaults.stringForKey(KeyRefreshToken),
            tokenType = defaults.stringForKey(KeyTokenType),
            nickname = defaults.stringForKey(KeyNickname),
            accessTokenExpiresIn = defaults.longOrNull(KeyAccessTokenExpiresIn),
            refreshTokenExpiresIn = defaults.longOrNull(KeyRefreshTokenExpiresIn),
        ) != LegacyAuthState.Empty

    private fun NSUserDefaults.longOrNull(key: String): Long? =
        if (objectForKey(key) != null) integerForKey(key) else null

    private fun NSUserDefaults.removeLegacyDefaultsAuth() {
        removeObjectForKey(KeyAccessToken)
        removeObjectForKey(KeyRefreshToken)
        removeObjectForKey(KeyTokenType)
        removeObjectForKey(KeyNickname)
        removeObjectForKey(KeyAccessTokenExpiresIn)
        removeObjectForKey(KeyRefreshTokenExpiresIn)
    }

    private fun writeDefaults(
        message: String,
        block: NSUserDefaults.() -> Unit,
    ) {
        runCatching {
            defaults.run(block)
            defaults.synchronize()
        }.getOrElse { cause ->
            throw SessionStorageException(message, cause)
        }.also { synchronized ->
            if (!synchronized) {
                throw SessionStorageException(message)
            }
        }
    }

    private const val KeyOnboardingCompleted = "onboarding_completed"
    private const val KeyHomeGuideCompleted = "home_guide_completed"
    private const val KeyInstallMarker = "install_marker"
    private const val KeyAuthInvalidationPending = "auth_invalidation_pending"
    private const val KeyAuthPayload = "auth_payload"
    private const val KeyAccessToken = "access_token"
    private const val KeyRefreshToken = "refresh_token"
    private const val KeyTokenType = "token_type"
    private const val KeyNickname = "nickname"
    private const val KeyAccessTokenExpiresIn = "access_token_expires_in"
    private const val KeyRefreshTokenExpiresIn = "refresh_token_expires_in"
}

@OptIn(ExperimentalForeignApi::class)
private object IosKeychain {
    private const val Service = "com.f1.quiket.session"
    private const val KeychainFailureStatus = -1

    fun hasAny(vararg accounts: String): Boolean =
        accounts.any { !readString(it).isNullOrBlank() }

    fun readString(account: String): String? = memScoped {
        val result = alloc<COpaquePointerVar>()
        val status = withQuery(account, KeychainFailureStatus) { query ->
            CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            SecItemCopyMatching(query, result.ptr)
        }
        if (status != errSecSuccess) return@memScoped null
        val data = result.value?.reinterpret<__CFData>()
            ?: return@memScoped null
        val value = data.toByteArray().decodeToString()
        CFRelease(data)
        value
    }

    fun saveString(account: String, value: String): Boolean {
        if (!delete(account)) {
            return false
        }
        val bytes = value.encodeToByteArray()
        val data = bytes.usePinned { pinned ->
            CFDataCreate(
                allocator = kCFAllocatorDefault,
                bytes = pinned.addressOf(0).reinterpret(),
                length = bytes.size.convert(),
            )
        } ?: return false
        val status = withQuery(account, KeychainFailureStatus) { query ->
            CFDictionarySetValue(query, kSecValueData, data)
            CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            SecItemAdd(query, null)
        }
        CFRelease(data)
        return status == errSecSuccess
    }

    fun delete(account: String): Boolean {
        val status = withQuery(account, KeychainFailureStatus) { query ->
            SecItemDelete(query)
        }
        return status == errSecSuccess || status == errSecItemNotFound
    }

    private inline fun <T> withQuery(
        account: String,
        fallback: T,
        block: (query: platform.CoreFoundation.CFMutableDictionaryRef?) -> T,
    ): T {
        val query = CFDictionaryCreateMutable(
            allocator = kCFAllocatorDefault,
            capacity = 0,
            keyCallBacks = null,
            valueCallBacks = null,
        ) ?: return fallback
        val service = Service.toCFString()
        val accountValue = account.toCFString()
        if (service == null || accountValue == null) {
            if (service != null) CFRelease(service)
            if (accountValue != null) CFRelease(accountValue)
            CFRelease(query)
            return fallback
        }
        return try {
            CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(query, kSecAttrService, service)
            CFDictionarySetValue(query, kSecAttrAccount, accountValue)
            block(query)
        } finally {
            CFRelease(query)
            CFRelease(service)
            CFRelease(accountValue)
        }
    }

    private fun String.toCFString() =
        CFStringCreateWithCString(kCFAllocatorDefault, this, kCFStringEncodingUTF8)

    private fun platform.CoreFoundation.CFDataRef.toByteArray(): ByteArray {
        val length = CFDataGetLength(this).toInt()
        if (length == 0) return ByteArray(0)
        return CFDataGetBytePtr(this)?.readBytes(length) ?: ByteArray(0)
    }
}
