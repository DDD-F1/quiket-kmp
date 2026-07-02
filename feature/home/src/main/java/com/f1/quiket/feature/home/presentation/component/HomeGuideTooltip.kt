package com.f1.quiket.feature.home.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Brown100
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.feature.home.R

@Composable
fun HomeGuideTooltip(
    onClose: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 14.dp, height = 8.dp)
                .offset(x = (-12).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color = Brown100)
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Brown100)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_guide_tootip),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.clip(RoundedCornerShape(1000.dp))
                    .size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "가이드북을 클릭해서 앱을 \n한 번 둘러볼까요?",
                color = Gray950,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painterResource(R.drawable.ic_home_guide_tooltip_close),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.clickable { onClose() }
                    .align(Alignment.Top)
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeGuideTooltipPreview() {
    HomeGuideTooltip(
        onClose = {},
        modifier = Modifier
    )
}