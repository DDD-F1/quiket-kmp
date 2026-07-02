package com.f1.quiket.feature.home.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Blue100
import com.f1.quiket.core.designsystem.theme.Blue500
import com.f1.quiket.core.designsystem.theme.Brown100
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray200
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.Orange100
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.Positive
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.home.R
import com.f1.quiket.feature.home.presentation.model.ActivityType

@Composable
fun HomeActivityCard(
    title: String,
    questionCount: Int,
    activityType: ActivityType,
    description: String,
    progressPercent: Int? = null,
    isQuizCreated: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Brown50,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 아이콘 영역
            ActivityTypeIcon(activityType = activityType)

            // 세로 Divider
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(1.dp)
                    .height(64.dp)
                    .background(color = Gray200)
            )

            // 중앙 텍스트 + 진행바 영역
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 제목
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Gray950,
                    maxLines = 1
                )

                // 문제 수
                Text(
                    text = "${questionCount}문제",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray900
                )

                // 진행률 바 (progressPercent가 있을 때만)
                if (progressPercent != null) {
                    ActivityProgressBar(
                        percent = progressPercent,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // 설명 문구
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray600,
                    maxLines = 1
                )

                // 퀴즈 상태 칩
                ActivityStatusChip(isQuizCreated = isQuizCreated)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 오른쪽 화살표
            Icon(
                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_subject_card_next),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ActivityTypeIcon(activityType: ActivityType) {
    val iconRes = when (activityType) {
        ActivityType.MULTIPLE_CHOICE -> R.drawable.ic_quiz_multiple
        ActivityType.SHORT_ANSWER -> R.drawable.ic_quiz_short
        ActivityType.OX_QUIZ -> R.drawable.ic_quiz_ox
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = Brown100, shape = RoundedCornerShape(1000.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = activityType.label,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = activityType.label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray800
        )
    }
}

@Composable
private fun ActivityProgressBar(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val clampedPercent = percent.coerceIn(0, 100)
    val dotCount = 10
    val filledDots = (clampedPercent / 100f * dotCount).toInt()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 12.dp)
                    .background(
                        color = if (index < filledDots) Positive else Negative,
                        shape = RoundedCornerShape(1000.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$clampedPercent%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Gray900
        )
    }
}

@Composable
private fun ActivityStatusChip(isQuizCreated: Boolean) {
    val (text, bgColor, textColor) = if (isQuizCreated) {
        Triple("퀴즈 완료", Orange100, Orange500)
    } else {
        Triple("퀴즈 생성", Blue100, Blue500)
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
    }
}


// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HomeActivityCardQuizCreatedPreview() {
    QuiketTheme {
        HomeActivityCard(
            title = "서양철학사",
            questionCount = 30,
            activityType = ActivityType.MULTIPLE_CHOICE,
            description = "잘하고 있어요!",
            progressPercent = 56,
            isQuizCreated = true,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeActivityCardQuizNotCreatedPreview() {
    QuiketTheme {
        HomeActivityCard(
            title = "기획자의 피그마 실무 워크...",
            questionCount = 10,
            activityType = ActivityType.SHORT_ANSWER,
            description = "아직 퀴즈 문제를 풀지 않았어요!",
            progressPercent = null,
            isQuizCreated = false,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeActivityCardOxPreview() {
    QuiketTheme {
        HomeActivityCard(
            title = "SQLD",
            questionCount = 10,
            activityType = ActivityType.OX_QUIZ,
            description = "나머지 퀴즈로 이어서 풀어볼까요?",
            progressPercent = 70,
            isQuizCreated = false,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}