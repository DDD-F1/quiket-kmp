package com.f1.quiket.feature.login.presentation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
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
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

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
    showPasswordResetRequiredDialog: Boolean = false,
    title: String = "로그인",
    buttonText: String = "로그인",
    onPasswordResetRequiredClick: () -> Unit = {},
    onPasswordResetRequiredDismiss: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        LoginEmailTopBar(
            title = title,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 144.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            Image(
                painter = painterResource(id = DesignSystemR.drawable.logo_splash),
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
            )
        }

        QuiketPrimaryButton(
            text = buttonText,
            enabled = isLoginEnabled,
            onClick = onLoginClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )

        if (showPasswordResetRequiredDialog) {
            PasswordResetRequiredOverlay(
                onPrimaryClick = onPasswordResetRequiredClick,
                onDismissClick = onPasswordResetRequiredDismiss,
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
            .height(80.dp),
    ) {
        BackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 26.dp),
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
    modifier: Modifier = Modifier,
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = !emailErrorMessage.isNullOrBlank(),
            errorMessage = emailErrorMessage,
        )
        LoginInputField(
            title = "비밀번호",
            value = password,
            onValueChange = onPasswordChange,
            hint = "비밀번호를 입력해주세요",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = !passwordErrorMessage.isNullOrBlank(),
            errorMessage = passwordErrorMessage,
            trailingIcon = {
                PasswordTrailingIcon(
                    isPasswordVisible = isPasswordVisible,
                    isError = !passwordErrorMessage.isNullOrBlank(),
                    onClick = onPasswordVisibilityClick,
                )
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "비밀번호를 잊어버렸어요",
                color = Gray600,
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
        BaseTextField(
            value = value,
            onValueChange = onValueChange,
            hint = hint,
            isError = isError,
            errorMessage = errorMessage,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun PasswordTrailingIcon(
    isPasswordVisible: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconRes = when {
        isError -> DesignSystemR.drawable.ic_password_wrong
        isPasswordVisible -> DesignSystemR.drawable.ic_password_on
        else -> DesignSystemR.drawable.ic_password
    }
    val contentDescription = if (isPasswordVisible) {
        "비밀번호 숨기기"
    } else {
        "비밀번호 보기"
    }

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = if (isError) null else contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(24.dp)
            .then(
                if (isError) {
                    Modifier
                } else {
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = onClick,
                        )
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
private fun PasswordResetRequiredOverlay(
    onPrimaryClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xB32A2A2A)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(328.dp)
                .height(300.dp)
                .offset(y = (-80).dp)
                .clip(RoundedCornerShape(16.dp))
                .clipToBounds()
                .background(White)
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PasswordResetRequiredIcon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "비밀번호 입력이 5회 초과됐어요",
                color = Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "보안을 위해 비밀번호 재설정이 필요해요\n이메일 인증 후 새 비밀번호를 설정해 주세요",
                color = Gray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            QuiketPrimaryButton(
                text = "이메일 인증하기",
                onClick = onPrimaryClick,
                modifier = Modifier.width(288.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "나중에 할게요",
                color = Gray600,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = onDismissClick,
                    )
                    .padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun PasswordResetRequiredIcon(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Gray100),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val envelopeTopLeft = Offset(size.width * 0.12f, size.height * 0.24f)
            val envelopeSize = Size(size.width * 0.62f, size.height * 0.43f)
            val lineStrokeWidth = 3.dp.toPx()

            drawRoundRect(
                color = Brown950,
                topLeft = envelopeTopLeft,
                size = envelopeSize,
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
            drawLine(
                color = White,
                start = Offset(envelopeTopLeft.x + 4.dp.toPx(), envelopeTopLeft.y + 5.dp.toPx()),
                end = Offset(envelopeTopLeft.x + envelopeSize.width / 2f, envelopeTopLeft.y + 22.dp.toPx()),
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = White,
                start = Offset(envelopeTopLeft.x + envelopeSize.width - 4.dp.toPx(), envelopeTopLeft.y + 5.dp.toPx()),
                end = Offset(envelopeTopLeft.x + envelopeSize.width / 2f, envelopeTopLeft.y + 22.dp.toPx()),
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Round,
            )

            val checkCenter = Offset(size.width * 0.72f, size.height * 0.67f)
            drawCircle(
                color = Brown950,
                radius = 18.dp.toPx(),
                center = checkCenter,
            )
            drawLine(
                color = White,
                start = Offset(checkCenter.x - 8.dp.toPx(), checkCenter.y),
                end = Offset(checkCenter.x - 2.dp.toPx(), checkCenter.y + 6.dp.toPx()),
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = White,
                start = Offset(checkCenter.x - 2.dp.toPx(), checkCenter.y + 6.dp.toPx()),
                end = Offset(checkCenter.x + 9.dp.toPx(), checkCenter.y - 8.dp.toPx()),
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginEmailScreenPreview() {
    QuiketTheme {
        LoginEmailScreen(
            email = "",
            password = "",
            isPasswordVisible = false,
            isLoginEnabled = false,
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityClick = {},
            onBackClick = {},
            onForgotPasswordClick = {},
            onLoginClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginEmailScreenErrorPreview() {
    QuiketTheme {
        LoginEmailScreen(
            email = "",
            password = "password",
            isPasswordVisible = false,
            isLoginEnabled = false,
            passwordErrorMessage = "비밀번호를 다시 입력해주세요 (1/5)",
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityClick = {},
            onBackClick = {},
            onForgotPasswordClick = {},
            onLoginClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginEmailScreenPasswordResetRequiredPreview() {
    QuiketTheme {
        LoginEmailScreen(
            email = "aiquiz@quiket.com",
            password = "password",
            isPasswordVisible = false,
            isLoginEnabled = false,
            passwordErrorMessage = "비밀번호를 다시 입력해주세요 (5/5)",
            showPasswordResetRequiredDialog = true,
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityClick = {},
            onBackClick = {},
            onForgotPasswordClick = {},
            onLoginClick = {},
        )
    }
}
