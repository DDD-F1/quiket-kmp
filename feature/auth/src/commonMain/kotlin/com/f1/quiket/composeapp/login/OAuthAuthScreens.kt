package com.f1.quiket.composeapp.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.auth.domain.model.AppleAccountLinkRequired
import com.f1.quiket.composeapp.auth.domain.model.AppleNicknameRequired
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.KakaoAccountLinkRequired
import com.f1.quiket.composeapp.auth.domain.model.KakaoNicknameRequired
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.util.runSuspendCatching
import kotlinx.coroutines.launch

data class OAuthAuthDraft(
    val signupToken: String = "",
    val suggestedNickname: String? = null,
    val linkToken: String = "",
    val linkEmail: String = "",
)

fun KakaoNicknameRequired.toOAuthDraft(): OAuthAuthDraft =
    OAuthAuthDraft(
        signupToken = signupToken,
        suggestedNickname = suggestedNickname,
    )

fun KakaoAccountLinkRequired.toOAuthDraft(): OAuthAuthDraft =
    OAuthAuthDraft(
        linkToken = linkToken,
        linkEmail = email,
    )

fun AppleNicknameRequired.toOAuthDraft(): OAuthAuthDraft =
    OAuthAuthDraft(
        signupToken = signupToken,
        suggestedNickname = suggestedNickname,
    )

fun AppleAccountLinkRequired.toOAuthDraft(): OAuthAuthDraft =
    OAuthAuthDraft(
        linkToken = linkToken,
        linkEmail = email,
    )

@Composable
fun OAuthNicknameRoute(
    draft: OAuthAuthDraft,
    providerName: String,
    onBackClick: () -> Unit,
    onComplete: (AuthTokenData) -> Unit,
    onCompleteNickname: suspend (signupToken: String, nickname: String) -> AuthTokenData,
) {
    var nickname by remember(draft.suggestedNickname) {
        mutableStateOf(draft.suggestedNickname.orEmpty().take(SignUpNicknameMaxLength))
    }
    var nicknameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
            runSuspendCatching {
                onCompleteNickname(draft.signupToken, trimmedNickname)
            }.onSuccess { tokenData ->
                onComplete(tokenData)
            }.onFailure { error ->
                nicknameErrorMessage = error.toUserFacingMessage("$providerName 닉네임 설정에 실패했습니다.")
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
fun OAuthAccountLinkRoute(
    draft: OAuthAuthDraft,
    providerName: String,
    onBackClick: () -> Unit,
    onComplete: (AuthTokenData) -> Unit,
    onLinkAccount: suspend (linkToken: String, email: String, password: String) -> AuthTokenData,
) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun submit() {
        if (password.isBlank() || isSubmitting) return

        isSubmitting = true
        passwordErrorMessage = null
        coroutineScope.launch {
            runSuspendCatching {
                onLinkAccount(draft.linkToken, draft.linkEmail, password)
            }.onSuccess { tokenData ->
                onComplete(tokenData)
            }.onFailure { error ->
                passwordErrorMessage = error.toUserFacingMessage("$providerName 계정 연결에 실패했습니다.")
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
