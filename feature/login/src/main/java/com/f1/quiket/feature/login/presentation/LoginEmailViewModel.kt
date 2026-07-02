package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.auth.DeviceInfoProvider
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginEmailViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val passwordResetDraftStore: PasswordResetDraftStore,
    private val signupDraftStore: SignupDraftStore,
) : MviViewModel<LoginEmailState, LoginEmailIntent, LoginEmailEffect>(
    initialState = LoginEmailState(),
) {
    override fun handleIntent(intent: LoginEmailIntent) {
        when (intent) {
            is LoginEmailIntent.EmailChanged -> updateState {
                copy(
                    email = intent.value,
                    emailErrorMessage = null,
                    passwordErrorMessage = null,
                )
            }

            is LoginEmailIntent.PasswordChanged -> updateState {
                copy(
                    password = intent.value,
                    passwordErrorMessage = null,
                )
            }

            LoginEmailIntent.TogglePasswordVisibility -> updateState {
                copy(isPasswordVisible = !isPasswordVisible)
            }

            LoginEmailIntent.Login -> login()
            LoginEmailIntent.PasswordResetRequiredClick -> navigateToPasswordReset()
            LoginEmailIntent.PasswordResetRequiredDismiss -> updateState {
                copy(showPasswordResetRequiredDialog = false)
            }
        }
    }

    private fun login() {
        val trimmedEmail = currentState.email.trim()
        if (!isValidEmail(trimmedEmail)) {
            updateState { copy(emailErrorMessage = EmailFormatErrorMessage) }
            return
        }

        launch {
            updateState {
                copy(
                    isLoading = true,
                    emailErrorMessage = null,
                    passwordErrorMessage = null,
                )
            }

            when (
                val result = repository.login(
                    email = trimmedEmail,
                    password = currentState.password,
                    deviceId = deviceInfoProvider.deviceId,
                    deviceName = deviceInfoProvider.deviceName,
                )
            ) {
                is AuthResult.Success -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(LoginEmailEffect.NavigateToMain)
                }

                is AuthResult.Failure -> handleLoginFailure(trimmedEmail, result)
            }
        }
    }

    private suspend fun handleLoginFailure(email: String, failure: AuthResult.Failure) {
        updateState { copy(isLoading = false) }
        when (failure.code) {
            "AUTH_INVALID_CREDENTIALS", "AUTH_LOGIN_FAILED" -> {
                val failedCount = failure.failedLoginCount
                val message = if (failedCount != null) {
                    "비밀번호를 다시 입력해주세요 ($failedCount/5)"
                } else {
                    failure.message
                }
                updateState { copy(passwordErrorMessage = message) }
            }

            "AUTH_ACCOUNT_LOCKED" -> {
                passwordResetDraftStore.update {
                    copy(
                        email = failure.email ?: email,
                        verificationCode = "",
                        resetCodeSent = failure.resetCodeSent == true,
                    )
                }
                updateState {
                    copy(
                        showPasswordResetRequiredDialog = true,
                        resetRequiredEmail = failure.email ?: email,
                    )
                }
            }

            "AUTH_EMAIL_NOT_VERIFIED" -> {
                val verificationEmail = failure.email ?: email
                signupDraftStore.update { copy(email = verificationEmail) }
                when (val resendResult = repository.resendEmailVerification(verificationEmail)) {
                    is AuthResult.Success -> {
                        sendEffect(LoginEmailEffect.NavigateToEmailVerification(verificationEmail))
                    }

                    is AuthResult.Failure -> {
                        sendEffect(LoginEmailEffect.ShowMessage(resendResult.message))
                        sendEffect(LoginEmailEffect.NavigateToEmailVerification(verificationEmail))
                    }
                }
            }

            else -> sendEffect(LoginEmailEffect.ShowMessage(failure.message))
        }
    }

    private fun navigateToPasswordReset() {
        val email = currentState.resetRequiredEmail.ifBlank { currentState.email.trim() }
        passwordResetDraftStore.update {
            copy(email = email, verificationCode = "")
        }
        updateState { copy(showPasswordResetRequiredDialog = false) }
        launch {
            sendEffect(LoginEmailEffect.NavigateToPasswordReset(email))
        }
    }
}
