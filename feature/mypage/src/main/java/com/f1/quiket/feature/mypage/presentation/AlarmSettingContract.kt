package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class AlarmSettingState(
    val isLoading: Boolean = true,
    val activityEnabled: Boolean = false,
    val updateEnabled: Boolean = false,
    val reviewEnabled: Boolean = false,
    val isSaving: Boolean = false,
) : UiState

sealed interface AlarmSettingIntent : UiIntent {
    data object LoadSettings : AlarmSettingIntent
    data class ToggleActivity(val enabled: Boolean) : AlarmSettingIntent
    data class ToggleUpdate(val enabled: Boolean) : AlarmSettingIntent
    data class ToggleReview(val enabled: Boolean) : AlarmSettingIntent
    data object NavigateBack : AlarmSettingIntent
}

sealed interface AlarmSettingEffect : UiEffect {
    data object GoBack : AlarmSettingEffect
    data class ShowMessage(val message: String) : AlarmSettingEffect
}