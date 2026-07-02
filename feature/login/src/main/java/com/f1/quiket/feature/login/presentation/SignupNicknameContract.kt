package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class SignupNicknameState(
    val nickname: String = "",
    val nicknameErrorMessage: String? = null,
) : UiState {
    val isNextEnabled: Boolean
        get() = isValidNickname(nickname.trim()) && nicknameErrorMessage == null
}

sealed interface SignupNicknameIntent : UiIntent {
    data class NicknameChanged(val value: String) : SignupNicknameIntent
    data object Next : SignupNicknameIntent
}

sealed interface SignupNicknameEffect : UiEffect {
    data object NavigateNext : SignupNicknameEffect
}
