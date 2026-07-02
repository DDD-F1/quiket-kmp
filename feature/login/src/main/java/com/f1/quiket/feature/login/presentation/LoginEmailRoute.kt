package com.f1.quiket.feature.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginEmailRoute(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onEmailVerificationRequired: () -> Unit = {},
    viewModel: LoginEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginEmailEffect.NavigateToMain -> onLoginSuccess()
                is LoginEmailEffect.NavigateToPasswordReset -> onForgotPasswordClick()
                is LoginEmailEffect.NavigateToEmailVerification -> onEmailVerificationRequired()
                is LoginEmailEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LoginEmailScreen(
        email = state.email,
        password = state.password,
        isPasswordVisible = state.isPasswordVisible,
        isLoginEnabled = state.isLoginEnabled,
        emailErrorMessage = state.emailErrorMessage,
        passwordErrorMessage = state.passwordErrorMessage,
        showPasswordResetRequiredDialog = state.showPasswordResetRequiredDialog,
        onEmailChange = { viewModel.onIntent(LoginEmailIntent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onIntent(LoginEmailIntent.PasswordChanged(it)) },
        onPasswordVisibilityClick = { viewModel.onIntent(LoginEmailIntent.TogglePasswordVisibility) },
        onBackClick = onBackClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onLoginClick = { viewModel.onIntent(LoginEmailIntent.Login) },
        onPasswordResetRequiredClick = {
            viewModel.onIntent(LoginEmailIntent.PasswordResetRequiredClick)
        },
        onPasswordResetRequiredDismiss = {
            viewModel.onIntent(LoginEmailIntent.PasswordResetRequiredDismiss)
        },
    )
}
