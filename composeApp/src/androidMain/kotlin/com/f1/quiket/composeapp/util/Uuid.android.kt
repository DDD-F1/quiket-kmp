package com.f1.quiket.composeapp.util

import java.util.UUID

internal actual fun generateUuid(): String = UUID.randomUUID().toString()
