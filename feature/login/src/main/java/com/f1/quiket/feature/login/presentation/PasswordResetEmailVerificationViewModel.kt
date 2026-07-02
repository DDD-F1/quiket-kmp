package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordResetEmailVerificationViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val passwordResetDraftStore: PasswordResetDraftStore,
) : MviViewModel<
    PasswordResetEmailVerificationState,
    PasswordResetEmailVerificationIntent,
    PasswordResetEmailVerificationEffect,
    >(
    initialState = passwordResetDraftStore.get().let { draft ->
        PasswordResetEmailVerificationState(
            email = draft.email,
            isVerificationRequested = draft.resetCodeSent,
            showVerificationSentMessage = draft.resetCodeSent,
        )
    },
) {
    override fun handleIntent(intent: PasswordResetEmailVerificationIntent) {
        when (intent) {
            is PasswordResetEmailVerificationIntent.EmailChanged -> updateState {
                copy(
                    email = intent.value,
                    verificationCode = "",
                    isVerificationRequested = false,
                    emailErrorMessage = null,
                    verificationCodeErrorMessage = null,
                )
            }

            is PasswordResetEmailVerificationIntent.VerificationCodeChanged -> updateState {
                copy(
                    verificationCode = intent.value.filter(Char::isDigit).take(6),
                    verificationCodeErrorMessage = null,
                )
            }

            PasswordResetEmailVerificationIntent.RequestCode -> requestCode()
            PasswordResetEmailVerificationIntent.CodeActionClick -> codeActionClick()
            PasswordResetEmailVerificationIntent.Tick -> updateState {
                val nextSeconds = (timerSeconds - 1).coerceAtLeast(0)
                copy(
                    timerSeconds = nextSeconds,
                    verificationCodeErrorMessage = if (nextSeconds == 0) {
                        VerificationExpiredErrorMessage
                    } else {
                        verificationCodeErrorMessage
                    },
                )
            }

            PasswordResetEmailVerificationIntent.HideVerificationSentMessage -> updateState {
                copy(showVerificationSentMessage = false)
            }
        }
    }

    private fun requestCode() {
        val email = currentState.email.trim()
        if (!isValidEmail(email)) {
            updateState { copy(emailErrorMessage = EmailFormatErrorMessage) }
            return
        }

        launch {
            updateState {
                copy(
                    isSubmitting = true,
                    emailErrorMessage = null,
                    verificationCodeErrorMessage = null,
                )
            }

            when (val result = repository.requestPasswordReset(email)) {
                is AuthResult.Success -> {
                    passwordResetDraftStore.update {
                        copy(
                            email = email,
                            verificationCode = "",
                            resetCodeSent = true,
                        )
                    }
                    updateState {
                        copy(
                            email = result.data.email,
                            verificationCode = "",
                            timerSeconds = result.data.expiresInSeconds.toInt(),
                            isVerificationRequested = true,
                            isSubmitting = false,
                            showVerificationSentMessage = true,
                        )
                    }
                }

                is AuthResult.Failure -> updateState {
                    copy(
                        isSubmitting = false,
                        isVerificationRequested = false,
                        emailErrorMessage = result.message,
                    )
                }
            }
        }
    }

    private fun codeActionClick() {
        if (currentState.verificationCodeErrorMessage != null || currentState.verificationCode.length < 6) {
            requestCode()
            return
        }
        if (currentState.timerSeconds <= 0) {
            updateState { copy(verificationCodeErrorMessage = VerificationExpiredErrorMessage) }
            return
        }

        passwordResetDraftStore.update {
            copy(
                email = currentState.email.trim(),
                verificationCode = currentState.verificationCode,
                resetCodeSent = true,
            )
        }
        launch { sendEffect(PasswordResetEmailVerificationEffect.NavigateNext) }
    }
}
