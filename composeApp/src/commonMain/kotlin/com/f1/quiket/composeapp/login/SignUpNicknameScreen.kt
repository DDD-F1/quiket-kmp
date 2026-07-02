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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite

private const val SignUpNicknameGuide = "2-12자 사이의 영문/한글"
internal const val SignUpNicknameMaxLength = 12

@Composable
internal fun SignUpNicknameRoute(
    draft: SignupDraft,
    onBackClick: () -> Unit,
    onNextClick: (SignupDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nickname by remember(draft.nickname) { mutableStateOf(draft.nickname) }
    var nicknameErrorMessage by remember { mutableStateOf<String?>(null) }

    fun updateNickname(nextValue: String) {
        val next = nextValue.take(SignUpNicknameMaxLength)
        nickname = next
        nicknameErrorMessage = if (next.isNotBlank() && !isValidNickname(next.trim())) {
            NicknameFormatErrorMessage
        } else {
            null
        }
    }

    fun submit() {
        val trimmedNickname = nickname.trim()
        if (!isValidNickname(trimmedNickname)) {
            nicknameErrorMessage = NicknameFormatErrorMessage
            return
        }

        onNextClick(draft.copy(nickname = trimmedNickname))
    }

    SignUpNicknameScreen(
        nickname = nickname,
        nicknameErrorMessage = nicknameErrorMessage,
        isNextEnabled = isValidNickname(nickname.trim()) && nicknameErrorMessage == null,
        onNicknameChange = ::updateNickname,
        onBackClick = onBackClick,
        onNextClick = ::submit,
        modifier = modifier,
    )
}

@Composable
internal fun SignUpNicknameScreen(
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
            .background(QuiketWhite),
    ) {
        SignUpTopBar(
            title = "회원가입",
            onBackClick = onBackClick,
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
                SignUpPageIndicator(currentPage = 1, pageCount = 3)
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
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )
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
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "불리고 싶은 닉네임을 설정해주세요!",
            color = QuiketGray700,
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
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        QuiketTextField(
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
                color = QuiketGray400,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
    }
}
