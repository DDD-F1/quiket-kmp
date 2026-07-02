package com.f1.quiket.composeapp.mypage

import org.koin.compose.koinInject
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.composeapp.AppMetadata
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCategory
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings
import com.f1.quiket.composeapp.mypage.presentation.AccountSettingsStateHolder
import com.f1.quiket.composeapp.mypage.presentation.InquiryStateHolder
import com.f1.quiket.composeapp.mypage.presentation.NotificationSettingsStateHolder
import com.f1.quiket.composeapp.util.hidePlatformKeyboard
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.app_logo
import quiket.composeapp.generated.resources.ic_next

@Composable
internal fun AccountSettingsRoute(
    onBackClick: () -> Unit,
    onAccountDeleted: () -> Unit,
    onProfileChanged: (MyProfile) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<AccountSettingsStateHolder>()

    fun loadProfile() {
        coroutineScope.launch {
            stateHolder.loadProfile(onSessionExpired = onSessionExpired)
        }
    }

    LaunchedEffect(stateHolder) {
        loadProfile()
    }

    AccountSettingsScreen(
        state = stateHolder.state,
        onBackClick = onBackClick,
        onRetryClick = { loadProfile() },
        onNicknameClick = stateHolder::showNicknameDialog,
        onEmailClick = stateHolder::showEmailRequestDialog,
        onPasswordClick = stateHolder::showPasswordDialog,
        onDeleteClick = stateHolder::showDeleteAccountDialog,
        modifier = modifier,
    )

    val state = stateHolder.state
    when (val dialog = stateHolder.activeDialog) {
        is AccountDialog.Nickname -> {
            var nickname by remember(dialog.initialNickname) { mutableStateOf(dialog.initialNickname) }
            SingleInputDialog(
                title = "닉네임 변경",
                value = nickname,
                hint = "새 닉네임",
                isSaving = state.isSaving,
                confirmLabel = "변경",
                errorMessage = state.message,
                onValueChange = {
                    nickname = it
                    stateHolder.clearMessage()
                },
                onDismiss = stateHolder::dismissDialog,
                onConfirm = {
                    coroutineScope.launch {
                        stateHolder.updateNickname(
                            nickname = nickname,
                            onSessionExpired = onSessionExpired,
                        )?.let(onProfileChanged)
                    }
                },
            )
        }

        AccountDialog.EmailRequest -> {
            var email by remember { mutableStateOf("") }
            SingleInputDialog(
                title = "이메일 변경",
                value = email,
                hint = "새 이메일",
                isSaving = state.isSaving,
                confirmLabel = "인증 메일 발송",
                keyboardType = KeyboardType.Email,
                errorMessage = state.message,
                onValueChange = {
                    email = it
                    stateHolder.clearMessage()
                },
                onDismiss = stateHolder::dismissDialog,
                onConfirm = {
                    coroutineScope.launch {
                        stateHolder.requestEmailChange(
                            email = email,
                            onSessionExpired = onSessionExpired,
                        )
                    }
                },
            )
        }

        is AccountDialog.EmailConfirm -> {
            var code by remember(dialog.email) { mutableStateOf("") }
            SingleInputDialog(
                title = "이메일 인증",
                description = "${dialog.email}로 발송된 6자리 인증 코드를 입력해주세요.",
                value = code,
                hint = "인증 코드",
                isSaving = state.isSaving,
                confirmLabel = "확인",
                keyboardType = KeyboardType.Number,
                errorMessage = state.message,
                onValueChange = {
                    code = it.take(6)
                    stateHolder.clearMessage()
                },
                onDismiss = stateHolder::dismissDialog,
                onConfirm = {
                    coroutineScope.launch {
                        stateHolder.confirmEmailChange(
                            email = dialog.email,
                            code = code,
                            onSessionExpired = onSessionExpired,
                        )?.let(onProfileChanged)
                    }
                },
            )
        }

        AccountDialog.Password -> {
            var currentPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var newPasswordConfirm by remember { mutableStateOf("") }
            PasswordChangeDialog(
                currentPassword = currentPassword,
                newPassword = newPassword,
                newPasswordConfirm = newPasswordConfirm,
                isSaving = state.isSaving,
                onCurrentPasswordChange = {
                    currentPassword = it
                    stateHolder.clearMessage()
                },
                onNewPasswordChange = {
                    newPassword = it
                    stateHolder.clearMessage()
                },
                onNewPasswordConfirmChange = {
                    newPasswordConfirm = it
                    stateHolder.clearMessage()
                },
                errorMessage = state.message,
                onDismiss = stateHolder::dismissDialog,
                onConfirm = {
                    coroutineScope.launch {
                        stateHolder.updatePassword(
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            newPasswordConfirm = newPasswordConfirm,
                            onSessionExpired = onSessionExpired,
                        )
                    }
                },
            )
        }

        AccountDialog.DeleteAccount -> {
            var password by remember { mutableStateOf("") }
            DeleteAccountDialog(
                password = password,
                requiresPassword = state.isLocalAccount,
                isSaving = state.isSaving,
                onPasswordChange = {
                    password = it
                    stateHolder.clearMessage()
                },
                errorMessage = state.message,
                onDismiss = stateHolder::dismissDialog,
                onConfirm = {
                    coroutineScope.launch {
                        if (stateHolder.deleteAccount(
                                password = password,
                                onSessionExpired = onSessionExpired,
                            )
                        ) {
                            onAccountDeleted()
                        }
                    }
                },
            )
        }

        null -> Unit
    }
}

