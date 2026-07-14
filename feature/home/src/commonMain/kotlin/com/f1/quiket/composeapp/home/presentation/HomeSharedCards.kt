package com.f1.quiket.composeapp.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.home.domain.model.HomeData
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.core.designsystem.resources.Res
import com.f1.quiket.core.designsystem.resources.ic_acorn

@Composable
fun HomeSummaryCard(
    homeData: HomeData,
    modifier: Modifier = Modifier,
) {
    val levelName = homeData.user.levelName?.takeIf { it.isNotBlank() } ?: "Lv.${homeData.user.currentLevel}"
    HomeSummaryCard(
        title = "$levelName · 도토리 ${homeData.user.dotoriBalance}개",
        description = "과목 ${homeData.subjects.size}개와 최근 활동 ${homeData.recentActivities.size}개를 이어서 볼 수 있어요.",
        modifier = modifier,
    )
}

@Composable
fun HomeSummaryCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    @Composable
    fun Content() {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(QuiketOrange500),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_acorn),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    color = QuiketWhite,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = description,
                    color = QuiketGray300,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = QuiketBrown950,
            shape = RoundedCornerShape(24.dp),
        ) {
            Content()
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            color = QuiketBrown950,
            shape = RoundedCornerShape(24.dp),
        ) {
            Content()
        }
    }
}

@Composable
fun HomeErrorCard(
    message: String,
    onRetry: () -> Unit,
    title: String = "홈 정보를 불러오지 못했어요",
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(20.dp),
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
                onClick = onRetry,
            )
        }
    }
}
