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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.network.toUserFacingMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val SignUpPasswordGuide = "8글자 이상의 영문/숫자/특수문자 조합"
private const val EmailAvailableMessageHideDelayMillis = 2_500L

@Composable
internal fun SignUpCredentialsRoute(
    draft: SignupDraft,
    onBackClick: () -> Unit,
    onNextClick: (SignupDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember(draft.email) { mutableStateOf(draft.email) }
    var password by remember(draft.password) { mutableStateOf(draft.password) }
    var passwordConfirm by remember(draft.passwordConfirm) { mutableStateOf(draft.passwordConfirm) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordConfirmVisible by remember { mutableStateOf(false) }
    var isEmailAvailable by remember(draft.email) { mutableStateOf(false) }
    var isCheckingEmail by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordConfirmErrorMessage by remember { mutableStateOf<String?>(null) }
    var showEmailAvailableMessage by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authStateHolder = koinInject<AuthStateHolder>()

    fun updatePasswordConfirmError(nextPassword: String, nextConfirm: String) {
        passwordConfirmErrorMessage = if (nextConfirm.isNotBlank() && nextConfirm != nextPassword) {
            PasswordConfirmMismatchMessage
        } else {
            null
        }
    }

    fun checkEmailAvailability() {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            emailErrorMessage = EmailFormatErrorMessage
            isEmailAvailable = false
            return
        }

        isCheckingEmail = true
        emailErrorMessage = null
        showEmailAvailableMessage = false
        coroutineScope.launch {
            runCatching {
                authStateHolder.checkEmailAvailability(trimmedEmail)
            }.onSuccess { result ->
                isEmailAvailable = result.available
                emailErrorMessage = if (result.available) null else "이미 사용 중인 이메일입니다."
                showEmailAvailableMessage = result.available
            }.onFailure { error ->
                isEmailAvailable = false
                emailErrorMessage = error.toUserFacingMessage("이메일 확인에 실패했습니다.")
            }
            isCheckingEmail = false
        }
    }

    fun submit() {
        val trimmedEmail = email.trim()
        if (!isEmailAvailable) {
            emailErrorMessage = "이메일 확인을 먼저 진행해주세요"
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

        onNextClick(
            draft.copy(
                email = trimmedEmail,
                password = password,
                passwordConfirm = passwordConfirm,
            ),
        )
    }

    LaunchedEffect(showEmailAvailableMessage) {
        if (showEmailAvailableMessage) {
            delay(EmailAvailableMessageHideDelayMillis)
            showEmailAvailableMessage = false
        }
    }

    val isNextEnabled = isEmailAvailable &&
        isValidAuthPassword(password) &&
        passwordConfirm.isNotBlank() &&
        passwordConfirmErrorMessage == null &&
        !isCheckingEmail

    SignUpCredentialsScreen(
        email = email,
        password = password,
        passwordConfirm = passwordConfirm,
        isPasswordVisible = isPasswordVisible,
        isPasswordConfirmVisible = isPasswordConfirmVisible,
        isEmailAvailable = isEmailAvailable,
        isCheckingEmail = isCheckingEmail,
        emailErrorMessage = emailErrorMessage,
        passwordErrorMessage = passwordErrorMessage,
        passwordConfirmErrorMessage = passwordConfirmErrorMessage,
        showEmailAvailableMessage = showEmailAvailableMessage,
        isNextEnabled = isNextEnabled,
        onEmailChange = {
            email = it
            isEmailAvailable = false
            emailErrorMessage = null
            showEmailAvailableMessage = false
        },
        onPasswordChange = {
            val nextPassword = it.filterNot(Char::isWhitespace)
            password = nextPassword
            passwordErrorMessage = null
            updatePasswordConfirmError(nextPassword, passwordConfirm)
        },
        onPasswordConfirmChange = {
            val nextPasswordConfirm = it.filterNot(Char::isWhitespace)
            passwordConfirm = nextPasswordConfirm
            updatePasswordConfirmError(password, nextPasswordConfirm)
        },
        onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
        onPasswordConfirmVisibilityClick = { isPasswordConfirmVisible = !isPasswordConfirmVisible },
        onEmailCheckClick = ::checkEmailAvailability,
        onBackClick = onBackClick,
        onNextClick = ::submit,
        modifier = modifier,
    )
}

@Composable
private fun SignUpCredentialsScreen(
    email: String,
    password: String,
    passwordConfirm: String,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    isEmailAvailable: Boolean,
    isCheckingEmail: Boolean,
    emailErrorMessage: String?,
    passwordErrorMessage: String?,
    passwordConfirmErrorMessage: String?,
    showEmailAvailableMessage: Boolean,
    isNextEnabled: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onPasswordConfirmVisibilityClick: () -> Unit,
    onEmailCheckClick: () -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
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
                    SignUpPageIndicator(currentPage = 0, pageCount = 3)
                }

                SignUpCredentialsTitleSection()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SignUpEmailInput(
                        email = email,
                        isEmailAvailable = isEmailAvailable,
                        isCheckingEmail = isCheckingEmail,
                        emailErrorMessage = emailErrorMessage,
                        onEmailChange = onEmailChange,
                        onEmailCheckClick = onEmailCheckClick,
                    )
                    if (showEmailAvailableMessage) {
                        Text(
                            text = "사용 가능한 이메일입니다.",
                            color = QuiketBrown950,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 2.dp),
                        )
                    }
                    SignUpPasswordInput(
                        title = "비밀번호",
                        value = password,
                        onValueChange = onPasswordChange,
                        hint = "비밀번호를 입력해주세요",
                        isPasswordVisible = isPasswordVisible,
                        errorMessage = passwordErrorMessage,
                        helperMessage = SignUpPasswordGuide,
                        onPasswordVisibilityClick = onPasswordVisibilityClick,
                    )
                    if (password.isNotBlank() || passwordConfirm.isNotBlank()) {
                        SignUpPasswordInput(
                            title = "비밀번호 확인",
                            value = passwordConfirm,
                            onValueChange = onPasswordConfirmChange,
                            hint = "비밀번호를 한 번 더 입력해주세요",
                            isPasswordVisible = isPasswordConfirmVisible,
                            errorMessage = passwordConfirmErrorMessage,
                            onPasswordVisibilityClick = onPasswordConfirmVisibilityClick,
                        )
                    }
                }
            }

            QuiketPrimaryButton(
                text = "다음",
                enabled = isNextEnabled,
                onClick = onNextClick,
                modifier = Modifier
                    .zIndex(1f)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            )
        }

        SignUpTopBar(
            title = "회원가입",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SignUpCredentialsTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "반가워요!",
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "가입하실 이메일과 비밀번호를 입력해주세요",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SignUpEmailInput(
    email: String,
    isEmailAvailable: Boolean,
    isCheckingEmail: Boolean,
    emailErrorMessage: String?,
    onEmailChange: (String) -> Unit,
    onEmailCheckClick: () -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            QuiketTextField(
                value = email,
                onValueChange = onEmailChange,
                hint = "이메일을 입력해주세요",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = !emailErrorMessage.isNullOrBlank(),
                errorMessage = emailErrorMessage,
                modifier = Modifier.weight(1f),
            )
            SignUpActionButton(
                text = when {
                    isCheckingEmail -> "확인 중"
                    isEmailAvailable -> "확인 완료"
                    else -> "이메일 확인"
                },
                enabled = email.isNotBlank() && !isCheckingEmail && !isEmailAvailable,
                onClick = onEmailCheckClick,
                modifier = Modifier.width(116.dp),
            )
        }
    }
}

@Composable
private fun SignUpPasswordInput(
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

@Composable
private fun SignUpActionButton(
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
