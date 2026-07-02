package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class PasswordResetNewPasswordState(
    val password: String = "",
    val passwordConfirm: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val passwordErrorMessage: String? = null,
    val passwordConfirmErrorMessage: String? = null,
    val showEmailVerifiedMessage: Boolean = true,
) : UiState {
    val isCompleteEnabled: Boolean
        get() = isValidAuthPassword(password) &&
            passwordConfirm.isNotBlank() &&
            passwordConfirmErrorMessage == null &&
            !isSubmitting
}

sealed interface PasswordResetNewPasswordIntent : UiIntent {
    data class PasswordChanged(val value: String) : PasswordResetNewPasswordIntent
    data class PasswordConfirmChanged(val value: String) : PasswordResetNewPasswordIntent
    data object TogglePasswordVisibility : PasswordResetNewPasswordIntent
    data object TogglePasswordConfirmVisibility : PasswordResetNewPasswordIntent
    data object HideEmailVerifiedMessage : PasswordResetNewPasswordIntent
    data object Submit : PasswordResetNewPasswordIntent
}

sealed interface PasswordResetNewPasswordEffect : UiEffect {
    data object NavigateToLogin : PasswordResetNewPasswordEffect
    data class ShowMessage(val message: String) : PasswordResetNewPasswordEffect
}
