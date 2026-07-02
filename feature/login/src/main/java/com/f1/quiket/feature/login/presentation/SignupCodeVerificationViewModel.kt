package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.auth.DeviceInfoProvider
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupCodeVerificationViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val signupDraftStore: SignupDraftStore,
) : MviViewModel<SignupCodeVerificationState, SignupCodeVerificationIntent, SignupCodeVerificationEffect>(
    initialState = SignupCodeVerificationState(
        email = signupDraftStore.get().email,
    ),
) {
    override fun handleIntent(intent: SignupCodeVerificationIntent) {
        when (intent) {
            is SignupCodeVerificationIntent.VerificationCodeChanged -> updateState {
                copy(
                    verificationCode = intent.value.filter(Char::isDigit).take(6),
                    verificationCodeErrorMessage = null,
                )
            }

            SignupCodeVerificationIntent.Tick -> updateState {
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

            SignupCodeVerificationIntent.CodeActionClick -> codeActionClick()
            SignupCodeVerificationIntent.HideVerificationSentMessage -> updateState {
                copy(showVerificationSentMessage = false)
            }
        }
    }

    private fun codeActionClick() {
        if (currentState.timerSeconds <= 0 || currentState.verificationCodeErrorMessage != null) {
            resend()
        } else if (currentState.verificationCode.length == 6) {
            confirm()
        } else {
            resend()
        }
    }

    private fun resend() {
        launch {
            updateState {
                copy(
                    isSubmitting = true,
                    verificationCode = "",
                    verificationCodeErrorMessage = null,
                )
            }

            when (val result = repository.resendEmailVerification(currentState.email)) {
                is AuthResult.Success -> updateState {
                    copy(
                        isSubmitting = false,
                        timerSeconds = result.data.expiresInSeconds.toInt(),
                        showVerificationSentMessage = true,
                    )
                }

                is AuthResult.Failure -> {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(SignupCodeVerificationEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun confirm() {
        launch {
            updateState { copy(isSubmitting = true) }
            when (
                val result = repository.confirmEmailVerification(
                    email = currentState.email,
                    verificationCode = currentState.verificationCode,
                    deviceId = deviceInfoProvider.deviceId,
                    deviceName = deviceInfoProvider.deviceName,
                )
            ) {
                is AuthResult.Success -> {
                    signupDraftStore.clear()
                    updateState { copy(isSubmitting = false) }
                    sendEffect(SignupCodeVerificationEffect.NavigateToMain)
                }

                is AuthResult.Failure -> updateState {
                    copy(
                        isSubmitting = false,
                        verificationCodeErrorMessage = result.message.ifBlank {
                            VerificationCodeMismatchErrorMessage
                        },
                    )
                }
            }
        }
    }
}