@Composable
internal fun NotificationSettingsRoute(
    onBackClick: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<NotificationSettingsStateHolder>()

    fun loadSettings() {
        coroutineScope.launch {
            stateHolder.loadSettings(onSessionExpired = onSessionExpired)
        }
    }

    fun updateSettings(nextSettings: NotificationSettings) {
        coroutineScope.launch {
            stateHolder.updateSettings(
                nextSettings = nextSettings,
                onSessionExpired = onSessionExpired,
            )
        }
    }

    LaunchedEffect(stateHolder) {
        loadSettings()
    }

    NotificationSettingsScreen(
        state = stateHolder.state,
        onBackClick = onBackClick,
        onRetryClick = { loadSettings() },
        onActivityToggle = { enabled ->
            stateHolder.state.settings?.let { updateSettings(it.copy(activityEnabled = enabled)) }
        },
        onUpdateToggle = { enabled ->
            stateHolder.state.settings?.let { updateSettings(it.copy(updateEnabled = enabled)) }
        },
        onReviewToggle = { enabled ->
            stateHolder.state.settings?.let { updateSettings(it.copy(reviewEnabled = enabled)) }
        },
        modifier = modifier,
    )
}

@Composable
internal fun InquiryRoute(
    onBackClick: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<InquiryStateHolder>()

    InquiryScreen(
        state = stateHolder.state,
        onBackClick = onBackClick,
        onCategoryClick = stateHolder::selectCategory,
        onBodyChange = stateHolder::updateBody,
        onReplyEmailChange = stateHolder::updateReplyEmail,
        onSubmitClick = {
            coroutineScope.launch {
                stateHolder.submit(onSessionExpired = onSessionExpired)
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun AppInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsTopBar(title = "앱 정보", onBackClick = onBackClick)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = QuiketGray50,
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_logo),
                    contentDescription = "Quiket 앱 로고",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp)),
                )
                Text(
                    text = AppMetadata.appName,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "AI 퀴즈 생성, 시험 공부",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        SubScreenSection(label = "버전")
        InfoRow(label = "버전", value = AppMetadata.versionName)
        InfoRow(label = "빌드", value = AppMetadata.buildNumber)

        SubScreenSection(label = "서비스")
        InfoRow(label = "운영", value = "JMaru")
        InfoRow(label = "문의", value = "앱 내 문의")
        InfoRow(label = "법적 고지", value = "이용 약관 및 개인정보처리방침")
    }
}

@Composable
private fun AccountSettingsScreen(
    state: AccountSettingsUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNicknameClick: (String) -> Unit,
    onEmailClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsTopBar(title = "계정 설정", onBackClick = onBackClick)

        when {
            state.isLoading -> MyPageLoadingBlock(text = "계정 정보를 불러오고 있어요")
            profile == null -> MyPageErrorBlock(
                title = "계정 정보를 불러오지 못했어요",
                message = state.message ?: "잠시 후 다시 시도해주세요.",
                onRetryClick = onRetryClick,
            )
            else -> {
                CurrentAccountInfo(
                    nickname = profile.nickname,
                    email = profile.email.orEmpty(),
                )
                state.message?.let { MyPageMessage(text = it) }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubScreenRow(
                        title = "닉네임 변경",
                        subtitle = profile.nickname.ifBlank { null },
                        onClick = { onNicknameClick(profile.nickname) },
                    )
                    SubScreenRow(
                        title = "이메일 변경",
                        subtitle = profile.email?.takeIf { it.isNotBlank() },
                        onClick = onEmailClick,
                    )
                    if (state.isLocalAccount) {
                        SubScreenRow(
                            title = "비밀번호 변경",
                            onClick = onPasswordClick,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubScreenRow(
                        title = "회원 탈퇴",
                        destructive = true,
                        onClick = onDeleteClick,
                    )
                }

                if (state.isSaving) {
                    MyPageLoadingBlock(text = "처리 중이에요")
                }
            }
        }
    }
}

