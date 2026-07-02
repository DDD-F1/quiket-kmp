package com.f1.quiket.feature.login.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R as DesignSystemR
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val SignUpPasswordPolicyGuide = "8글자 이상의 영문/숫자/특수문자 조합"
private const val SignUpPasswordAutoScrollDelayMillis = 250L
private val SignUpPasswordAutoScrollTop = 208.dp
private val SignUpDefaultBottomScrollPadding = 128.dp
private val SignUpVerifiedBottomScrollPadding = 360.dp

private enum class SignUpPasswordFocusTarget {
    Password,
    PasswordConfirm,
}

@Composable
fun SignUpEmailVerificationScreen(
    email: String,
    verificationCode: String,
    password: String,
    passwordConfirm: String,
    timerText: String,
    isEmailVerificationRequested: Boolean,
    isEmailVerified: Boolean,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    isEmailVerificationButtonEnabled: Boolean,
    emailErrorMessage: String?,
    verificationCodeErrorMessage: String?,
    passwordConfirmErrorMessage: String?,
    showVerificationSentMessage: Boolean,
    showEmailVerifiedMessage: Boolean,
    isNextEnabled: Boolean,
    onEmailChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onEmailVerificationRequestClick: () -> Unit,
    onCodeActionClick: () -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onPasswordConfirmVisibilityClick: () -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    emailActionButtonText: String = if (isEmailVerified) "인증 완료" else "이메일 인증",
    isEmailReadOnly: Boolean = false,
    showVerificationFields: Boolean = isEmailVerificationRequested,
    showPasswordFields: Boolean = isEmailVerified,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val passwordAutoScrollTopPx = remember(density) {
        with(density) { SignUpPasswordAutoScrollTop.roundToPx() }
    }
    val contentBottomPadding = if (isEmailVerified) {
        SignUpVerifiedBottomScrollPadding
    } else {
        SignUpDefaultBottomScrollPadding
    }
    var focusedPasswordTarget by remember { mutableStateOf<SignUpPasswordFocusTarget?>(null) }
    var passwordAutoScrollRequest by remember { mutableStateOf(0) }
    var passwordSectionTopPx by remember { mutableStateOf<Int?>(null) }

    fun onPasswordFocusChange(target: SignUpPasswordFocusTarget, isFocused: Boolean) {
        if (isFocused) {
            focusedPasswordTarget = target
            passwordAutoScrollRequest += 1
        } else if (focusedPasswordTarget == target) {
            focusedPasswordTarget = null
        }
    }

    LaunchedEffect(passwordAutoScrollRequest, focusedPasswordTarget) {
        if (passwordAutoScrollRequest == 0 || focusedPasswordTarget == null) return@LaunchedEffect

        delay(SignUpPasswordAutoScrollDelayMillis)
        val passwordSectionTop = passwordSectionTopPx ?: return@LaunchedEffect
        val scrollDelta = passwordSectionTop - passwordAutoScrollTopPx
        if (scrollDelta > 0) {
            scrollState.animateScrollTo(
                (scrollState.value + scrollDelta).coerceIn(0, scrollState.maxValue),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(
                    top = 92.dp,
                    bottom = contentBottomPadding,
                )
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PasswordResetPageIndicator(currentPage = 0, pageCount = 3)
            }
            SignUpTitleSection()
            SignUpInputWithAction(
                title = "이메일",
                value = email,
                onValueChange = onEmailChange,
                hint = "이메일을 입력해주세요",
                buttonText = emailActionButtonText,
                buttonEnabled = isEmailVerificationButtonEnabled,
                onButtonClick = onEmailVerificationRequestClick,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                readOnly = isEmailReadOnly,
                isError = !emailErrorMessage.isNullOrBlank(),
                errorMessage = emailErrorMessage,
                trailingIcon = if (isEmailVerified) {
                    { PasswordResetSuccessIcon() }
                } else {
                    null
                },
            )
            if (showVerificationFields) {
                val hasVerificationCodeError = !verificationCodeErrorMessage.isNullOrBlank()
                SignUpInputWithAction(
                    title = "인증번호",
                    value = verificationCode,
                    onValueChange = onVerificationCodeChange,
                    hint = "6자리를 입력해주세요",
                    buttonText = if (!hasVerificationCodeError && verificationCode.length == 6) "확인" else "재요청",
                    buttonEnabled = !isEmailVerified,
                    onButtonClick = onCodeActionClick,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = isEmailVerified,
                    isError = hasVerificationCodeError,
                    errorMessage = verificationCodeErrorMessage,
                    trailingIcon = if (hasVerificationCodeError) {
                        { PasswordResetErrorIcon() }
                    } else if (isEmailVerified) {
                        null
                    } else {
                        {
                            Text(
                                text = timerText,
                                color = Gray700,
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    },
                )
            }
            if (showPasswordFields) {
                SignUpPasswordFields(
                    password = password,
                    passwordConfirm = passwordConfirm,
                    isPasswordVisible = isPasswordVisible,
                    isPasswordConfirmVisible = isPasswordConfirmVisible,
                    passwordConfirmErrorMessage = passwordConfirmErrorMessage,
                    onPasswordChange = onPasswordChange,
                    onPasswordConfirmChange = onPasswordConfirmChange,
                    onPasswordVisibilityClick = onPasswordVisibilityClick,
                    onPasswordConfirmVisibilityClick = onPasswordConfirmVisibilityClick,
                    onPasswordFocusChange = ::onPasswordFocusChange,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        passwordSectionTopPx = coordinates.boundsInRoot().top.roundToInt()
                    },
                )
            }
        }

        SignUpTopBar(
            title = "회원가입",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

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
        if (showEmailVerifiedMessage) {
            PasswordResetToast(
                message = "이메일 인증이 완료되었습니다.",
                showSuccessIcon = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 104.dp),
            )
        }

        QuiketPrimaryButton(
            text = "다음",
            enabled = isNextEnabled,
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 28.dp),
        )
    }
}

