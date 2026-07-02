package com.f1.quiket.feature.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpNicknameRoute(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    viewModel: SignupNicknameViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SignupNicknameEffect.NavigateNext -> onNextClick()
            }
        }
    }

    SignUpNicknameScreen(
        nickname = state.nickname,
        nicknameErrorMessage = state.nicknameErrorMessage,
        isNextEnabled = state.isNextEnabled,
        onNicknameChange = { viewModel.onIntent(SignupNicknameIntent.NicknameChanged(it)) },
        onBackClick = onBackClick,
        onNextClick = { viewModel.onIntent(SignupNicknameIntent.Next) },
    )
}
