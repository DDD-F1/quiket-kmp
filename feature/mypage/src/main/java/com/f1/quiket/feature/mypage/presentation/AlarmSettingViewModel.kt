package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.mypage.domain.model.NotificationSettings
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmSettingViewModel @Inject constructor(
    private val repository: MyPageRepository,
) : MviViewModel<AlarmSettingState, AlarmSettingIntent, AlarmSettingEffect>(
    initialState = AlarmSettingState(),
) {
    init {
        onIntent(AlarmSettingIntent.LoadSettings)
    }

    override fun handleIntent(intent: AlarmSettingIntent) {
        when (intent) {
            is AlarmSettingIntent.LoadSettings -> loadSettings()
            is AlarmSettingIntent.ToggleActivity -> updateSettings(activityEnabled = intent.enabled)
            is AlarmSettingIntent.ToggleUpdate -> updateSettings(updateEnabled = intent.enabled)
            is AlarmSettingIntent.ToggleReview -> updateSettings(reviewEnabled = intent.enabled)
            is AlarmSettingIntent.NavigateBack -> launch { sendEffect(AlarmSettingEffect.GoBack) }
        }
    }

    private fun loadSettings() {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = repository.getNotificationSettings()) {
                is NetworkResult.Success -> updateState {
                    copy(
                        isLoading = false,
                        activityEnabled = result.data.activityEnabled,
                        updateEnabled = result.data.updateEnabled,
                        reviewEnabled = result.data.reviewEnabled,
                    )
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(AlarmSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun updateSettings(
        activityEnabled: Boolean = currentState.activityEnabled,
        updateEnabled: Boolean = currentState.updateEnabled,
        reviewEnabled: Boolean = currentState.reviewEnabled,
    ) {
        val prevActivity = currentState.activityEnabled
        val prevUpdate = currentState.updateEnabled
        val prevReview = currentState.reviewEnabled

        launch {
            updateState {
                copy(
                    isSaving = true,
                    activityEnabled = activityEnabled,
                    updateEnabled = updateEnabled,
                    reviewEnabled = reviewEnabled,
                )
            }
            val settings = NotificationSettings(
                fcmTokenRegistered = null,
                activityEnabled = activityEnabled,
                updateEnabled = updateEnabled,
                reviewEnabled = reviewEnabled,
            )
            when (val result = repository.updateNotificationSettings(settings)) {
                is NetworkResult.Success -> updateState {
                    copy(
                        isSaving = false,
                        activityEnabled = result.data.activityEnabled,
                        updateEnabled = result.data.updateEnabled,
                        reviewEnabled = result.data.reviewEnabled,
                    )
                }
                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isSaving = false,
                            activityEnabled = prevActivity,
                            updateEnabled = prevUpdate,
                            reviewEnabled = prevReview,
                        )
                    }
                    sendEffect(AlarmSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }
}