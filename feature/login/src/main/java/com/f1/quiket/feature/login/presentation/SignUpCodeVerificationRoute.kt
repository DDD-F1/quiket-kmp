package com.f1.quiket.feature.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpCodeVerificationRoute(
    onBackClick: () -> Unit,
    onVerificationComplete: () -> Unit,
    viewModel: SignupCodeVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SignupCodeVerificationEffect.NavigateToMain -> onVerificationComplete()
                is SignupCodeVerificationEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LaunchedEffect(state.timerSeconds, state.isSubmitting) {
        if (state.timerSeconds <= 0 || state.isSubmitting) return@LaunchedEffect

        delay(1_000L)
        viewModel.onIntent(SignupCodeVerificationIntent.Tick)
    }

    LaunchedEffect(state.showVerificationSentMessage) {
        if (!state.showVerificationSentMessage) return@LaunchedEffect

        delay(2_500L)
        viewModel.onIntent(SignupCodeVerificationIntent.HideVerificationSentMessage)
    }

    SignUpEmailVerificationScreen(
        email = state.email,
        verificationCode = state.verificationCode,
        password = "",
        passwordConfirm = "",
        timerText = state.timerSeconds.toTimerText(),
        isEmailVerificationRequested = true,
        isEmailVerified = false,
        isPasswordVisible = false,
        isPasswordConfirmVisible = false,
        isEmailVerificationButtonEnabled = false,
        emailErrorMessage = null,
        verificationCodeErrorMessage = state.verificationCodeErrorMessage,
        passwordConfirmErrorMessage = null,
        showVerificationSentMessage = state.showVerificationSentMessage,
        showEmailVerifiedMessage = false,
        isNextEnabled = state.isCodeComplete && !state.isSubmitting,
        emailActionButtonText = "발송 완료",
        isEmailReadOnly = true,
        showVerificationFields = true,
        showPasswordFields = false,
        onEmailChange = {},
        onVerificationCodeChange = { viewModel.onIntent(SignupCodeVerificationIntent.VerificationCodeChanged(it)) },
        onPasswordChange = {},
        onPasswordConfirmChange = {},
        onEmailVerificationRequestClick = {},
        onCodeActionClick = { viewModel.onIntent(SignupCodeVerificationIntent.CodeActionClick) },
        onPasswordVisibilityClick = {},
        onPasswordConfirmVisibilityClick = {},
        onBackClick = onBackClick,
        onNextClick = { viewModel.onIntent(SignupCodeVerificationIntent.CodeActionClick) },
    )
}
