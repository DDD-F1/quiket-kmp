package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class SignupTermsUiState(
    val termsState: SignUpTermsState = SignUpTermsState(),
    val isSubmitting: Boolean = false,
) : UiState

sealed interface SignupTermsIntent : UiIntent {
    data object ToggleAllTerms : SignupTermsIntent
    data object ToggleServiceTerms : SignupTermsIntent
    data object TogglePrivacyTerms : SignupTermsIntent
    data object ToggleMarketingTerms : SignupTermsIntent
    data object AgreeServiceTerms : SignupTermsIntent
    data object AgreePrivacyTerms : SignupTermsIntent
    data object Submit : SignupTermsIntent
}

sealed interface SignupTermsEffect : UiEffect {
    data object NavigateToEmailVerification : SignupTermsEffect
    data class ShowMessage(val message: String) : SignupTermsEffect
}
