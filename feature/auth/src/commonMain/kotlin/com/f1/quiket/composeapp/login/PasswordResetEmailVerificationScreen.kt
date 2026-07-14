package com.f1.quiket.composeapp.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.util.runSuspendCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val PasswordResetVerificationSentHideDelayMillis = 2_500L

@Composable
fun PasswordResetEmailVerificationRoute(
    draft: PasswordResetDraft,
    onCloseClick: () -> Unit,
    onVerificationComplete: (PasswordResetDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember(draft.email) { mutableStateOf(draft.email) }
    var verificationCode by remember(draft.email) { mutableStateOf(draft.verificationCode) }
    var timerSeconds by remember(draft.email) { mutableStateOf(VerificationTimeoutSeconds) }
    var isVerificationRequested by remember(draft.email) { mutableStateOf(draft.resetCodeSent) }
    var isSubmitting by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf<String?>(null) }
    var verificationCodeErrorMessage by remember { mutableStateOf<String?>(null) }
    var showVerificationSentMessage by remember(draft.email) { mutableStateOf(draft.resetCodeSent) }
    val coroutineScope = rememberCoroutineScope()
    val authStateHolder = koinInject<AuthStateHolder>()

    fun requestCode() {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            emailErrorMessage = EmailFormatErrorMessage
            return
        }

        isSubmitting = true
        emailErrorMessage = null
        verificationCodeErrorMessage = null
        coroutineScope.launch {
            runSuspendCatching {
                authStateHolder.requestPasswordReset(trimmedEmail)
            }.onSuccess { result ->
                email = result.email
                verificationCode = ""
                timerSeconds = result.expiresInSeconds.toInt()
                isVerificationRequested = true
                showVerificationSentMessage = true
            }.onFailure { error ->
                isVerificationRequested = false
                emailErrorMessage = error.toUserFacingMessage("비밀번호 재설정 요청에 실패했습니다.")
            }
            isSubmitting = false
        }
    }

    fun codeActionClick() {
        if (verificationCodeErrorMessage != null || verificationCode.length < 6) {
            requestCode()
            return
        }
        if (timerSeconds <= 0) {
            verificationCodeErrorMessage = VerificationExpiredErrorMessage
            return
        }

        onVerificationComplete(
            draft.copy(
                email = email.trim(),
                verificationCode = verificationCode,
                resetCodeSent = true,
            ),
        )
    }

    LaunchedEffect(timerSeconds, isVerificationRequested, isSubmitting) {
        if (!isVerificationRequested || timerSeconds <= 0 || isSubmitting) return@LaunchedEffect

        delay(1_000L)
        val nextSeconds = (timerSeconds - 1).coerceAtLeast(0)
        timerSeconds = nextSeconds
        if (nextSeconds == 0) {
            verificationCodeErrorMessage = VerificationExpiredErrorMessage
        }
    }

    LaunchedEffect(showVerificationSentMessage) {
        if (showVerificationSentMessage) {
            delay(PasswordResetVerificationSentHideDelayMillis)
            showVerificationSentMessage = false
        }
    }

    PasswordResetEmailVerificationScreen(
        email = email,
        verificationCode = verificationCode,
        timerText = timerSeconds.toTimerText(),
        isVerificationRequested = isVerificationRequested,
        isSubmitting = isSubmitting,
        emailErrorMessage = emailErrorMessage,
        verificationCodeErrorMessage = verificationCodeErrorMessage,
        showVerificationSentMessage = showVerificationSentMessage,
        onEmailChange = {
            email = it
            verificationCode = ""
            isVerificationRequested = false
            emailErrorMessage = null
            verificationCodeErrorMessage = null
        },
        onVerificationCodeChange = {
            verificationCode = it.filter(Char::isDigit).take(6)
            verificationCodeErrorMessage = null
        },
        onEmailVerificationRequestClick = ::requestCode,
        onCodeActionClick = ::codeActionClick,
        onCloseClick = onCloseClick,
        modifier = modifier,
    )
}

@Composable
private fun PasswordResetEmailVerificationScreen(
    email: String,
    verificationCode: String,
    timerText: String,
    isVerificationRequested: Boolean,
    isSubmitting: Boolean,
    emailErrorMessage: String?,
    verificationCodeErrorMessage: String?,
    showVerificationSentMessage: Boolean,
    onEmailChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onEmailVerificationRequestClick: () -> Unit,
    onCodeActionClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        PasswordResetTopBar(
            title = "비밀번호 재설정",
            onCloseClick = onCloseClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 124.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PasswordResetPageIndicator(currentPage = 0)
            }
            PasswordResetTitleSection(
                title = "이메일 인증을 진행할게요",
                description = "가입하신 이메일을 입력해주세요",
            )
            PasswordResetInputWithAction(
                title = "이메일",
                value = email,
                onValueChange = onEmailChange,
                hint = "이메일을 입력해주세요",
                buttonText = if (isSubmitting) "요청 중" else "이메일 인증",
                buttonEnabled = email.isNotBlank() && !isSubmitting,
                onButtonClick = onEmailVerificationRequestClick,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = !emailErrorMessage.isNullOrBlank(),
                errorMessage = emailErrorMessage,
            )
            if (isVerificationRequested) {
                val hasVerificationCodeError = !verificationCodeErrorMessage.isNullOrBlank()
                PasswordResetInputWithAction(
                    title = "인증번호",
                    value = verificationCode,
                    onValueChange = onVerificationCodeChange,
                    hint = "6자리를 입력해주세요",
                    buttonText = if (!hasVerificationCodeError && verificationCode.length == 6) {
                        "확인"
                    } else {
                        "재요청"
                    },
                    buttonEnabled = !isSubmitting,
                    onButtonClick = onCodeActionClick,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hasVerificationCodeError,
                    errorMessage = verificationCodeErrorMessage,
                    trailingIcon = {
                        Text(
                            text = timerText,
                            color = QuiketGray700,
                            textAlign = TextAlign.Right,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }

        if (showVerificationSentMessage) {
            PasswordResetToast(
                message = "이메일에 있는 인증번호 6자리를 확인해주세요.",
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
private fun PasswordResetTitleSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = description,
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun PasswordResetInputWithAction(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    buttonText: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
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
                keyboardOptions = keyboardOptions,
                isError = isError,
                errorMessage = errorMessage,
                trailingIcon = trailingIcon,
            )
        }
        PasswordResetActionButton(
            text = buttonText,
            enabled = buttonEnabled,
            onClick = onButtonClick,
            modifier = Modifier
                .padding(top = 35.dp)
                .width(108.dp),
        )
    }
}

@Composable
private fun PasswordResetActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = QuiketBrown950,
            contentColor = QuiketWhite,
            disabledContainerColor = QuiketGray100,
            disabledContentColor = QuiketGray300,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
    }
}
