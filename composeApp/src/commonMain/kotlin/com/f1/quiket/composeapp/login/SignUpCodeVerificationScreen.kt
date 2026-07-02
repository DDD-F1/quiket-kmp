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
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.network.withNetworkRetryGuide
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val VerificationSentMessageHideDelayMillis = 2_500L

@Composable
internal fun SignUpCodeVerificationRoute(
    draft: SignupDraft,
    onBackClick: () -> Unit,
    onVerificationComplete: (AuthTokenData) -> Unit,
    modifier: Modifier = Modifier,
) {
    var verificationCode by remember(draft.email) { mutableStateOf("") }
    var timerSeconds by remember(draft.email) { mutableStateOf(VerificationTimeoutSeconds) }
    var isSubmitting by remember { mutableStateOf(false) }
    var verificationCodeErrorMessage by remember { mutableStateOf<String?>(null) }
    var showVerificationSentMessage by remember { mutableStateOf(true) }
    var noticeMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val authStateHolder = koinInject<AuthStateHolder>()

    fun resend() {
        isSubmitting = true
        verificationCode = ""
        verificationCodeErrorMessage = null
        noticeMessage = null
        coroutineScope.launch {
            runCatching {
                authStateHolder.resendEmailVerification(draft.email)
            }.onSuccess { result ->
                timerSeconds = result.expiresInSeconds.toInt()
                showVerificationSentMessage = true
            }.onFailure { error ->
                noticeMessage = error.toUserFacingMessage("인증번호 재요청에 실패했습니다.")
            }
            isSubmitting = false
        }
    }

    fun confirm() {
        isSubmitting = true
        verificationCodeErrorMessage = null
        noticeMessage = null
        coroutineScope.launch {
            runCatching {
                authStateHolder.confirmEmailVerification(
                    email = draft.email,
                    verificationCode = verificationCode,
                )
            }.onSuccess { tokenData ->
                onVerificationComplete(tokenData)
            }.onFailure { error ->
                verificationCodeErrorMessage = error.toUserFacingMessage(
                    fallback = VerificationCodeMismatchErrorMessage,
                    networkMessage = "인증번호 확인에 실패했습니다.".withNetworkRetryGuide(),
                )
            }
            isSubmitting = false
        }
    }

    fun codeActionClick() {
        when {
            timerSeconds <= 0 || verificationCodeErrorMessage != null -> resend()
            verificationCode.length == 6 -> confirm()
            else -> resend()
        }
    }

    LaunchedEffect(timerSeconds, isSubmitting) {
        if (timerSeconds <= 0 || isSubmitting) return@LaunchedEffect

        delay(1_000L)
        val nextSeconds = (timerSeconds - 1).coerceAtLeast(0)
        timerSeconds = nextSeconds
        if (nextSeconds == 0) {
            verificationCodeErrorMessage = VerificationExpiredErrorMessage
        }
    }

    LaunchedEffect(showVerificationSentMessage) {
        if (showVerificationSentMessage) {
            delay(VerificationSentMessageHideDelayMillis)
            showVerificationSentMessage = false
        }
    }

    SignUpCodeVerificationScreen(
        email = draft.email,
        verificationCode = verificationCode,
        timerText = timerSeconds.toTimerText(),
        isSubmitting = isSubmitting,
        verificationCodeErrorMessage = verificationCodeErrorMessage,
        showVerificationSentMessage = showVerificationSentMessage,
        noticeMessage = noticeMessage,
        onVerificationCodeChange = {
            verificationCode = it.filter(Char::isDigit).take(6)
            verificationCodeErrorMessage = null
        },
        onCodeActionClick = ::codeActionClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun SignUpCodeVerificationScreen(
    email: String,
    verificationCode: String,
    timerText: String,
    isSubmitting: Boolean,
    verificationCodeErrorMessage: String?,
    showVerificationSentMessage: Boolean,
    noticeMessage: String?,
    onVerificationCodeChange: (String) -> Unit,
    onCodeActionClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasVerificationCodeError = !verificationCodeErrorMessage.isNullOrBlank()
    val canSubmit = verificationCode.length == 6 && !isSubmitting

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 124.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SignUpPageIndicator(currentPage = 2, pageCount = 3)
            }
            SignUpCodeTitleSection()
            ReadOnlyEmailBlock(email = email)
            VerificationCodeBlock(
                verificationCode = verificationCode,
                timerText = timerText,
                verificationCodeErrorMessage = verificationCodeErrorMessage,
                onVerificationCodeChange = onVerificationCodeChange,
                onCodeActionClick = onCodeActionClick,
                buttonText = if (!hasVerificationCodeError && verificationCode.length == 6) "확인" else "재요청",
                buttonEnabled = !isSubmitting,
            )
            if (showVerificationSentMessage) {
                Text(
                    text = "이메일에 있는 인증번호 6자리를 확인해주세요.",
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
            if (!noticeMessage.isNullOrBlank()) {
                Text(
                    text = noticeMessage,
                    color = QuiketGray700,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }

        SignUpTopBar(
            title = "회원가입",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        QuiketPrimaryButton(
            text = if (isSubmitting) "확인 중..." else "다음",
            enabled = canSubmit,
            onClick = onCodeActionClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )
    }
}

@Composable
private fun SignUpCodeTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "이메일을 인증해주세요",
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "메일로 받은 인증번호 6자리를 입력해주세요",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun ReadOnlyEmailBlock(
    email: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "이메일",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(QuiketGray100, MaterialTheme.shapes.medium)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = email,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun VerificationCodeBlock(
    verificationCode: String,
    timerText: String,
    verificationCodeErrorMessage: String?,
    buttonText: String,
    buttonEnabled: Boolean,
    onVerificationCodeChange: (String) -> Unit,
    onCodeActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "인증번호",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            QuiketTextField(
                value = verificationCode,
                onValueChange = onVerificationCodeChange,
                hint = "6자리를 입력해주세요",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !verificationCodeErrorMessage.isNullOrBlank(),
                errorMessage = verificationCodeErrorMessage,
                trailingIcon = {
                    Text(
                        text = timerText,
                        color = QuiketGray700,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                modifier = Modifier.weight(1f),
            )
            VerificationActionButton(
                text = buttonText,
                enabled = buttonEnabled,
                onClick = onCodeActionClick,
                modifier = Modifier.width(84.dp),
            )
        }
    }
}

@Composable
private fun VerificationActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        contentPadding = ButtonDefaults.ContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = QuiketBrown950,
            contentColor = QuiketWhite,
            disabledContainerColor = QuiketGray100,
            disabledContentColor = QuiketGray300,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
    }
}
