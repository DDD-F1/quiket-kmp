package com.f1.quiket.feature.mypage.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.f1.quiket.core.designsystem.theme.White
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AccountSettingRoute(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: AccountSettingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showVerifyEmailDialog by remember { mutableStateOf(false) }
    var pendingEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AccountSettingEffect.GoBack -> onNavigateBack()
                AccountSettingEffect.AccountDeleted -> onAccountDeleted()
                is AccountSettingEffect.ShowMessage ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is AccountSettingEffect.NicknameUpdated ->
                    scope.launch { snackbarHostState.showSnackbar("닉네임이 변경되었습니다.") }
                is AccountSettingEffect.EmailChangeRequested -> {
                    pendingEmail = effect.email
                    showVerifyEmailDialog = true
                }
                is AccountSettingEffect.EmailChanged -> {
                    showVerifyEmailDialog = false
                    scope.launch { snackbarHostState.showSnackbar("이메일이 변경되었습니다.") }
                }
                AccountSettingEffect.PasswordChanged ->
                    scope.launch { snackbarHostState.showSnackbar("비밀번호가 변경되었습니다.") }
            }
        }
    }

    Scaffold(
        containerColor = White,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) }
        },
    ) { paddingValues ->
        AccountSettingScreen(
            state = state,
            showVerifyEmailDialog = showVerifyEmailDialog,
            pendingEmail = pendingEmail,
            onDismissVerifyEmailDialog = { showVerifyEmailDialog = false },
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(paddingValues),
        )
    }
}