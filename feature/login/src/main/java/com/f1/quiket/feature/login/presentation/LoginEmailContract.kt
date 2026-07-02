package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class LoginEmailState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailErrorMessage: String? = null,
    val passwordErrorMessage: String? = null,
    val showPasswordResetRequiredDialog: Boolean = false,
    val resetRequiredEmail: String = "",
) : UiState {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginEmailIntent : UiIntent {
    data class EmailChanged(val value: String) : LoginEmailIntent
    data class PasswordChanged(val value: String) : LoginEmailIntent
    data object TogglePasswordVisibility : LoginEmailIntent
    data object Login : LoginEmailIntent
    data object PasswordResetRequiredClick : LoginEmailIntent
    data object PasswordResetRequiredDismiss : LoginEmailIntent
}

sealed interface LoginEmailEffect : UiEffect {
    data object NavigateToMain : LoginEmailEffect
    data class NavigateToPasswordReset(val email: String) : LoginEmailEffect
    data class NavigateToEmailVerification(val email: String) : LoginEmailEffect
    data class ShowMessage(val message: String) : LoginEmailEffect
}
