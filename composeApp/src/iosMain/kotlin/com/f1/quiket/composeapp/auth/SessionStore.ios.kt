package com.f1.quiket.composeapp.auth

import cnames.structs.__CFData
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
        ensureInstallMarker()

        val accessToken = IosKeychain.readString(KeyAccessToken)
            ?: defaults.stringForKey(KeyAccessToken)
        val refreshToken = IosKeychain.readString(KeyRefreshToken)
            ?: defaults.stringForKey(KeyRefreshToken)
        val tokenType = IosKeychain.readString(KeyTokenType)
            ?: defaults.stringForKey(KeyTokenType)
        val accessTokenExpiresIn = IosKeychain.readString(KeyAccessTokenExpiresIn)?.toLongOrNull()
            ?: defaults.integerForKey(KeyAccessTokenExpiresIn)
        val refreshTokenExpiresIn = IosKeychain.readString(KeyRefreshTokenExpiresIn)?.toLongOrNull()
            ?: defaults.integerForKey(KeyRefreshTokenExpiresIn)

        val snapshot = SessionSnapshot(
            onboardingCompleted = defaults.boolForKey(KeyOnboardingCompleted),
            homeGuideCompleted = defaults.boolForKey(KeyHomeGuideCompleted),
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            nickname = defaults.stringForKey(KeyNickname),
            accessTokenExpiresIn = accessTokenExpiresIn,
            refreshTokenExpiresIn = refreshTokenExpiresIn,
        )

        if (snapshot.isLoggedIn && !IosKeychain.hasAuthTokenPair()) {
            if (saveAuthFields(snapshot)) {
                removeLegacyAuthFields()
            }
        }

        return snapshot
    }

    actual suspend fun saveOnboardingCompleted() {
        defaults.setBool(true, KeyOnboardingCompleted)
        defaults.synchronize()
    }

    actual suspend fun saveHomeGuideCompleted() {
        defaults.setBool(true, KeyHomeGuideCompleted)
        defaults.synchronize()
    }

    actual suspend fun saveAuth(tokenData: AuthTokenData) {
        val nickname = tokenData.user?.nickname
            ?: defaults.stringForKey(KeyNickname)
        defaults.setBool(true, KeyOnboardingCompleted)
        if (nickname != null) {
            defaults.setObject(nickname, KeyNickname)
        } else {
            defaults.removeObjectForKey(KeyNickname)
        }
        val savedToKeychain = saveAuthFields(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
            tokenType = tokenData.tokenType,
            accessTokenExpiresIn = tokenData.accessTokenExpiresIn,
            refreshTokenExpiresIn = tokenData.refreshTokenExpiresIn,
        )
        if (savedToKeychain) {
            removeLegacyAuthFields()
        } else {
            saveLegacyAuthFields(tokenData)
        }
        defaults.synchronize()
    }

    actual suspend fun clearAuth() {
        clearAuthFields()
        defaults.synchronize()
    }

    private fun ensureInstallMarker() {
        if (defaults.boolForKey(KeyInstallMarker)) return

        if (!hasLocalInstallState()) {
            clearAuthFields()
        }
        defaults.setBool(true, KeyInstallMarker)
        defaults.synchronize()
    }

    private fun hasLocalInstallState(): Boolean =
        defaults.boolForKey(KeyOnboardingCompleted) ||
            defaults.boolForKey(KeyHomeGuideCompleted) ||
            defaults.stringForKey(KeyNickname) != null ||
            defaults.stringForKey(KeyAccessToken) != null ||
            defaults.stringForKey(KeyRefreshToken) != null

    private fun clearAuthFields() {
        IosKeychain.delete(KeyAccessToken)
        IosKeychain.delete(KeyRefreshToken)
        IosKeychain.delete(KeyTokenType)
        IosKeychain.delete(KeyAccessTokenExpiresIn)
        IosKeychain.delete(KeyRefreshTokenExpiresIn)
        listOf(
            KeyAccessToken,
            KeyRefreshToken,
            KeyTokenType,
            KeyNickname,
            KeyAccessTokenExpiresIn,
            KeyRefreshTokenExpiresIn,
        ).forEach(defaults::removeObjectForKey)
    }

    private fun saveAuthFields(snapshot: SessionSnapshot): Boolean {
        val accessToken = snapshot.accessToken ?: return false
        val refreshToken = snapshot.refreshToken ?: return false
        val tokenType = snapshot.tokenType ?: return false
        return saveAuthFields(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            accessTokenExpiresIn = snapshot.accessTokenExpiresIn,
            refreshTokenExpiresIn = snapshot.refreshTokenExpiresIn,
        )
    }

    private fun saveAuthFields(
        accessToken: String,
        refreshToken: String,
        tokenType: String,
        accessTokenExpiresIn: Long,
        refreshTokenExpiresIn: Long,
    ): Boolean = listOf(
        IosKeychain.saveString(KeyAccessToken, accessToken),
        IosKeychain.saveString(KeyRefreshToken, refreshToken),
        IosKeychain.saveString(KeyTokenType, tokenType),
        IosKeychain.saveString(KeyAccessTokenExpiresIn, accessTokenExpiresIn.toString()),
        IosKeychain.saveString(KeyRefreshTokenExpiresIn, refreshTokenExpiresIn.toString()),
    ).all { it }

    private fun saveLegacyAuthFields(tokenData: AuthTokenData) {
        defaults.setObject(tokenData.accessToken, KeyAccessToken)
        defaults.setObject(tokenData.refreshToken, KeyRefreshToken)
        defaults.setObject(tokenData.tokenType, KeyTokenType)
        defaults.setInteger(tokenData.accessTokenExpiresIn, KeyAccessTokenExpiresIn)
        defaults.setInteger(tokenData.refreshTokenExpiresIn, KeyRefreshTokenExpiresIn)
    }

    private fun removeLegacyAuthFields() {
        listOf(
            KeyAccessToken,
            KeyRefreshToken,
            KeyTokenType,
            KeyAccessTokenExpiresIn,
            KeyRefreshTokenExpiresIn,
        ).forEach(defaults::removeObjectForKey)
    }

    private const val KeyOnboardingCompleted = "onboarding_completed"
    private const val KeyHomeGuideCompleted = "home_guide_completed"
    private const val KeyInstallMarker = "install_marker"
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

    fun hasAuthTokenPair(): Boolean =
        !readString("access_token").isNullOrBlank() &&
            !readString("refresh_token").isNullOrBlank()

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
        delete(account)
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

    fun delete(account: String) {
        withQuery(account, Unit) { query ->
            SecItemDelete(query)
            Unit
        }
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
