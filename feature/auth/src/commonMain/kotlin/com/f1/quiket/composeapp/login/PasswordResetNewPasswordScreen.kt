package com.f1.quiket.composeapp.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.util.runSuspendCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val PasswordResetPasswordGuide = "8글자 이상의 영문/숫자/특수문자 조합"
private const val PasswordResetEmailVerifiedHideDelayMillis = 2_500L

@Composable
fun PasswordResetNewPasswordRoute(
    draft: PasswordResetDraft,
    onCloseClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordConfirmVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordConfirmErrorMessage by remember { mutableStateOf<String?>(null) }
    var submitErrorMessage by remember { mutableStateOf<String?>(null) }
    var showEmailVerifiedMessage by remember { mutableStateOf(draft.verificationCode.isNotBlank()) }
    val coroutineScope = rememberCoroutineScope()
    val authStateHolder = koinInject<AuthStateHolder>()

    fun updatePasswordConfirmError(nextPassword: String, nextConfirm: String) {
        passwordConfirmErrorMessage = if (nextConfirm.isNotBlank() && nextConfirm != nextPassword) {
            PasswordConfirmMismatchMessage
        } else {
            null
        }
    }

    fun submit() {
        if (draft.email.isBlank() || draft.verificationCode.isBlank()) {
            submitErrorMessage = "이메일 인증을 먼저 진행해주세요"
            return
        }
        if (!isValidAuthPassword(password)) {
            passwordErrorMessage = PasswordPolicyErrorMessage
            return
        }
        if (password != passwordConfirm) {
            passwordConfirmErrorMessage = PasswordConfirmMismatchMessage
            return
        }

        isSubmitting = true
        passwordErrorMessage = null
        passwordConfirmErrorMessage = null
        submitErrorMessage = null
        coroutineScope.launch {
            runSuspendCatching {
                authStateHolder.confirmPasswordReset(
                    email = draft.email,
                    verificationCode = draft.verificationCode,
                    newPassword = password,
                    newPasswordConfirm = passwordConfirm,
                )
            }.onSuccess {
                onCompleteClick()
            }.onFailure { error ->
                submitErrorMessage = error.toUserFacingMessage("비밀번호 재설정에 실패했습니다.")
            }
            isSubmitting = false
        }
    }

    LaunchedEffect(showEmailVerifiedMessage) {
        if (showEmailVerifiedMessage) {
            delay(PasswordResetEmailVerifiedHideDelayMillis)
            showEmailVerifiedMessage = false
        }
    }

    val isCompleteEnabled = isValidAuthPassword(password) &&
        passwordConfirm.isNotBlank() &&
        passwordConfirmErrorMessage == null &&
        !isSubmitting

    PasswordResetNewPasswordScreen(
        password = password,
        passwordConfirm = passwordConfirm,
        isPasswordVisible = isPasswordVisible,
        isPasswordConfirmVisible = isPasswordConfirmVisible,
        passwordErrorMessage = passwordErrorMessage,
        passwordConfirmErrorMessage = passwordConfirmErrorMessage ?: submitErrorMessage,
        showEmailVerifiedMessage = showEmailVerifiedMessage,
        isCompleteEnabled = isCompleteEnabled,
        buttonText = if (isSubmitting) "변경 중..." else "완료 및 로그인하기",
        onPasswordChange = {
            val nextPassword = it.filterNot(Char::isWhitespace)
            password = nextPassword
            passwordErrorMessage = null
            submitErrorMessage = null
            updatePasswordConfirmError(nextPassword, passwordConfirm)
        },
        onPasswordConfirmChange = {
            val nextPasswordConfirm = it.filterNot(Char::isWhitespace)
            passwordConfirm = nextPasswordConfirm
            submitErrorMessage = null
            updatePasswordConfirmError(password, nextPasswordConfirm)
        },
        onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
        onPasswordConfirmVisibilityClick = { isPasswordConfirmVisible = !isPasswordConfirmVisible },
        onCloseClick = onCloseClick,
        onCompleteClick = ::submit,
        modifier = modifier,
    )
}

@Composable
private fun PasswordResetNewPasswordScreen(
    password: String,
    passwordConfirm: String,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    passwordErrorMessage: String?,
    passwordConfirmErrorMessage: String?,
    showEmailVerifiedMessage: Boolean,
    isCompleteEnabled: Boolean,
    buttonText: String,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onPasswordConfirmVisibilityClick: () -> Unit,
    onCloseClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(password.isNotBlank(), passwordConfirm.isBlank()) {
        if (password.isNotBlank() && passwordConfirm.isBlank()) {
            delay(100L)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 124.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
                    .padding(bottom = 136.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PasswordResetPageIndicator(currentPage = 1)
                }

                PasswordResetNewPasswordTitleSection()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PasswordResetPasswordInput(
                        title = "새 비밀번호",
                        value = password,
                        onValueChange = onPasswordChange,
                        hint = "새 비밀번호를 입력해주세요",
                        isPasswordVisible = isPasswordVisible,
                        errorMessage = passwordErrorMessage,
                        helperMessage = PasswordResetPasswordGuide,
                        onPasswordVisibilityClick = onPasswordVisibilityClick,
                    )
                    PasswordResetPasswordInput(
                        title = "새 비밀번호 확인",
                        value = passwordConfirm,
                        onValueChange = onPasswordConfirmChange,
                        hint = "비밀번호를 한 번 더 입력해주세요",
                        isPasswordVisible = isPasswordConfirmVisible,
                        errorMessage = passwordConfirmErrorMessage,
                        onPasswordVisibilityClick = onPasswordConfirmVisibilityClick,
                    )
                }
            }

            QuiketPrimaryButton(
                text = buttonText,
                enabled = isCompleteEnabled,
                onClick = onCompleteClick,
                modifier = Modifier
                    .zIndex(1f)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            )
        }

        PasswordResetTopBar(
            title = "비밀번호 재설정",
            onCloseClick = onCloseClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (showEmailVerifiedMessage) {
            PasswordResetToast(
                message = "이메일 인증이 완료되었습니다.",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 104.dp),
            )
        }
    }
}

@Composable
private fun PasswordResetNewPasswordTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "새로운 비밀번호를 설정해주세요",
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "직전에 사용한 비밀번호는 사용할 수 없어요",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun PasswordResetPasswordInput(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    isPasswordVisible: Boolean,
    onPasswordVisibilityClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    helperMessage: String? = null,
) {
    val isError = !errorMessage.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        QuiketTextField(
            value = value,
            onValueChange = onValueChange,
            hint = hint,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = isError,
            errorMessage = errorMessage,
            trailingIcon = {
                PasswordVisibilityIcon(
                    isPasswordVisible = isPasswordVisible,
                    isError = isError,
                    onClick = onPasswordVisibilityClick,
                )
            },
        )
        if (!isError && !helperMessage.isNullOrBlank()) {
            Text(
                text = helperMessage,
                color = QuiketGray400,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
    }
}
