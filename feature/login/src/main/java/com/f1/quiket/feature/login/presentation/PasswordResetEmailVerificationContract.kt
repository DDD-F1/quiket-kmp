package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class PasswordResetEmailVerificationState(
    val email: String = "",
    val verificationCode: String = "",
    val timerSeconds: Int = VerificationTimeoutSeconds,
    val isVerificationRequested: Boolean = false,
    val isSubmitting: Boolean = false,
    val emailErrorMessage: String? = null,
    val verificationCodeErrorMessage: String? = null,
    val showVerificationSentMessage: Boolean = false,
) : UiState

sealed interface PasswordResetEmailVerificationIntent : UiIntent {
    data class EmailChanged(val value: String) : PasswordResetEmailVerificationIntent
    data class VerificationCodeChanged(val value: String) : PasswordResetEmailVerificationIntent
    data object RequestCode : PasswordResetEmailVerificationIntent
    data object CodeActionClick : PasswordResetEmailVerificationIntent
    data object Tick : PasswordResetEmailVerificationIntent
    data object HideVerificationSentMessage : PasswordResetEmailVerificationIntent
}

sealed interface PasswordResetEmailVerificationEffect : UiEffect {
    data object NavigateNext : PasswordResetEmailVerificationEffect
    data class ShowMessage(val message: String) : PasswordResetEmailVerificationEffect
}
