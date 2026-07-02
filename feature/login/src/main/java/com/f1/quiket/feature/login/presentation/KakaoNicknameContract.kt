package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class KakaoNicknameState(
    val nickname: String = "",
    val nicknameErrorMessage: String? = null,
    val isSubmitting: Boolean = false,
) : UiState {
    val isNextEnabled: Boolean
        get() = isValidNickname(nickname.trim()) && nicknameErrorMessage == null && !isSubmitting
}

sealed interface KakaoNicknameIntent : UiIntent {
    data class NicknameChanged(val value: String) : KakaoNicknameIntent
    data object Submit : KakaoNicknameIntent
}

sealed interface KakaoNicknameEffect : UiEffect {
    data object NavigateToMain : KakaoNicknameEffect
    data class ShowMessage(val message: String) : KakaoNicknameEffect
}
