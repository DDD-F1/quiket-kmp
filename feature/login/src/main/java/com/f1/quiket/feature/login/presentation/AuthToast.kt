package com.f1.quiket.feature.login.presentation

import android.content.Context
import android.widget.Toast

internal fun Context.showAuthToast(message: String) {
    if (message.isBlank()) return

    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}
