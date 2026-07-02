package com.f1.quiket.composeapp.login

internal data class PasswordResetDraft(
    val email: String = "",
    val verificationCode: String = "",
    val resetCodeSent: Boolean = false,
)
