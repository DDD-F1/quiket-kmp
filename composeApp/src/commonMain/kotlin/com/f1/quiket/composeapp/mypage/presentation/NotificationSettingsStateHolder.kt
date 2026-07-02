package com.f1.quiket.composeapp.mypage.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.mypage.domain.model.MyPageException
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings
import com.f1.quiket.composeapp.mypage.NotificationSettingsUiState
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.network.toUserFacingMessage

internal class NotificationSettingsStateHolder(
    private val myPageUseCases: MyPageUseCases,
) {
    var state by mutableStateOf(NotificationSettingsUiState())
        private set

    suspend fun loadSettings(onSessionExpired: () -> Unit) {
        state = state.copy(isLoading = true, message = null)
        runCatching {
            myPageUseCases.getNotificationSettings()
        }.onSuccess { settings ->
            state = state.copy(
                isLoading = false,
                isSaving = false,
                settings = settings,
                message = null,
            )
        }.onFailure { error ->
            handleError(error, fallbackMessage = "알림 설정을 불러오지 못했습니다.", onSessionExpired)
        }
    }

    suspend fun updateSettings(
        nextSettings: NotificationSettings,
        onSessionExpired: () -> Unit,
    ) {
        val previousSettings = state.settings
        state = state.copy(isSaving = true, settings = nextSettings, message = null)
        runCatching {
            myPageUseCases.updateNotificationSettings(nextSettings)
        }.onSuccess { savedSettings ->
            state = state.copy(
                isSaving = false,
                settings = savedSettings,
                message = "알림 설정이 저장되었습니다.",
            )
        }.onFailure { error ->
            state = state.copy(settings = previousSettings)
            handleError(error, fallbackMessage = "알림 설정을 저장하지 못했습니다.", onSessionExpired)
        }
    }

    private fun handleError(
        error: Throwable,
        fallbackMessage: String,
        onSessionExpired: () -> Unit,
    ) {
        if (error is MyPageException && error.isUnauthorized) {
            onSessionExpired()
        } else {
            state = state.copy(
                isLoading = false,
                isSaving = false,
                message = error.toUserFacingMessage(fallbackMessage),
            )
        }
    }
}
