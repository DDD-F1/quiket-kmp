package com.f1.quiket.composeapp.auth

import platform.UIKit.UIDevice

internal actual object DeviceInfo {
    actual val deviceId: String
        get() = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "quiket-ios"

    actual val deviceName: String
        get() = UIDevice.currentDevice.name.ifBlank { "iOS" }
}
