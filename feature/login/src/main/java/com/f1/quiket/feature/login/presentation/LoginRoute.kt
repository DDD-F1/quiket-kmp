package com.f1.quiket.feature.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onEmailLoginClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onKakaoNicknameRequired: () -> Unit = {},
    onKakaoAccountLinkRequired: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateToMain -> onLoginSuccess()
                LoginEffect.NavigateToKakaoNickname -> onKakaoNicknameRequired()
                LoginEffect.NavigateToKakaoAccountLink -> onKakaoAccountLinkRequired()
                is LoginEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LoginScreen(
        onBackClick = onBackClick,
        onQuiketLoginClick = onEmailLoginClick,
        onKakaoLoginClick = {
            viewModel.onIntent(LoginIntent.KakaoLoginClick(context))
        },
        onSignUpClick = onSignUpClick,
    )
}
