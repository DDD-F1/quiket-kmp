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
fun PasswordResetEmailVerificationRoute(
    onCloseClick: () -> Unit,
    onVerificationComplete: () -> Unit,
    viewModel: PasswordResetEmailVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                PasswordResetEmailVerificationEffect.NavigateNext -> onVerificationComplete()
                is PasswordResetEmailVerificationEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LaunchedEffect(state.timerSeconds, state.isVerificationRequested, state.isSubmitting) {
        if (!state.isVerificationRequested || state.timerSeconds <= 0 || state.isSubmitting) return@LaunchedEffect

        delay(1_000L)
        viewModel.onIntent(PasswordResetEmailVerificationIntent.Tick)
    }

    LaunchedEffect(state.showVerificationSentMessage) {
        if (!state.showVerificationSentMessage) return@LaunchedEffect

        delay(2_500L)
        viewModel.onIntent(PasswordResetEmailVerificationIntent.HideVerificationSentMessage)
    }

    PasswordResetEmailVerificationScreen(
        email = state.email,
        verificationCode = state.verificationCode,
        timerText = state.timerSeconds.toTimerText(),
        isEmailVerificationRequested = state.isVerificationRequested,
        isEmailVerified = false,
        isEmailVerificationButtonEnabled = state.email.isNotBlank() && !state.isSubmitting,
        emailErrorMessage = state.emailErrorMessage,
        verificationCodeErrorMessage = state.verificationCodeErrorMessage,
        isCodeVerifying = state.isSubmitting,
        showVerificationSentMessage = state.showVerificationSentMessage,
        onEmailChange = { viewModel.onIntent(PasswordResetEmailVerificationIntent.EmailChanged(it)) },
        onVerificationCodeChange = {
            viewModel.onIntent(PasswordResetEmailVerificationIntent.VerificationCodeChanged(it))
        },
        onEmailVerificationRequestClick = {
            viewModel.onIntent(PasswordResetEmailVerificationIntent.RequestCode)
        },
        onCodeActionClick = {
            viewModel.onIntent(PasswordResetEmailVerificationIntent.CodeActionClick)
        },
        onCloseClick = onCloseClick,
    )
}
