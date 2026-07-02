package com.f1.quiket.feature.login.presentation

import android.content.Context
import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class LoginState(
    val isKakaoLoading: Boolean = false,
) : UiState

sealed interface LoginIntent : UiIntent {
    data class KakaoLoginClick(val context: Context) : LoginIntent
}

sealed interface LoginEffect : UiEffect {
    data object NavigateToMain : LoginEffect
    data object NavigateToKakaoNickname : LoginEffect
    data object NavigateToKakaoAccountLink : LoginEffect
    data class ShowMessage(val message: String) : LoginEffect
}
