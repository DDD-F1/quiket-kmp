package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class AccountSettingState(
    val isLoading: Boolean = true,
    val nickname: String = "",
    val email: String = "",
    val providers: List<String> = emptyList(),
    val isOperationLoading: Boolean = false,
) : UiState

val AccountSettingState.isLocalAccount: Boolean
    get() = providers.contains("local")

sealed interface AccountSettingIntent : UiIntent {
    data object LoadProfile : AccountSettingIntent
    data class UpdateNickname(val nickname: String) : AccountSettingIntent
    data class RequestEmailChange(val newEmail: String) : AccountSettingIntent
    data class ConfirmEmailChange(val newEmail: String, val code: String) : AccountSettingIntent
    data class ChangePassword(
        val currentPassword: String,
        val newPassword: String,
        val newPasswordConfirm: String,
    ) : AccountSettingIntent
    data class DeleteAccount(
        val password: String?,
        val agreed: Boolean,
    ) : AccountSettingIntent
    data object NavigateBack : AccountSettingIntent
}

sealed interface AccountSettingEffect : UiEffect {
    data object GoBack : AccountSettingEffect
    data class ShowMessage(val message: String) : AccountSettingEffect
    data class NicknameUpdated(val nickname: String) : AccountSettingEffect
    data class EmailChangeRequested(val email: String, val expiresInSeconds: Long) : AccountSettingEffect
    data class EmailChanged(val newEmail: String) : AccountSettingEffect
    data object PasswordChanged : AccountSettingEffect
    data object AccountDeleted : AccountSettingEffect
}