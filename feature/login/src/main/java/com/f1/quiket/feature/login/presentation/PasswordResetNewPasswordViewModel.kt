package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordResetNewPasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val passwordResetDraftStore: PasswordResetDraftStore,
) : MviViewModel<
    PasswordResetNewPasswordState,
    PasswordResetNewPasswordIntent,
    PasswordResetNewPasswordEffect,
    >(
    initialState = PasswordResetNewPasswordState(),
) {
    override fun handleIntent(intent: PasswordResetNewPasswordIntent) {
        when (intent) {
            is PasswordResetNewPasswordIntent.PasswordChanged -> updateState {
                copy(
                    password = intent.value,
                    passwordErrorMessage = null,
                    passwordConfirmErrorMessage = if (passwordConfirm.isNotBlank() && passwordConfirm != intent.value) {
                        PasswordConfirmMismatchMessage
                    } else {
                        null
                    },
                )
            }

            is PasswordResetNewPasswordIntent.PasswordConfirmChanged -> updateState {
                copy(
                    passwordConfirm = intent.value,
                    passwordConfirmErrorMessage = if (intent.value.isNotBlank() && intent.value != password) {
                        PasswordConfirmMismatchMessage
                    } else {
                        null
                    },
                )
            }

            PasswordResetNewPasswordIntent.TogglePasswordVisibility -> updateState {
                copy(isPasswordVisible = !isPasswordVisible)
            }

            PasswordResetNewPasswordIntent.TogglePasswordConfirmVisibility -> updateState {
                copy(isPasswordConfirmVisible = !isPasswordConfirmVisible)
            }

            PasswordResetNewPasswordIntent.HideEmailVerifiedMessage -> updateState {
                copy(showEmailVerifiedMessage = false)
            }

            PasswordResetNewPasswordIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val draft = passwordResetDraftStore.get()
        if (!isValidAuthPassword(currentState.password)) {
            updateState { copy(passwordErrorMessage = PasswordPolicyErrorMessage) }
            return
        }
        if (currentState.password != currentState.passwordConfirm) {
            updateState { copy(passwordConfirmErrorMessage = PasswordConfirmMismatchMessage) }
            return
        }

        launch {
            updateState { copy(isSubmitting = true) }
            when (
                val result = repository.confirmPasswordReset(
                    email = draft.email,
                    verificationCode = draft.verificationCode,
                    newPassword = currentState.password,
                    newPasswordConfirm = currentState.passwordConfirm,
                )
            ) {
                is AuthResult.Success -> {
                    passwordResetDraftStore.clear()
                    updateState { copy(isSubmitting = false) }
                    sendEffect(PasswordResetNewPasswordEffect.NavigateToLogin)
                }

                is AuthResult.Failure -> {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(PasswordResetNewPasswordEffect.ShowMessage(result.message))
                }
            }
        }
    }
}
