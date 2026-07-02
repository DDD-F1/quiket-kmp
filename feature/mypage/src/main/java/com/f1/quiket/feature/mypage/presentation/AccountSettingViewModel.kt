package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.mypage.domain.model.AccountDelete
import com.f1.quiket.feature.mypage.domain.model.PasswordChange
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val repository: MyPageRepository,
) : MviViewModel<AccountSettingState, AccountSettingIntent, AccountSettingEffect>(
    initialState = AccountSettingState(),
) {
    init {
        onIntent(AccountSettingIntent.LoadProfile)
    }

    override fun handleIntent(intent: AccountSettingIntent) {
        when (intent) {
            is AccountSettingIntent.LoadProfile -> loadProfile()
            is AccountSettingIntent.UpdateNickname -> updateNickname(intent.nickname)
            is AccountSettingIntent.RequestEmailChange -> requestEmailChange(intent.newEmail)
            is AccountSettingIntent.ConfirmEmailChange -> confirmEmailChange(intent.newEmail, intent.code)
            is AccountSettingIntent.ChangePassword -> changePassword(
                intent.currentPassword,
                intent.newPassword,
                intent.newPasswordConfirm,
            )
            is AccountSettingIntent.DeleteAccount -> deleteAccount(intent.password, intent.agreed)
            is AccountSettingIntent.NavigateBack -> launch { sendEffect(AccountSettingEffect.GoBack) }
        }
    }

    private fun loadProfile() {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = repository.getMyProfile()) {
                is NetworkResult.Success -> updateState {
                    copy(
                        isLoading = false,
                        nickname = result.data.nickname,
                        email = result.data.email,
                        providers = result.data.providers,
                    )
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(AccountSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun updateNickname(nickname: String) {
        launch {
            updateState { copy(isOperationLoading = true) }
            when (val result = repository.updateMyNickname(nickname)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isOperationLoading = false,
                            nickname = result.data.nickname,
                        )
                    }
                    sendEffect(AccountSettingEffect.NicknameUpdated(result.data.nickname))
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun requestEmailChange(newEmail: String) {
        launch {
            updateState { copy(isOperationLoading = true) }
            when (val result = repository.requestMyEmailChange(newEmail)) {
                is NetworkResult.Success -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.EmailChangeRequested(result.data.email, result.data.expiresInSeconds))
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun confirmEmailChange(newEmail: String, code: String) {
        launch {
            updateState { copy(isOperationLoading = true) }
            when (val result = repository.confirmMyEmailChange(newEmail, code)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isOperationLoading = false,
                            email = result.data.email,
                        )
                    }
                    sendEffect(AccountSettingEffect.EmailChanged(result.data.email))
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun changePassword(current: String, new: String, confirm: String) {
        launch {
            updateState { copy(isOperationLoading = true) }
            when (val result = repository.updateMyPassword(PasswordChange(current, new, confirm))) {
                is NetworkResult.Success -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.PasswordChanged)
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun deleteAccount(password: String?, agreed: Boolean) {
        launch {
            updateState { copy(isOperationLoading = true) }
            when (val result = repository.deleteMyAccount(AccountDelete(password, agreed))) {
                is NetworkResult.Success -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.AccountDeleted)
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isOperationLoading = false) }
                    sendEffect(AccountSettingEffect.ShowMessage(result.message))
                }
            }
        }
    }
}