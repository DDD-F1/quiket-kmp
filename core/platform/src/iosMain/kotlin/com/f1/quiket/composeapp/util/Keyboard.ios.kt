package com.f1.quiket.composeapp.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.objc.sel_registerName

@OptIn(ExperimentalForeignApi::class)
actual fun hidePlatformKeyboard() {
    UIApplication.sharedApplication.sendAction(
        action = sel_registerName("resignFirstResponder"),
        to = null,
        from = null,
        forEvent = null,
    )
}
