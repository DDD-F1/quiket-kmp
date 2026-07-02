package com.f1.quiket.composeapp.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_password
import quiket.composeapp.generated.resources.ic_password_on
import quiket.composeapp.generated.resources.ic_password_wrong
import quiket.composeapp.generated.resources.logo_splash

@Composable
fun LoginEmailScreen(
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    isLoginEnabled: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    emailErrorMessage: String? = null,
    passwordErrorMessage: String? = null,
    title: String = "로그인",
    buttonText: String = "로그인",
    isEmailEnabled: Boolean = true,
    showForgotPassword: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        LoginEmailTopBar(
            title = title,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 144.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 136.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_splash),
                    contentDescription = "Quiket",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = 132.dp, height = 41.5.dp),
                )
                LoginEmailFields(
                    email = email,
                    password = password,
                    isPasswordVisible = isPasswordVisible,
                    emailErrorMessage = emailErrorMessage,
                    passwordErrorMessage = passwordErrorMessage,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityClick = onPasswordVisibilityClick,
                    onForgotPasswordClick = onForgotPasswordClick,
                    isLoginEnabled = isLoginEnabled,
                    onLoginClick = onLoginClick,
                    isEmailEnabled = isEmailEnabled,
                    showForgotPassword = showForgotPassword,
                )
            }

            QuiketPrimaryButton(
                text = buttonText,
                enabled = isLoginEnabled,
                onClick = onLoginClick,
                modifier = Modifier
                    .zIndex(1f)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 56.dp),
            )
        }
    }
}

@Composable
private fun LoginEmailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        BackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 50.dp),
        )
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp),
        )
    }
}

@Composable
private fun LoginEmailFields(
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    emailErrorMessage: String?,
    passwordErrorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    isLoginEnabled: Boolean,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEmailEnabled: Boolean = true,
    showForgotPassword: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoginInputField(
            title = "이메일",
            value = email,
            onValueChange = onEmailChange,
            hint = "이메일을 입력해주세요",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            isError = !emailErrorMessage.isNullOrBlank(),
            errorMessage = emailErrorMessage,
            enabled = isEmailEnabled,
        )
        LoginInputField(
            title = "비밀번호",
            value = password,
            onValueChange = onPasswordChange,
            hint = "비밀번호를 입력해주세요",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isLoginEnabled) {
                        onLoginClick()
                    }
                },
            ),
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = !passwordErrorMessage.isNullOrBlank(),
            errorMessage = passwordErrorMessage,
            trailingIcon = {
                PasswordTrailingIcon(
                    iconRes = when {
                        !passwordErrorMessage.isNullOrBlank() -> Res.drawable.ic_password_wrong
                        isPasswordVisible -> Res.drawable.ic_password_on
                        else -> Res.drawable.ic_password
                    },
                    contentDescription = if (isPasswordVisible) {
                        "비밀번호 숨기기"
                    } else {
                        "비밀번호 보기"
                    },
                    enabled = passwordErrorMessage.isNullOrBlank(),
                    onClick = onPasswordVisibilityClick,
                )
            },
        )
        if (showForgotPassword) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "비밀번호를 잊어버렸어요",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = onForgotPasswordClick,
                        ),
                )
            }
        }
    }
}

@Composable
private fun LoginInputField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable (() -> Unit))? = null,
    enabled: Boolean = true,
) {
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
            isError = isError,
            errorMessage = errorMessage,
            enabled = enabled,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun PasswordTrailingIcon(
    iconRes: DrawableResource,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = if (enabled) contentDescription else null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(24.dp)
            .then(
                if (enabled) {
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = onClick,
                        )
                } else {
                    Modifier
                },
            ),
    )
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .semantics { contentDescription = "뒤로" }
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = QuiketGray700,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray700,
                start = Offset(size.width * 0.32f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