@Composable
private fun SignUpTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(White),
    ) {
        SignUpBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 26.dp),
        )
        Text(
            text = title,
            color = Gray950,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 38.dp),
        )
    }
}

@Composable
private fun SignUpBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = Gray700,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Gray700,
                start = Offset(size.width * 0.32f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SignUpTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "반가워요!",
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "가입하실 이메일과 비밀번호를 입력해주세요",
            color = Gray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SignUpInputWithAction(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    buttonText: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = Gray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            BaseTextField(
                value = value,
                onValueChange = onValueChange,
                hint = hint,
                keyboardOptions = keyboardOptions,
                readOnly = readOnly,
                isError = isError,
                errorMessage = errorMessage,
                trailingIcon = trailingIcon,
                modifier = Modifier.weight(1f),
            )
            QuiketPrimaryButton(
                text = buttonText,
                enabled = buttonEnabled,
                fillMaxWidth = false,
                onClick = onButtonClick,
                modifier = Modifier.width(97.dp),
            )
        }
    }
}

@Composable
private fun SignUpPasswordFields(
    password: String,
    passwordConfirm: String,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    passwordConfirmErrorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onPasswordConfirmVisibilityClick: () -> Unit,
    onPasswordFocusChange: (SignUpPasswordFocusTarget, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SignUpPasswordField(
            title = "비밀번호",
            value = password,
            onValueChange = onPasswordChange,
            hint = "비밀번호를 입력해주세요",
            helperMessage = SignUpPasswordPolicyGuide,
            isPasswordVisible = isPasswordVisible,
            onPasswordVisibilityClick = onPasswordVisibilityClick,
            onFocusChange = { isFocused ->
                onPasswordFocusChange(SignUpPasswordFocusTarget.Password, isFocused)
            },
        )
        if (password.isNotBlank() || passwordConfirm.isNotBlank()) {
            SignUpPasswordField(
                title = "비밀번호 확인",
                value = passwordConfirm,
                onValueChange = onPasswordConfirmChange,
                hint = "비밀번호를 한 번 더 입력해주세요",
                isPasswordVisible = isPasswordConfirmVisible,
                errorMessage = passwordConfirmErrorMessage,
                onPasswordVisibilityClick = onPasswordConfirmVisibilityClick,
                onFocusChange = { isFocused ->
                    onPasswordFocusChange(SignUpPasswordFocusTarget.PasswordConfirm, isFocused)
                },
            )
        }
    }
}

