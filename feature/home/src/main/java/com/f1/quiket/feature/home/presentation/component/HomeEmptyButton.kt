package com.f1.quiket.feature.home.presentation.component

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.QuiketTheme

@Composable
fun HomeEmptySubjectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Gray50)
            .dashedBorder(
                color = Gray300,
                strokeWidth = 1.5.dp,
                cornerRadius = 16.dp,
                dashLength = 5.dp,
                gapLength = 4.dp
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "추가된 과목이 아직 없어요",
                style = MaterialTheme.typography.labelSmall,
                color = Gray800
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_subject_add),
                    contentDescription = "과목 추가",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "학습 중인 과목을 추가해 보세요",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray900
                )
            }
        }
    }
}

@Composable
fun HomeEmptyActivityButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Gray50)
            .dashedBorder(
                color = Gray300,
                strokeWidth = 1.5.dp,
                cornerRadius = 16.dp,
                dashLength = 5.dp,
                gapLength = 4.dp
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "활동이 아직 없어요",
                style = MaterialTheme.typography.labelSmall,
                color = Gray800
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "차근차근 하나씩 채워 나가 보아요",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray900
                )
            }
        }
    }
}

// 과목 추가 border 점선 Modifier
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 2.dp, // 단위를 Dp로 받아 기기 대응
    cornerRadius: Dp = 16.dp, // 더 깊은 라운드를 위해 기본값 상향
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
) = this.then(
    Modifier.drawWithContent {
        drawContent() // 기존 내용(텍스트 등)을 먼저 그림

        val strokeWidthPx = strokeWidth.toPx()
        val cornerRadiusPx = cornerRadius.toPx()
        val dashLengthPx = dashLength.toPx()
        val gapLengthPx = gapLength.toPx()

        // 선 두께의 절반만큼 안쪽으로 밀어넣어야 모서리가 안 잘리고 예쁘게 나옴
        val size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

        drawRoundRect(
            color = color,
            topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
            size = size,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(
                width = strokeWidthPx,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(dashLengthPx, gapLengthPx),
                    phase = 0f
                ),
                // 선 끝 처리를 둥글게 하면 더 부드러워 보임 (선택 사항)
                cap = StrokeCap.Round
            )
        )
    }
)

@Preview(showBackground = true)
@Composable
private fun HomeEmptyButtonPreview() {
    QuiketTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Column {
                HomeEmptySubjectButton(onClick = {})
                HomeEmptyActivityButton(onClick = {})
            }
        }
    }
}