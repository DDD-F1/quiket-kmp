package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class SignupCodeVerificationState(
    val email: String = "",
    val verificationCode: String = "",
    val timerSeconds: Int = VerificationTimeoutSeconds,
    val isSubmitting: Boolean = false,
    val verificationCodeErrorMessage: String? = null,
    val showVerificationSentMessage: Boolean = true,
) : UiState {
    val isCodeComplete: Boolean
        get() = verificationCode.length == 6
}

sealed interface SignupCodeVerificationIntent : UiIntent {
    data class VerificationCodeChanged(val value: String) : SignupCodeVerificationIntent
    data object Tick : SignupCodeVerificationIntent
    data object CodeActionClick : SignupCodeVerificationIntent
    data object HideVerificationSentMessage : SignupCodeVerificationIntent
}

sealed interface SignupCodeVerificationEffect : UiEffect {
    data object NavigateToMain : SignupCodeVerificationEffect
    data class ShowMessage(val message: String) : SignupCodeVerificationEffect
}
