package com.f1.quiket.composeapp

import platform.Foundation.NSBundle

internal actual object AppMetadata {
    actual val appName: String
        get() = bundleString("CFBundleDisplayName")
            ?: bundleString("CFBundleName")
            ?: "Quiket"

    actual val versionName: String
        get() = bundleString("CFBundleShortVersionString") ?: "1.5"

    actual val buildNumber: String
        get() = bundleString("CFBundleVersion") ?: "1"

    private fun bundleString(key: String): String? =
        (NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String)
            ?.takeIf { it.isNotBlank() }
}
