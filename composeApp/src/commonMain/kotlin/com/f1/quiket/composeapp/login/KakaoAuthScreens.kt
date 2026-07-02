package com.f1.quiket.composeapp.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.KakaoAccountLinkRequired
import com.f1.quiket.composeapp.auth.domain.model.KakaoNicknameRequired
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.network.toUserFacingMessage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

internal data class KakaoAuthDraft(
    val signupToken: String = "",
    val suggestedNickname: String? = null,
    val linkToken: String = "",
    val linkEmail: String = "",
)

internal fun KakaoNicknameRequired.toDraft(): KakaoAuthDraft =
    KakaoAuthDraft(
        signupToken = signupToken,
        suggestedNickname = suggestedNickname,
    )

internal fun KakaoAccountLinkRequired.toDraft(): KakaoAuthDraft =
    KakaoAuthDraft(
        linkToken = linkToken,
        linkEmail = email,
    )

@Composable
internal fun KakaoNicknameRoute(
    draft: KakaoAuthDraft,
    onBackClick: () -> Unit,
    onComplete: (AuthTokenData) -> Unit,
) {
    var nickname by remember(draft.suggestedNickname) {
        mutableStateOf(draft.suggestedNickname.orEmpty().take(SignUpNicknameMaxLength))
    }
    var nicknameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authStateHolder = koinInject<AuthStateHolder>()

    fun updateNickname(nextValue: String) {
        val next = nextValue.take(SignUpNicknameMaxLength)
        nickname = next
        nicknameErrorMessage = if (next.isNotBlank() && !isValidNickname(next.trim())) {
            NicknameFormatErrorMessage
        } else {
            null
        }
    }

    fun submit() {
        val trimmedNickname = nickname.trim()
        if (!isValidNickname(trimmedNickname)) {
            nicknameErrorMessage = NicknameFormatErrorMessage
            return
        }
        if (isSubmitting) return

        isSubmitting = true
        nicknameErrorMessage = null
        coroutineScope.launch {
            runCatching {
                authStateHolder.completeKakaoNickname(
                    signupToken = draft.signupToken,
                    nickname = trimmedNickname,
                )
            }.onSuccess { tokenData ->
                onComplete(tokenData)
            }.onFailure { error ->
                nicknameErrorMessage = error.toUserFacingMessage("카카오 닉네임 설정에 실패했습니다.")
            }
            isSubmitting = false
        }
    }

    SignUpNicknameScreen(
        nickname = nickname,
        nicknameErrorMessage = nicknameErrorMessage,
        isNextEnabled = isValidNickname(nickname.trim()) && nicknameErrorMessage == null && !isSubmitting,
        onNicknameChange = ::updateNickname,
        onBackClick = onBackClick,
        onNextClick = ::submit,
    )
}

@Composable
internal fun KakaoAccountLinkRoute(
    draft: KakaoAuthDraft,
    onBackClick: () -> Unit,
    onComplete: (AuthTokenData) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val authStateHolder = koinInject<AuthStateHolder>()

    fun submit() {
        if (password.isBlank() || isSubmitting) return

        isSubmitting = true
        passwordErrorMessage = null
        coroutineScope.launch {
            runCatching {
                authStateHolder.linkKakaoAccount(
                    linkToken = draft.linkToken,
                    email = draft.linkEmail,
                    password = password,
                )
            }.onSuccess { tokenData ->
                onComplete(tokenData)
            }.onFailure { error ->
                passwordErrorMessage = error.toUserFacingMessage("카카오 계정 연결에 실패했습니다.")
            }
            isSubmitting = false
        }
    }

    LoginEmailScreen(
        email = draft.linkEmail,
        password = password,
        isPasswordVisible = isPasswordVisible,
        isLoginEnabled = draft.linkEmail.isNotBlank() && password.isNotBlank() && !isSubmitting,
        onEmailChange = {},
        onPasswordChange = {
            password = it
            passwordErrorMessage = null
        },
        onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
        onBackClick = onBackClick,
        onForgotPasswordClick = {},
        onLoginClick = ::submit,
        passwordErrorMessage = passwordErrorMessage,
        title = "계정 연결",
        buttonText = if (isSubmitting) "연결 중..." else "연결하기",
        isEmailEnabled = false,
        showForgotPassword = false,
    )
}
