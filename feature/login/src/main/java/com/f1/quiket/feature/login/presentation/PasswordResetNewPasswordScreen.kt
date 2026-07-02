package com.f1.quiket.feature.login.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

private const val PasswordPolicyGuide = "8글자 이상의 영문/숫자/특수문자 조합"

@Composable
fun PasswordResetNewPasswordScreen(
    password: String,
    passwordConfirm: String,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    passwordConfirmErrorMessage: String?,
    isCompleteEnabled: Boolean,
    showEmailVerifiedMessage: Boolean,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onPasswordConfirmVisibilityClick: () -> Unit,
    onCloseClick: () -> Unit,
    onCompleteClick: () -> Unit,
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
                PasswordResetPageIndicator(currentPage = 1)
            }
            PasswordResetNewPasswordTitle()
            PasswordResetPasswordFields(
                password = password,
                passwordConfirm = passwordConfirm,
                isPasswordVisible = isPasswordVisible,
                isPasswordConfirmVisible = isPasswordConfirmVisible,
                passwordConfirmErrorMessage = passwordConfirmErrorMessage,
                onPasswordChange = onPasswordChange,
                onPasswordConfirmChange = onPasswordConfirmChange,
                onPasswordVisibilityClick = onPasswordVisibilityClick,
                onPasswordConfirmVisibilityClick = onPasswordConfirmVisibilityClick,
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
            text = "완료 및 로그인하기",
            enabled = isCompleteEnabled,
            onClick = onCompleteClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 28.dp),
        )
    }
}

@Composable
private fun PasswordResetNewPasswordTitle(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "새로운 비밀번호를 설정해주세요",
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "직전에 사용한 비밀번호는 사용할 수 없어요",
            color = Gray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun PasswordResetPasswordFields(
    password: String,
    passwordConfirm: String,
    isPasswordVisible: Boolean,
    isPasswordConfirmVisible: Boolean,
    passwordConfirmErrorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onPasswordConfirmVisibilityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PasswordResetPasswordField(
            title = "비밀번호",
            value = password,
            onValueChange = onPasswordChange,
            hint = "비밀번호를 입력해주세요",
            helperMessage = PasswordPolicyGuide,
            isPasswordVisible = isPasswordVisible,
            onPasswordVisibilityClick = onPasswordVisibilityClick,
        )
        if (password.isNotBlank() || passwordConfirm.isNotBlank()) {
            PasswordResetPasswordField(
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

@Composable
private fun PasswordResetPasswordField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    isPasswordVisible: Boolean,
    onPasswordVisibilityClick: () -> Unit,
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
                PasswordResetPasswordVisibilityIcon(
                    isPasswordVisible = isPasswordVisible,
                    onClick = onPasswordVisibilityClick,
                )
                }
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
private fun PasswordResetPasswordVisibilityIcon(
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
private fun PasswordResetNewPasswordPreview() {
    QuiketTheme {
        PasswordResetNewPasswordScreen(
            password = "",
            passwordConfirm = "",
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            passwordConfirmErrorMessage = null,
            isCompleteEnabled = false,
            showEmailVerifiedMessage = true,
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onCloseClick = {},
            onCompleteClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetNewPasswordInputPreview() {
    QuiketTheme {
        PasswordResetNewPasswordScreen(
            password = "Password1!",
            passwordConfirm = "",
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            passwordConfirmErrorMessage = null,
            isCompleteEnabled = false,
            showEmailVerifiedMessage = false,
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onCloseClick = {},
            onCompleteClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PasswordResetNewPasswordMismatchPreview() {
    QuiketTheme {
        PasswordResetNewPasswordScreen(
            password = "Password1!",
            passwordConfirm = "Password2!",
            isPasswordVisible = false,
            isPasswordConfirmVisible = false,
            passwordConfirmErrorMessage = "비밀번호가 일치하지 않아요",
            isCompleteEnabled = false,
            showEmailVerifiedMessage = false,
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onPasswordVisibilityClick = {},
            onPasswordConfirmVisibilityClick = {},
            onCloseClick = {},
            onCompleteClick = {},
        )
    }
}
