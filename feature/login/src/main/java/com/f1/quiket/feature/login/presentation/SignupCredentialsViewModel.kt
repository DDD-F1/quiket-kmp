package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupCredentialsViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val signupDraftStore: SignupDraftStore,
) : MviViewModel<SignupCredentialsState, SignupCredentialsIntent, SignupCredentialsEffect>(
    initialState = signupDraftStore.get().let { draft ->
        SignupCredentialsState(
            email = draft.email,
            password = draft.password,
            passwordConfirm = draft.passwordConfirm,
        )
    },
) {
    override fun handleIntent(intent: SignupCredentialsIntent) {
        when (intent) {
            is SignupCredentialsIntent.EmailChanged -> updateState {
                copy(
                    email = intent.value,
                    isEmailAvailable = false,
                    emailErrorMessage = null,
                    showEmailAvailableMessage = false,
                )
            }

            is SignupCredentialsIntent.PasswordChanged -> updateState {
                val confirmError = if (passwordConfirm.isNotBlank() && passwordConfirm != intent.value) {
                    PasswordConfirmMismatchMessage
                } else {
                    null
                }
                copy(
                    password = intent.value,
                    passwordErrorMessage = null,
                    passwordConfirmErrorMessage = confirmError,
                )
            }

            is SignupCredentialsIntent.PasswordConfirmChanged -> updateState {
                copy(
                    passwordConfirm = intent.value,
                    passwordConfirmErrorMessage = if (intent.value.isNotBlank() && intent.value != password) {
                        PasswordConfirmMismatchMessage
                    } else {
                        null
                    },
                )
            }

            SignupCredentialsIntent.CheckEmailAvailability -> checkEmailAvailability()
            SignupCredentialsIntent.TogglePasswordVisibility -> updateState {
                copy(isPasswordVisible = !isPasswordVisible)
            }

            SignupCredentialsIntent.TogglePasswordConfirmVisibility -> updateState {
                copy(isPasswordConfirmVisible = !isPasswordConfirmVisible)
            }

            SignupCredentialsIntent.Next -> submit()
            SignupCredentialsIntent.HideEmailAvailableMessage -> updateState {
                copy(showEmailAvailableMessage = false)
            }
        }
    }

    private fun checkEmailAvailability() {
        val email = currentState.email.trim()
        if (!isValidEmail(email)) {
            updateState { copy(emailErrorMessage = EmailFormatErrorMessage) }
            return
        }

        launch {
            updateState {
                copy(
                    isCheckingEmail = true,
                    emailErrorMessage = null,
                    showEmailAvailableMessage = false,
                )
            }

            when (val result = repository.checkEmailAvailability(email)) {
                is AuthResult.Success -> updateState {
                    copy(
                        isCheckingEmail = false,
                        isEmailAvailable = result.data.available,
                        emailErrorMessage = if (result.data.available) null else "이미 사용 중인 이메일입니다.",
                        showEmailAvailableMessage = result.data.available,
                    )
                }

                is AuthResult.Failure -> updateState {
                    copy(
                        isCheckingEmail = false,
                        isEmailAvailable = false,
                        emailErrorMessage = result.message,
                    )
                }
            }
        }
    }

    private fun submit() {
        val email = currentState.email.trim()
        if (!currentState.isEmailAvailable) {
            updateState { copy(emailErrorMessage = "이메일 확인을 먼저 진행해주세요") }
            return
        }
        if (!isValidAuthPassword(currentState.password)) {
            updateState { copy(passwordErrorMessage = PasswordPolicyErrorMessage) }
            return
        }
        if (currentState.password != currentState.passwordConfirm) {
            updateState { copy(passwordConfirmErrorMessage = PasswordConfirmMismatchMessage) }
            return
        }

        signupDraftStore.update {
            copy(
                email = email,
                password = currentState.password,
                passwordConfirm = currentState.passwordConfirm,
            )
        }
        launch {
            sendEffect(SignupCredentialsEffect.NavigateNext)
        }
    }
}