@Composable
private fun SignUpPasswordField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    isPasswordVisible: Boolean,
    onPasswordVisibilityClick: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    helperMessage: String? = null,
    errorMessage: String? = null,
) {
    val isError = !errorMessage.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation(mask = '*')
            },
            isError = isError,
            errorMessage = errorMessage,
            trailingIcon = if (isError) {
                { PasswordResetErrorIcon() }
            } else {
                {
                    SignUpPasswordVisibilityIcon(
                        isPasswordVisible = isPasswordVisible,
                        onClick = onPasswordVisibilityClick,
                    )
                }
            },
            onFocusChanged = { focusState ->
                onFocusChange(focusState.isFocused)
            },
        )
        if (!helperMessage.isNullOrBlank()) {
            Text(
                text = helperMessage,
                color = Gray500,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

@Composable
private fun SignUpPasswordVisibilityIcon(
    isPasswordVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconRes = if (isPasswordVisible) {
        DesignSystemR.drawable.ic_password_on
    } else {
        DesignSystemR.drawable.ic_password
    }

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = if (isPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpEmailVerificationEmptyPreview() {
    QuiketTheme {
        SignUpEmailVerificationScreen(
            email = "",
            verificationCode = "",
            password = "",
            passwordConfirm = "",
            timerText = "03:00",
            isEmailVerificationRequested = false,
            isEmailVerified = false,
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            isEmailVerificationButtonEnabled = false,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            passwordConfirmErrorMessage = null,
            showVerificationSentMessage = false,
            showEmailVerifiedMessage = false,
            isNextEnabled = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpEmailVerificationEmailPreview() {
    QuiketTheme {
        SignUpEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "",
            password = "",
            passwordConfirm = "",
            timerText = "03:00",
            isEmailVerificationRequested = false,
            isEmailVerified = false,
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            isEmailVerificationButtonEnabled = true,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            passwordConfirmErrorMessage = null,
            showVerificationSentMessage = false,
            showEmailVerifiedMessage = false,
            isNextEnabled = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpEmailVerificationCodePreview() {
    QuiketTheme {
        SignUpEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "",
            password = "",
            passwordConfirm = "",
            timerText = "02:59",
            isEmailVerificationRequested = true,
            isEmailVerified = false,
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            isEmailVerificationButtonEnabled = false,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            passwordConfirmErrorMessage = null,
            showVerificationSentMessage = true,
            showEmailVerifiedMessage = false,
            isNextEnabled = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpEmailVerificationMismatchPreview() {
    QuiketTheme {
        SignUpEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "123456",
            password = "",
            passwordConfirm = "",
            timerText = "02:59",
            isEmailVerificationRequested = true,
            isEmailVerified = false,
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            isEmailVerificationButtonEnabled = false,
            emailErrorMessage = null,
            verificationCodeErrorMessage = "인증번호가 일치하지 않아요",
            passwordConfirmErrorMessage = null,
            showVerificationSentMessage = false,
            showEmailVerifiedMessage = false,
            isNextEnabled = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpEmailVerificationExpiredPreview() {
    QuiketTheme {
        SignUpEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "",
            password = "",
            passwordConfirm = "",
            timerText = "00:00",
            isEmailVerificationRequested = true,
            isEmailVerified = false,
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            isEmailVerificationButtonEnabled = false,
            emailErrorMessage = null,
            verificationCodeErrorMessage = "인증 시간이 만료되었어요",
            passwordConfirmErrorMessage = null,
            showVerificationSentMessage = false,
            showEmailVerifiedMessage = false,
            isNextEnabled = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpEmailVerificationVerifiedPreview() {
    QuiketTheme {
        SignUpEmailVerificationScreen(
            email = "aiquiz@quiket.com",
            verificationCode = "123456",
            password = "",
            passwordConfirm = "",
            timerText = "02:59",
            isEmailVerificationRequested = true,
            isEmailVerified = true,
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            isEmailVerificationButtonEnabled = false,
            emailErrorMessage = null,
            verificationCodeErrorMessage = null,
            passwordConfirmErrorMessage = null,
            showVerificationSentMessage = false,
            showEmailVerifiedMessage = true,
            isNextEnabled = false,
            onEmailChange = {},
            onVerificationCodeChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onEmailVerificationRequestClick = {},
            onCodeActionClick = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}
