package com.f1.quiket.composeapp.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.AppMetadata
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_alert
import com.f1.quiket.core.designsystem.resources.ic_info
import com.f1.quiket.core.designsystem.resources.ic_next
import com.f1.quiket.core.designsystem.resources.ic_topbar_back
import com.f1.quiket.feature.mypage.resources.Res
import com.f1.quiket.feature.mypage.resources.ic_person
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPageSettingsScreen(
    onBackClick: () -> Unit,
    onAccountClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsTopBar(
            title = "설정",
            onBackClick = onBackClick,
        )

        SettingsSection(label = "계정")
        SettingsGroup {
            SettingsRow(
                title = "계정 설정",
                subtitle = "닉네임, 이메일, 비밀번호",
                icon = Res.drawable.ic_person,
                onClick = onAccountClick,
            )
            SettingsRow(
                title = "알림 설정",
                subtitle = "활동, 업데이트, 복습 주기",
            icon = DesignSystemRes.drawable.ic_alert,
                onClick = onNotificationClick,
            )
        }

        SettingsSection(label = "앱")
        SettingsGroup {
            SettingsRow(
                title = "문의",
                icon = DesignSystemRes.drawable.ic_info,
                iconTint = QuiketGray400,
                onClick = onInquiryClick,
            )
            SettingsRow(
                title = "이용 약관",
                icon = DesignSystemRes.drawable.ic_info,
                iconTint = QuiketGray400,
                onClick = onTermsClick,
            )
            SettingsRow(
                title = "개인정보 처리방침",
                icon = DesignSystemRes.drawable.ic_info,
                iconTint = QuiketGray400,
                onClick = onPrivacyPolicyClick,
            )
            SettingsRow(
                title = "앱 정보",
                icon = DesignSystemRes.drawable.ic_info,
                iconTint = QuiketGray400,
                onClick = onAppInfoClick,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(999.dp))
                .clickable(role = Role.Button, onClick = onLogoutClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Quiket을 로그아웃 하실 건가요?",
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = " 로그아웃",
                color = QuiketOrange500,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
fun LegalTextScreen(
    title: String,
    body: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val documentTitle = remember(body) { body.lines().firstOrNull().orEmpty() }
    val sections = remember(body) { body.toLegalSections() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        SettingsTopBar(
            title = title,
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 116.dp, bottom = 116.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = documentTitle,
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            sections.forEach { section ->
                LegalSection(section = section)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to QuiketWhite.copy(alpha = 0f),
                        0.22f to QuiketWhite,
                    ),
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 24.dp),
        ) {
            QuiketPrimaryButton(
                text = "확인",
                enabled = true,
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun SettingsTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(DesignSystemRes.drawable.ic_topbar_back),
            contentDescription = "뒤로",
            tint = QuiketGray700,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onBackClick)
                .padding(10.dp),
        )
        Text(
            text = title,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun SettingsSection(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = QuiketGray700,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun SettingsRow(
    title: String,
    icon: DrawableResource,
    subtitle: String? = null,
    iconTint: Color = QuiketBrown950,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, QuiketGray100, shape)
            .background(QuiketWhite)
            .semantics { contentDescription = title }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (enabled) QuiketGray50 else QuiketGray100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (enabled) iconTint else QuiketGray400,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                color = if (enabled) QuiketGray950 else QuiketGray400,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = QuiketGray600,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Icon(
            painter = painterResource(DesignSystemRes.drawable.ic_next),
            contentDescription = null,
            tint = if (enabled) QuiketGray700 else QuiketGray400,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun LegalSection(
    section: LegalSectionData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = section.title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        if (section.body.isNotBlank()) {
            Text(
                text = section.body,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class LegalSectionData(
    val title: String,
    val body: String,
)

private fun String.toLegalSections(): List<LegalSectionData> {
    val sections = mutableListOf<LegalSectionData>()
    var currentTitle: String? = null
    val currentBody = mutableListOf<String>()

    fun flush() {
        val title = currentTitle ?: return
        sections += LegalSectionData(
            title = title,
            body = currentBody.joinToString("\n").trim(),
        )
        currentBody.clear()
    }

    lines()
        .drop(1)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach { line ->
            if (line.isLegalSectionTitle()) {
                flush()
                currentTitle = line
            } else {
                currentBody += line
            }
        }

    flush()
    return sections
}

private fun String.isLegalSectionTitle(): Boolean =
    this == "부칙" || ArticleTitleRegex.matches(this)

private val ArticleTitleRegex = Regex("""^제\d+조\s*(\([^)]+\)|\.)""")
