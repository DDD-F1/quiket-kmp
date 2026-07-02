package com.f1.quiket.feature.mypage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

@Composable
fun AlarmSettingScreen(
    state: AlarmSettingState,
    onIntent: (AlarmSettingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState()),
    ) {
        AlarmSettingTopBar(onBackClick = { onIntent(AlarmSettingIntent.NavigateBack) })

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Orange500)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                AlarmToggleRow(
                    title = "활동 알림",
                    subtitle = "퀴즈 생성 완료 등 주요 활동 알림",
                    enabled = state.activityEnabled,
                    isSaving = state.isSaving,
                    onToggle = { onIntent(AlarmSettingIntent.ToggleActivity(it)) },
                )
                Spacer(modifier = Modifier.height(12.dp))
                AlarmToggleRow(
                    title = "업데이트 알림",
                    subtitle = "새로운 기능 및 공지사항 알림",
                    enabled = state.updateEnabled,
                    isSaving = state.isSaving,
                    onToggle = { onIntent(AlarmSettingIntent.ToggleUpdate(it)) },
                )
                Spacer(modifier = Modifier.height(12.dp))
                AlarmToggleRow(
                    title = "복습 알림",
                    subtitle = "학습 복습 주기 알림",
                    enabled = state.reviewEnabled,
                    isSaving = state.isSaving,
                    onToggle = { onIntent(AlarmSettingIntent.ToggleReview(it)) },
                )
            }
        }
    }
}

@Composable
private fun AlarmSettingTopBar(onBackClick: () -> Unit) {
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
            text = "알림 설정",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray950,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun AlarmToggleRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    isSaving: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Gray100, RoundedCornerShape(12.dp))
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Gray950,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Gray700,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = { if (!isSaving) onToggle(it) },
            enabled = !isSaving,
            colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = Orange500),
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "알림 설정 — 로딩 중")
@Composable
private fun AlarmSettingScreenLoadingPreview() {
    QuiketTheme {
        AlarmSettingScreen(
            state = AlarmSettingState(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "알림 설정 — 모두 켜짐")
@Composable
private fun AlarmSettingScreenAllOnPreview() {
    QuiketTheme {
        AlarmSettingScreen(
            state = AlarmSettingState(
                isLoading = false,
                activityEnabled = true,
                updateEnabled = true,
                reviewEnabled = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "알림 설정 — 일부 꺼짐")
@Composable
private fun AlarmSettingScreenMixedPreview() {
    QuiketTheme {
        AlarmSettingScreen(
            state = AlarmSettingState(
                isLoading = false,
                activityEnabled = true,
                updateEnabled = false,
                reviewEnabled = false,
            ),
            onIntent = {},
        )
    }
}