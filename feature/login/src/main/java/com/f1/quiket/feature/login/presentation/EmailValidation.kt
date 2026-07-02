package com.f1.quiket.feature.login.presentation

private val EmailRegex = Regex(
    pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
)

internal fun isValidEmail(email: String): Boolean =
    email.trim().matches(EmailRegex)

internal const val EmailFormatErrorMessage = "올바른 이메일 형식으로 입력해주세요"
