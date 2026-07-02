package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class MyPageSettingState(
    val nickname: String = "",
    val appVersion: String = "",
): UiState

sealed interface MyPageSettingIntent: UiIntent {
    data object NavigateBack : MyPageSettingIntent
    data object NavigateToAccountSetting : MyPageSettingIntent
    data object NavigateToAlarmSetting : MyPageSettingIntent
    data object NavigateToInquiry : MyPageSettingIntent
    data object NavigateToTerms : MyPageSettingIntent
    data object NavigateToPrivacyPolicy : MyPageSettingIntent
    data object NavigateToAppInfo : MyPageSettingIntent
    data object Logout : MyPageSettingIntent
}

sealed interface MyPageSettingEffect: UiEffect {
    data object GoBack : MyPageSettingEffect
    data object GoToAccountSetting : MyPageSettingEffect
    data object GoToAlarmSetting : MyPageSettingEffect
    data object GoToInquiry : MyPageSettingEffect
    data object GoToTerms : MyPageSettingEffect
    data object GoToPrivacyPolicy : MyPageSettingEffect
    data object GoToAppInfo : MyPageSettingEffect
    data object GoToLogin : MyPageSettingEffect
}
