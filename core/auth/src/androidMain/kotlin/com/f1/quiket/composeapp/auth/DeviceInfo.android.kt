package com.f1.quiket.composeapp.auth

import android.os.Build
import android.provider.Settings
import java.util.UUID

internal actual object DeviceInfo {
    actual val deviceId: String
        get() {
            val context = AndroidSessionContext.contextOrNull()
                ?: return processFallbackDeviceId
            val androidId = runCatching {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }.getOrNull()?.trim().takeUnless { it.isNullOrEmpty() }
            if (androidId != null) {
                return androidId
            }
            synchronized(deviceIdLock) {
                val preferences = AndroidSessionContext.securePreferences()
                preferences.getString(KeyFallbackDeviceId, null)
                    ?.trim()
                    ?.takeUnless(String::isEmpty)
                    ?.let { return it }

                val fallbackDeviceId = processFallbackDeviceId
                preferences.edit()
                    .putString(KeyFallbackDeviceId, fallbackDeviceId)
                    .commit()
                return fallbackDeviceId
            }
        }

    actual val deviceName: String
        get() = listOf(Build.MANUFACTURER.orEmpty(), Build.MODEL.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
            .ifBlank { "Android" }

    private val deviceIdLock = Any()
    private val processFallbackDeviceId by lazy { UUID.randomUUID().toString() }

    private const val KeyFallbackDeviceId = "fallback_device_id"
}