@Composable
private fun CurrentAccountInfo(
    nickname: String,
    email: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = nickname,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = email,
            color = QuiketGray700,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                color = QuiketGray600,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.42f),
            )
            Text(
                text = value,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(0.58f),
            )
        }
    }
}

@Composable
private fun NotificationSettingsScreen(
    state: NotificationSettingsUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onActivityToggle: (Boolean) -> Unit,
    onUpdateToggle: (Boolean) -> Unit,
    onReviewToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsTopBar(title = "알림 설정", onBackClick = onBackClick)

        when {
            state.isLoading -> MyPageLoadingBlock(text = "알림 설정을 불러오고 있어요")
            settings == null -> MyPageErrorBlock(
                title = "알림 설정을 불러오지 못했어요",
                message = state.message ?: "잠시 후 다시 시도해주세요.",
                onRetryClick = onRetryClick,
            )
            else -> {
                state.message?.let { MyPageMessage(text = it) }
                NotificationToggleRow(
                    title = "활동 알림",
                    subtitle = "퀴즈 생성 완료 등 주요 활동 알림",
                    checked = settings.activityEnabled,
                    enabled = !state.isSaving,
                    onCheckedChange = onActivityToggle,
                )
                NotificationToggleRow(
                    title = "업데이트 알림",
                    subtitle = "새로운 기능 및 공지사항 알림",
                    checked = settings.updateEnabled,
                    enabled = !state.isSaving,
                    onCheckedChange = onUpdateToggle,
                )
                NotificationToggleRow(
                    title = "복습 알림",
                    subtitle = "학습 복습 주기 알림",
                    checked = settings.reviewEnabled,
                    enabled = !state.isSaving,
                    onCheckedChange = onReviewToggle,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InquiryScreen(
    state: InquiryUiState,
    onBackClick: () -> Unit,
    onCategoryClick: (FeedbackCategory) -> Unit,
    onBodyChange: (String) -> Unit,
    onReplyEmailChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun hideKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
        hidePlatformKeyboard()
    }

    fun submitInquiry() {
        hideKeyboard()
        onSubmitClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 28.dp),
    ) {
        SettingsTopBar(title = "문의", onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(16.dp))
        SubScreenSection(label = "문의 유형")
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                FeedbackCategory.Feature,
                FeedbackCategory.Bug,
                FeedbackCategory.Inquiry,
                FeedbackCategory.Other,
            ).forEach { category ->
                CategoryChip(
                    category = category,
                    selected = state.category == category,
                    onClick = { onCategoryClick(category) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        SubScreenSection(label = "내용")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.body,
            onValueChange = onBodyChange,
            placeholder = {
                Text(
                    text = "문의 내용을 입력해주세요. (최대 1000자)",
                    color = QuiketGray700,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .semantics { contentDescription = "문의 내용 입력" },
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
            maxLines = 10,
        )

        Spacer(modifier = Modifier.height(16.dp))
        SubScreenSection(label = "답변 받을 이메일 (선택)")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.replyEmail,
            onValueChange = onReplyEmailChange,
            placeholder = { Text("이메일 주소", color = QuiketGray700) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "답변 이메일 입력" },
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
            singleLine = true,
        )

        state.message?.let { MyPageMessage(text = it) }

        Spacer(modifier = Modifier.height(32.dp))
        InquirySubmitButton(
            text = if (state.isSubmitting) "제출 중..." else "제출하기",
            enabled = state.body.isNotBlank() && !state.isSubmitting,
            onClick = ::submitInquiry,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SubScreenSection(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier,
    )
}

@Composable
private fun SubScreenRow(
    title: String,
    subtitle: String? = null,
    destructive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, QuiketGray100, shape)
            .background(QuiketWhite)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (destructive) QuiketOrange500 else QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            painter = painterResource(Res.drawable.ic_next),
            contentDescription = null,
            tint = QuiketGray900,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, QuiketGray100, shape)
            .background(QuiketWhite)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = subtitle,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = QuiketWhite,
                checkedTrackColor = QuiketOrange500,
                uncheckedThumbColor = QuiketWhite,
                uncheckedTrackColor = QuiketGray300,
            ),
        )
    }
}

@Composable
private fun CategoryChip(
    category: FeedbackCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) QuiketOrange500 else QuiketWhite
    val textColor = if (selected) QuiketWhite else QuiketGray950
    val borderColor = if (selected) QuiketOrange500 else QuiketGray100
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.displayLabel,
            color = textColor,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun InquirySubmitButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = QuiketOrange500,
            contentColor = QuiketWhite,
            disabledContainerColor = QuiketGray100,
            disabledContentColor = QuiketGray300,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun MyPageLoadingBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = QuiketOrange500, modifier = Modifier.size(26.dp))
        Text(
            text = text,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun MyPageErrorBlock(
    title: String,
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = message,
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall,
            )
            QuiketPrimaryButton(
                text = "다시 시도",
                enabled = true,
                onClick = onRetryClick,
            )
        }
    }
}

