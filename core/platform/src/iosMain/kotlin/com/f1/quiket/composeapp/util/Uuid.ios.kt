package com.f1.quiket.composeapp.util

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString.lowercase()
