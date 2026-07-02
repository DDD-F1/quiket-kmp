package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class SignupCredentialsState(
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val isEmailAvailable: Boolean = false,
    val isCheckingEmail: Boolean = false,
    val emailErrorMessage: String? = null,
    val passwordErrorMessage: String? = null,
    val passwordConfirmErrorMessage: String? = null,
    val showEmailAvailableMessage: Boolean = false,
) : UiState {
    val isNextEnabled: Boolean
        get() = isEmailAvailable &&
            isValidAuthPassword(password) &&
            passwordConfirm.isNotBlank() &&
            passwordConfirmErrorMessage == null &&
            !isCheckingEmail
}

sealed interface SignupCredentialsIntent : UiIntent {
    data class EmailChanged(val value: String) : SignupCredentialsIntent
    data class PasswordChanged(val value: String) : SignupCredentialsIntent
    data class PasswordConfirmChanged(val value: String) : SignupCredentialsIntent
    data object CheckEmailAvailability : SignupCredentialsIntent
    data object TogglePasswordVisibility : SignupCredentialsIntent
    data object TogglePasswordConfirmVisibility : SignupCredentialsIntent
    data object Next : SignupCredentialsIntent
    data object HideEmailAvailableMessage : SignupCredentialsIntent
}

sealed interface SignupCredentialsEffect : UiEffect {
    data object NavigateNext : SignupCredentialsEffect
    data class ShowMessage(val message: String) : SignupCredentialsEffect
}
