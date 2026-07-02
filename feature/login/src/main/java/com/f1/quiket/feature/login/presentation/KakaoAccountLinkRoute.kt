package com.f1.quiket.feature.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun KakaoAccountLinkRoute(
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    viewModel: KakaoAccountLinkViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                KakaoAccountLinkEffect.NavigateToMain -> onComplete()
                is KakaoAccountLinkEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LoginEmailScreen(
        email = state.email,
        password = state.password,
        isPasswordVisible = state.isPasswordVisible,
        isLoginEnabled = state.isSubmitEnabled,
        onEmailChange = {},
        onPasswordChange = { viewModel.onIntent(KakaoAccountLinkIntent.PasswordChanged(it)) },
        onPasswordVisibilityClick = { viewModel.onIntent(KakaoAccountLinkIntent.TogglePasswordVisibility) },
        onBackClick = onBackClick,
        onForgotPasswordClick = {},
        onLoginClick = { viewModel.onIntent(KakaoAccountLinkIntent.Submit) },
        emailErrorMessage = state.emailErrorMessage,
        passwordErrorMessage = state.passwordErrorMessage,
        title = "계정 연결",
        buttonText = "연결하기",
    )
}
