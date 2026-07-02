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
fun PasswordResetNewPasswordRoute(
    onCloseClick: () -> Unit,
    onCompleteClick: () -> Unit,
    viewModel: PasswordResetNewPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                PasswordResetNewPasswordEffect.NavigateToLogin -> onCompleteClick()
                is PasswordResetNewPasswordEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    LaunchedEffect(state.showEmailVerifiedMessage) {
        if (!state.showEmailVerifiedMessage) return@LaunchedEffect

        delay(2_500L)
        viewModel.onIntent(PasswordResetNewPasswordIntent.HideEmailVerifiedMessage)
    }

    PasswordResetNewPasswordScreen(
        password = state.password,
        passwordConfirm = state.passwordConfirm,
        isPasswordVisible = state.isPasswordVisible,
        isPasswordConfirmVisible = state.isPasswordConfirmVisible,
        passwordConfirmErrorMessage = state.passwordConfirmErrorMessage ?: state.passwordErrorMessage,
        isCompleteEnabled = state.isCompleteEnabled,
        showEmailVerifiedMessage = state.showEmailVerifiedMessage,
        onPasswordChange = { viewModel.onIntent(PasswordResetNewPasswordIntent.PasswordChanged(it)) },
        onPasswordConfirmChange = { viewModel.onIntent(PasswordResetNewPasswordIntent.PasswordConfirmChanged(it)) },
        onPasswordVisibilityClick = { viewModel.onIntent(PasswordResetNewPasswordIntent.TogglePasswordVisibility) },
        onPasswordConfirmVisibilityClick = {
            viewModel.onIntent(PasswordResetNewPasswordIntent.TogglePasswordConfirmVisibility)
        },
        onCloseClick = onCloseClick,
        onCompleteClick = { viewModel.onIntent(PasswordResetNewPasswordIntent.Submit) },
    )
}
