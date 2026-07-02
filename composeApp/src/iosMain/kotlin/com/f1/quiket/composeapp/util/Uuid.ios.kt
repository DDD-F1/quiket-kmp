package com.f1.quiket.composeapp.util

import platform.Foundation.NSUUID

internal actual fun generateUuid(): String = NSUUID().UUIDString.lowercase()
