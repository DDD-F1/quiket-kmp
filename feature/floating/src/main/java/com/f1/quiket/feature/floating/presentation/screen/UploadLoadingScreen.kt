package com.f1.quiket.feature.floating.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.f1.quiket.feature.floating.R

@Composable
fun UploadLoadingScreen(
    progress: Float,
    isFailed: Boolean = false,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        if (isFailed) {
            UploadFailedContent(
                onBack = onBack,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            )
        } else {
            UploadingContent(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun UploadingContent(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val progressPercent = (progress.coerceIn(0f, 1f) * 100).toInt()
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/upload_processing.json"))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "큐링이가 강의를 가져와 파트를\n열심히 나누고 있어요!",
                color = Gray950,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "화면을 나가면 업로드가 취소돼요\n보통 10~30초 정도 걸려요",
                color = Gray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Row(
            modifier = Modifier
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
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

@Composable
private fun UploadFailedContent(
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // 이미지 + 텍스트 (화면 중앙)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_uploading_fail),
                contentDescription = null,
                modifier = Modifier.size(320.dp, 350.dp),
                tint = Color.Unspecified,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "앗 ! 자료 추가 중에\n문제가 발생했어요.",
                    color = Gray950,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "네트워크 상태를 확인한 뒤\n다시 시도해주세요",
                    color = Gray700,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // 버튼 (하단 고정)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Brown950, RoundedCornerShape(12.dp))
                    .background(White)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "이전",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray950,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brown950)
                    .clickable(onClick = onRetry),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "다시 시도하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "업로드 중")
@Composable
private fun UploadLoadingScreenPreview() {
    QuiketTheme {
        UploadLoadingScreen(progress = 0.45f)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "업로드 실패")
@Composable
private fun UploadFailedScreenPreview() {
    QuiketTheme {
        UploadLoadingScreen(progress = 0f, isFailed = true)
    }
}