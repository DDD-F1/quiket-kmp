package com.f1.quiket.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

@Composable
fun PasswordResetEmailVerificationScreen(
    email: String,
    verificationCode: String,
    timerText: String,
    isEmailVerificationRequested: Boolean,
    isEmailVerified: Boolean,
    isEmailVerificationButtonEnabled: Boolean,
    emailErrorMessage: String?,
    verificationCodeErrorMessage: String?,
    isCodeVerifying: Boolean,
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
            .background(White),
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
                .padding(top = 92.dp),
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
                buttonText = "이메일 인증",
                buttonEnabled = isEmailVerificationButtonEnabled,
                onButtonClick = onEmailVerificationRequestClick,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = !emailErrorMessage.isNullOrBlank(),
                errorMessage = emailErrorMessage,
                trailingIcon = if (isEmailVerified) {
                    { PasswordResetSuccessIcon() }
                } else {
                    null
                },
            )
            if (isEmailVerificationRequested) {
                val hasVerificationCodeError = !verificationCodeErrorMessage.isNullOrBlank()
                PasswordResetInputWithAction(
                    title = "인증번호",
                    value = verificationCode,
                    onValueChange = onVerificationCodeChange,
                    hint = "6자리를 입력해주세요",
                    buttonText = if (!hasVerificationCodeError && (verificationCode.length == 6 || isCodeVerifying)) {
                        "확인"
                    } else {
                        "재요청"
                    },
                    buttonEnabled = !isCodeVerifying,
                    onButtonClick = onCodeActionClick,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hasVerificationCodeError,
                    errorMessage = verificationCodeErrorMessage,
                    trailingIcon = if (hasVerificationCodeError) {
                        { PasswordResetErrorIcon() }
                    } else {
                        {
                            Text(
                                text = timerText,
                                color = Gray700,
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
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
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = description,
            color = Gray700,
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
                color = Gray950,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
            BaseTextField(
                value = value,
                onValueChange = onValueChange,
                hint = hint,
                keyboardOptions = keyboardOptions,
                isError = isError,
                errorMessage = errorMessage,
                trailingIcon = trailingIcon,
            )
        }
        QuiketPrimaryButton(
            text = buttonText,
            enabled = buttonEnabled,
            fillMaxWidth = false,
            onClick = onButtonClick,
            modifier = Modifier
                .padding(top = 35.dp)
                .width(97.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetEmailVerificationEmptyPreview() {
    QuiketTheme {
        PasswordResetEmailVerificationScreen(
            email = "",
            verificationCode = "",
            timerText = "03:00",
            isEmailVerificationRequested = false,
            isEmailVerified = false,
            isEmailVerificationButtonEnabled = false,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            isCodeVerifying = false,
            showVerificationSentMessage = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetEmailVerificationCodePreview() {
    QuiketTheme {
        PasswordResetEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "",
            timerText = "02:59",
            isEmailVerificationRequested = true,
            isEmailVerified = false,
            isEmailVerificationButtonEnabled = true,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            isCodeVerifying = false,
            showVerificationSentMessage = true,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetEmailVerificationVerifiedPreview() {
    QuiketTheme {
        PasswordResetEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "123456",
            timerText = "02:59",
            isEmailVerificationRequested = true,
            isEmailVerified = true,
            isEmailVerificationButtonEnabled = true,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            isCodeVerifying = false,
            showVerificationSentMessage = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetEmailVerificationMismatchPreview() {
    QuiketTheme {
        PasswordResetEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "123456",
            timerText = "02:59",
            isEmailVerificationRequested = true,
            isEmailVerified = false,
            isEmailVerificationButtonEnabled = true,
            emailErrorMessage = null,
            verificationCodeErrorMessage = "인증번호가 일치하지 않아요",
            isCodeVerifying = false,
            showVerificationSentMessage = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetEmailVerificationExpiredPreview() {
    QuiketTheme {
        PasswordResetEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "",
            timerText = "00:00",
            isEmailVerificationRequested = true,
            isEmailVerified = false,
            isEmailVerificationButtonEnabled = true,
            emailErrorMessage = null,
            verificationCodeErrorMessage = "인증 시간이 만료되었어요",
            isCodeVerifying = false,
            showVerificationSentMessage = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onCloseClick = {},
        )
    }
}
