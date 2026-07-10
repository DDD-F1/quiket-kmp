package com.f1.quiket.composeapp.auth

import android.os.Build

internal actual object DeviceInfo {
    actual val deviceId: String = "quiket-android"

    actual val deviceName: String
        get() = listOf(Build.MANUFACTURER.orEmpty(), Build.MODEL.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
            .ifBlank { "Android" }
}
