package com.f1.quiket.feature.login.presentation

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

private const val SignUpNicknameGuide = "2-12자 사이의 영문/한글"

@Composable
fun SignUpNicknameScreen(
    nickname: String,
    nicknameErrorMessage: String?,
    isNextEnabled: Boolean,
    onNicknameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        SignUpNicknameTopBar(
            title = "회원가입",
            onBackClick = onBackClick,
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
                PasswordResetPageIndicator(currentPage = 1, pageCount = 3)
            }
            SignUpNicknameTitleSection()
            SignUpNicknameInput(
                nickname = nickname,
                nicknameErrorMessage = nicknameErrorMessage,
                onNicknameChange = onNicknameChange,
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
private fun SignUpNicknameTopBar(
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
        SignUpNicknameBackButton(
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
private fun SignUpNicknameBackButton(
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
private fun SignUpNicknameTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "어떻게 불러드릴까요?",
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "불리고 싶은 닉네임을 설정해주세요!",
            color = Gray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SignUpNicknameInput(
    nickname: String,
    nicknameErrorMessage: String?,
    onNicknameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isError = !nicknameErrorMessage.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "닉네임",
            color = Gray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        BaseTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            hint = "원하시는 닉네임을 입력해주세요",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = isError,
            errorMessage = nicknameErrorMessage,
        )
        if (!isError) {
            Text(
                text = SignUpNicknameGuide,
                color = Gray500,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpNicknameEmptyPreview() {
    QuiketTheme {
        SignUpNicknameScreen(
            nickname = "",
            nicknameErrorMessage = null,
            isNextEnabled = false,
            onNicknameChange = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpNicknameInputPreview() {
    QuiketTheme {
        SignUpNicknameScreen(
            nickname = "퀴켓",
            nicknameErrorMessage = null,
            isNextEnabled = false,
            onNicknameChange = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpNicknameDuplicatePreview() {
    QuiketTheme {
        SignUpNicknameScreen(
            nickname = "퀴켓",
            nicknameErrorMessage = "앗! 중복되는 닉네임이에요",
            isNextEnabled = false,
            onNicknameChange = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpNicknameValidPreview() {
    QuiketTheme {
        SignUpNicknameScreen(
            nickname = "갓생 사는 다람쥐",
            nicknameErrorMessage = null,
            isNextEnabled = true,
            onNicknameChange = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}