@Composable
private fun MyPageMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketBrown50,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = text,
            color = QuiketBrown950,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun SingleInputDialog(
    title: String,
    value: String,
    hint: String,
    isSaving: Boolean,
    confirmLabel: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    description: String? = null,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    AccountSettingDialog(
        title = title,
        onDismissRequest = onDismiss,
        confirmLabel = confirmLabel,
        confirmEnabled = !isSaving,
        dismissEnabled = !isSaving,
        onConfirm = onConfirm,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            description?.let {
                Text(
                    text = it,
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            QuiketTextField(
                value = value,
                onValueChange = onValueChange,
                hint = hint,
                modifier = Modifier.semantics { contentDescription = "$title 입력" },
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = QuiketNegative,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun AccountSettingDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QuiketGray950.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(QuiketWhite)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                content()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DialogOutlinedButton(
                        text = "취소",
                        enabled = dismissEnabled,
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                    )
                    QuiketPrimaryButton(
                        text = confirmLabel,
                        enabled = confirmEnabled,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogOutlinedButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .border(2.dp, if (enabled) QuiketBrown950 else QuiketGray300, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) QuiketBrown950 else QuiketGray300,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun PasswordChangeDialog(
    currentPassword: String,
    newPassword: String,
    newPasswordConfirm: String,
    isSaving: Boolean,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AccountSettingDialog(
        title = "비밀번호 변경",
        onDismissRequest = onDismiss,
        confirmLabel = "변경",
        confirmEnabled = !isSaving,
        dismissEnabled = !isSaving,
        onConfirm = onConfirm,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuiketTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                hint = "현재 비밀번호",
                enabled = !isSaving,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            QuiketTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                hint = "새 비밀번호",
                enabled = !isSaving,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            QuiketTextField(
                value = newPasswordConfirm,
                onValueChange = onNewPasswordConfirmChange,
                hint = "새 비밀번호 확인",
                enabled = !isSaving,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = QuiketNegative,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    password: String,
    requiresPassword: Boolean,
    isSaving: Boolean,
    onPasswordChange: (String) -> Unit,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AccountSettingDialog(
        title = "회원 탈퇴",
        onDismissRequest = onDismiss,
        confirmLabel = "탈퇴",
        confirmEnabled = !isSaving,
        dismissEnabled = !isSaving,
        onConfirm = onConfirm,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "탈퇴 시 모든 학습 기록과 도토리가 삭제됩니다. 정말 탈퇴하시겠습니까?",
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
            if (requiresPassword) {
                QuiketTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    hint = "비밀번호 확인",
                    enabled = !isSaving,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            }
            errorMessage?.let {
                Text(
                    text = it,
                    color = QuiketNegative,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

internal data class AccountSettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val profile: MyProfile? = null,
    val message: String? = null,
) {
    val isLocalAccount: Boolean
        get() = profile?.providers.orEmpty().any { it.equals("local", ignoreCase = true) }
}

internal sealed interface AccountDialog {
    data class Nickname(val initialNickname: String) : AccountDialog
    data object EmailRequest : AccountDialog
    data class EmailConfirm(val email: String) : AccountDialog
    data object Password : AccountDialog
    data object DeleteAccount : AccountDialog
}

internal data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val settings: NotificationSettings? = null,
    val message: String? = null,
)

internal data class InquiryUiState(
    val isSubmitting: Boolean = false,
    val category: FeedbackCategory = FeedbackCategory.Inquiry,
    val body: String = "",
    val replyEmail: String = "",
    val message: String? = null,
)
