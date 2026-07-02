package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class KakaoAccountLinkState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val emailErrorMessage: String? = null,
    val passwordErrorMessage: String? = null,
) : UiState {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isSubmitting
}

sealed interface KakaoAccountLinkIntent : UiIntent {
    data class PasswordChanged(val value: String) : KakaoAccountLinkIntent
    data object TogglePasswordVisibility : KakaoAccountLinkIntent
    data object Submit : KakaoAccountLinkIntent
}

sealed interface KakaoAccountLinkEffect : UiEffect {
    data object NavigateToMain : KakaoAccountLinkEffect
    data class ShowMessage(val message: String) : KakaoAccountLinkEffect
}
