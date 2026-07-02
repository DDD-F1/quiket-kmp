package com.f1.quiket.feature.mypage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.component.QuiketOutlinedButton
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

@Composable
fun AccountSettingScreen(
    state: AccountSettingState,
    showVerifyEmailDialog: Boolean,
    pendingEmail: String,
    onDismissVerifyEmailDialog: () -> Unit,
    onIntent: (AccountSettingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showNicknameDialog by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf("") }

    var showEmailDialog by remember { mutableStateOf(false) }
    var newEmailInput by remember { mutableStateOf("") }
    var verifyCodeInput by remember { mutableStateOf("") }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPwInput by remember { mutableStateOf("") }
    var newPwInput by remember { mutableStateOf("") }
    var confirmPwInput by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState()),
    ) {
        AccountSettingTopBar(
            onBackClick = { onIntent(AccountSettingIntent.NavigateBack) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Orange500)
            }
        } else {
            CurrentInfoSection(
                nickname = state.nickname,
                email = state.email,
            )

            Spacer(modifier = Modifier.height(20.dp))

            AccountSettingGroup {
                AccountSettingRow(
                    title = "닉네임 변경",
                    subtitle = state.nickname.ifBlank { null },
                    onClick = {
                        nicknameInput = state.nickname
                        showNicknameDialog = true
                    },
                )
                AccountSettingRow(
                    title = "이메일 변경",
                    subtitle = state.email.ifBlank { null },
                    onClick = {
                        newEmailInput = ""
                        showEmailDialog = true
                    },
                )
                if (state.isLocalAccount) {
                    AccountSettingRow(
                        title = "비밀번호 변경",
                        onClick = {
                            currentPwInput = ""
                            newPwInput = ""
                            confirmPwInput = ""
                            showPasswordDialog = true
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AccountSettingGroup {
                AccountSettingRow(
                    title = "회원 탈퇴",
                    titleColor = Orange500,
                    onClick = {
                        deletePasswordInput = ""
                        showDeleteDialog = true
                    },
                )
            }
        }

        if (state.isOperationLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Orange500, modifier = Modifier.size(24.dp))
            }
        }
    }

    // ── Nickname Dialog ────────────────────────────────────────────────────────
    if (showNicknameDialog) {
        AccountSettingDialog(
            title = "닉네임 변경",
            onDismiss = { showNicknameDialog = false },
            confirmLabel = "변경",
            onConfirm = {
                showNicknameDialog = false
                onIntent(AccountSettingIntent.UpdateNickname(nicknameInput.trim()))
            },
        ) {
            BaseTextField(
                value = nicknameInput,
                onValueChange = { nicknameInput = it },
                hint = "새 닉네임",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // ── Email Change Request Dialog ────────────────────────────────────────────
    if (showEmailDialog) {
        AccountSettingDialog(
            title = "이메일 변경",
            onDismiss = { showEmailDialog = false },
            confirmLabel = "인증 메일 발송",
            onConfirm = {
                showEmailDialog = false
                onIntent(AccountSettingIntent.RequestEmailChange(newEmailInput.trim()))
            },
        ) {
            BaseTextField(
                value = newEmailInput,
                onValueChange = { newEmailInput = it },
                hint = "새 이메일",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
        }
    }

    // ── Email Verify Code Dialog ───────────────────────────────────────────────
    if (showVerifyEmailDialog) {
        AccountSettingDialog(
            title = "이메일 인증",
            onDismiss = {
                verifyCodeInput = ""
                onDismissVerifyEmailDialog()
            },
            confirmLabel = "확인",
            onConfirm = {
                onIntent(AccountSettingIntent.ConfirmEmailChange(pendingEmail, verifyCodeInput.trim()))
            },
        ) {
            Text(
                text = "$pendingEmail 으로 발송된 6자리 인증 코드를 입력하세요.",
                color = Gray700,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            BaseTextField(
                value = verifyCodeInput,
                onValueChange = { verifyCodeInput = it.take(6) },
                hint = "인증 코드",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }

    // ── Password Change Dialog ─────────────────────────────────────────────────
    if (showPasswordDialog) {
        AccountSettingDialog(
            title = "비밀번호 변경",
            onDismiss = { showPasswordDialog = false },
            confirmLabel = "변경",
            onConfirm = {
                showPasswordDialog = false
                onIntent(
                    AccountSettingIntent.ChangePassword(
                        currentPassword = currentPwInput,
                        newPassword = newPwInput,
                        newPasswordConfirm = confirmPwInput,
                    ),
                )
            },
        ) {
            BaseTextField(
                value = currentPwInput,
                onValueChange = { currentPwInput = it },
                hint = "현재 비밀번호",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(modifier = Modifier.height(8.dp))
            BaseTextField(
                value = newPwInput,
                onValueChange = { newPwInput = it },
                hint = "새 비밀번호",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(modifier = Modifier.height(8.dp))
            BaseTextField(
                value = confirmPwInput,
                onValueChange = { confirmPwInput = it },
                hint = "새 비밀번호 확인",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
        }
    }

    // ── Delete Account Dialog ──────────────────────────────────────────────────
    if (showDeleteDialog) {
        AccountSettingDialog(
            title = "회원 탈퇴",
            onDismiss = { showDeleteDialog = false },
            confirmLabel = "탈퇴",
            onConfirm = {
                showDeleteDialog = false
                onIntent(
                    AccountSettingIntent.DeleteAccount(
                        password = deletePasswordInput.ifBlank { null },
                        agreed = true,
                    ),
                )
            },
        ) {
            Text(
                text = "탈퇴 시 모든 학습 기록과 도토리가 삭제됩니다. 정말 탈퇴하시겠습니까?",
                color = Gray700,
                fontSize = 13.sp,
            )
            if (state.isLocalAccount) {
                Spacer(modifier = Modifier.height(12.dp))
                BaseTextField(
                    value = deletePasswordInput,
                    onValueChange = { deletePasswordInput = it },
                    hint = "비밀번호 확인",
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            }
        }
    }
}

@Composable
private fun AccountSettingDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray950,
                )
                content()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuiketOutlinedButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    QuiketPrimaryButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSettingTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
            contentDescription = "뒤로",
            tint = Gray700,
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() },
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "계정 설정",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray950,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun CurrentInfoSection(nickname: String, email: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(text = nickname, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray950)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = email, fontSize = 14.sp, color = Gray700)
    }
}

@Composable
private fun AccountSettingGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

@Composable
private fun AccountSettingRow(
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = Gray950,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Gray100, RoundedCornerShape(12.dp))
            .background(White)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Gray700,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = com.f1.quiket.core.designsystem.R.drawable.ic_next),
            contentDescription = null,
            tint = Gray900,
            modifier = Modifier.size(24.dp),
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "계정 설정 — 로딩 중")
@Composable
private fun AccountSettingScreenLoadingPreview() {
    QuiketTheme {
        AccountSettingScreen(
            state = AccountSettingState(isLoading = true),
            showVerifyEmailDialog = false,
            pendingEmail = "",
            onDismissVerifyEmailDialog = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "계정 설정 — Local 계정")
@Composable
private fun AccountSettingScreenLocalPreview() {
    QuiketTheme {
        AccountSettingScreen(
            state = AccountSettingState(
                isLoading = false,
                nickname = "도토리장인",
                email = "user@example.com",
                providers = listOf("local"),
            ),
            showVerifyEmailDialog = false,
            pendingEmail = "",
            onDismissVerifyEmailDialog = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "계정 설정 — Kakao 전용")
@Composable
private fun AccountSettingScreenKakaoPreview() {
    QuiketTheme {
        AccountSettingScreen(
            state = AccountSettingState(
                isLoading = false,
                nickname = "카카오유저",
                email = "kakao@example.com",
                providers = listOf("kakao"),
            ),
            showVerifyEmailDialog = false,
            pendingEmail = "",
            onDismissVerifyEmailDialog = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "계정 설정 — 이메일 인증 다이얼로그")
@Composable
private fun AccountSettingScreenVerifyDialogPreview() {
    QuiketTheme {
        AccountSettingScreen(
            state = AccountSettingState(
                isLoading = false,
                nickname = "도토리장인",
                email = "user@example.com",
                providers = listOf("local"),
            ),
            showVerifyEmailDialog = true,
            pendingEmail = "new@example.com",
            onDismissVerifyEmailDialog = {},
            onIntent = {},
        )
    }
}