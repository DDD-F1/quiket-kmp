package com.f1.quiket.feature.mypage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.White

@Composable
fun MyPageSettingScreen(
    onIntent: (MyPageSettingIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState()),
    ) {
        SettingTopBar(onBackClick = { onIntent(MyPageSettingIntent.NavigateBack) })

        Spacer(modifier = Modifier.height(8.dp))

        SettingSectionLabel(label = "계정")

        SettingGroup {
            SettingRow(
                icon = com.f1.quiket.core.designsystem.R.drawable.ic_person,
                title = "계정 설정",
                subtitle = "닉네임, 이메일, 비밀번호",
                onClick = { onIntent(MyPageSettingIntent.NavigateToAccountSetting) },
            )

            SettingRow(
                icon = com.f1.quiket.core.designsystem.R.drawable.ic_alert,
                title = "알림 설정",
                subtitle = "활동, 업데이트, 복습 주기",
                onClick = { onIntent(MyPageSettingIntent.NavigateToAlarmSetting) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingSectionLabel(label = "앱")

        SettingGroup {
            SettingRow(
                icon = com.f1.quiket.core.designsystem.R.drawable.ic_info,
                title = "문의",
                iconTint = Gray400,
                onClick = { onIntent(MyPageSettingIntent.NavigateToInquiry) },
            )

            SettingRow(
                icon = com.f1.quiket.core.designsystem.R.drawable.ic_info,
                title = "이용 약관",
                iconTint = Gray400,
                onClick = { onIntent(MyPageSettingIntent.NavigateToTerms) },
            )

            SettingRow(
                icon = com.f1.quiket.core.designsystem.R.drawable.ic_info,
                title = "개인정보 처리방침",
                iconTint = Gray400,
                onClick = { onIntent(MyPageSettingIntent.NavigateToPrivacyPolicy) },
            )

            SettingRow(
                icon = com.f1.quiket.core.designsystem.R.drawable.ic_info,
                title = "앱 정보",
                iconTint = Gray400,
                onClick = { onIntent(MyPageSettingIntent.NavigateToAppInfo) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        LogoutButton(onClick = { onIntent(MyPageSettingIntent.Logout) })

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@Composable
private fun SettingTopBar(onBackClick: () -> Unit) {
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
            text = "설정",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray950,
        )
        Spacer(modifier = Modifier.weight(1f))
        // 우측 여백 정렬용
        Box(modifier = Modifier.size(24.dp))
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun SettingSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Normal
        ),
        color = Gray800,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

// ─── Setting Group 카드 ────────────────────────────────────────────────────────

@Composable
private fun SettingGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun SettingRow(
    icon: Int,
    title: String,
    subtitle: String? = null,
    iconTint: Color = Black,
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(1000.dp))
                .background(Gray50),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Gray950
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Gray700
                )
            }
        }

        Icon(
            painter = painterResource(id = com.f1.quiket.core.designsystem.R.drawable.ic_next),
            contentDescription = null,
            tint = Gray900,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyPageSettingScreenPreview() {
    QuiketTheme {
        MyPageSettingScreen(onIntent = {})
    }
}

// 로그아웃
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Quiket을 로그아웃 하실 건가요?",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Gray700,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "로그아웃",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Orange500,
            )
        }
    }
}