package com.f1.quiket.feature.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpEmailVerificationRoute(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    viewModel: SignupCredentialsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SignupCredentialsEffect.NavigateNext -> onNextClick()
                is SignupCredentialsEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LaunchedEffect(state.showEmailAvailableMessage) {
        if (state.showEmailAvailableMessage) {
            kotlinx.coroutines.delay(2_500L)
            viewModel.onIntent(SignupCredentialsIntent.HideEmailAvailableMessage)
        }
    }

    SignUpEmailVerificationScreen(
        email = state.email,
        verificationCode = "",
        password = state.password,
        passwordConfirm = state.passwordConfirm,
        timerText = VerificationTimeoutSeconds.toTimerText(),
        isEmailVerificationRequested = false,
        isEmailVerified = state.isEmailAvailable,
        isPasswordVisible = state.isPasswordVisible,
        isPasswordConfirmVisible = state.isPasswordConfirmVisible,
        isEmailVerificationButtonEnabled = state.email.isNotBlank() && !state.isCheckingEmail,
        emailErrorMessage = state.emailErrorMessage,
        verificationCodeErrorMessage = null,
        passwordConfirmErrorMessage = state.passwordConfirmErrorMessage ?: state.passwordErrorMessage,
        showVerificationSentMessage = false,
        showEmailVerifiedMessage = state.showEmailAvailableMessage,
        isNextEnabled = state.isNextEnabled,
        emailActionButtonText = if (state.isEmailAvailable) "확인 완료" else "이메일 확인",
        showVerificationFields = false,
        showPasswordFields = true,
        onEmailChange = { viewModel.onIntent(SignupCredentialsIntent.EmailChanged(it)) },
        onVerificationCodeChange = {},
        onPasswordChange = { viewModel.onIntent(SignupCredentialsIntent.PasswordChanged(it)) },
        onPasswordConfirmChange = { viewModel.onIntent(SignupCredentialsIntent.PasswordConfirmChanged(it)) },
        onEmailVerificationRequestClick = {
            viewModel.onIntent(SignupCredentialsIntent.CheckEmailAvailability)
        },
        onCodeActionClick = {},
        onPasswordVisibilityClick = {
            viewModel.onIntent(SignupCredentialsIntent.TogglePasswordVisibility)
        },
        onPasswordConfirmVisibilityClick = {
            viewModel.onIntent(SignupCredentialsIntent.TogglePasswordConfirmVisibility)
        },
        onBackClick = onBackClick,
        onNextClick = { viewModel.onIntent(SignupCredentialsIntent.Next) },
    )
}
