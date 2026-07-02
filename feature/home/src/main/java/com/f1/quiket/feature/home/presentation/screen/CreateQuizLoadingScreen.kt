package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

private const val QUIZ_LOADING_FULL_ASSET = "lottie/quiz_loading_full.json"

@Composable
fun CreateQuizLoadingScreen(
    progress: Float,
    rewardCount: Int,
    browseEnabled: Boolean,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressPercent = (progress.coerceIn(0f, 1f) * 100).toInt()
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(QUIZ_LOADING_FULL_ASSET),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 80.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "큐링이가 열심히\n퀴즈랑 도토리 배달을 준비하고 있어요!",
                        color = Gray950,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "잠시 쉬다 오셔도 돼요!\n배달이 완료되면 불러드릴게요",
                        color = Gray700,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                LoadingProgressRow(
                    progress = progress,
                    progressPercent = progressPercent,
                )
                RewardComment(rewardCount = rewardCount)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    enabled = browseEnabled,
                    role = Role.Button,
                    onClick = onBrowseClick,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (browseEnabled) "잠시 딴짓하러 가기" else "퀴즈 생성 요청 중이에요",
                color = if (browseEnabled) Brown950 else Gray700,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            LoadingChevronRight(
                modifier = Modifier.size(20.dp),
                color = if (browseEnabled) Brown950 else Gray700,
            )
        }
    }
}

@Composable
private fun LoadingProgressRow(
    progress: Float,
    progressPercent: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(21.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Gray100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brown950),
            )
        }
        Text(
            text = "$progressPercent%",
            color = Gray900,
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun RewardComment(
    rewardCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(21.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "완료 후",
            color = Gray900,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
        Image(
            painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_acorn),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = rewardCount.toString(),
            color = Brown950,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Text(
            text = "획득 예정이에요!",
            color = Gray900,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun LoadingChevronRight(
    modifier: Modifier = Modifier,
    color: Color = Brown950,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.36f, size.height * 0.18f),
            end = Offset(size.width * 0.66f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.66f, size.height * 0.5f),
            end = Offset(size.width * 0.36f, size.height * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateQuizLoadingScreenPreview() {
    QuiketTheme {
        CreateQuizLoadingScreen(
            progress = 0.4f,
            rewardCount = 10,
            browseEnabled = true,
            onBrowseClick = {},
        )
    }
}
