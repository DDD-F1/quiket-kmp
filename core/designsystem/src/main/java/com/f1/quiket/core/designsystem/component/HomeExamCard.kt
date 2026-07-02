package com.f1.quiket.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

@Composable
fun HomeExamCard(
    examName: String,
    date: String,
    dDay: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrgent = when {
        dDay == "D-Day" -> true
        dDay.startsWith("D-") -> dDay.removePrefix("D-").toIntOrNull()?.let { it in 0..7 } ?: false
        else -> false
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(1000.dp))
            .background(White)
            .clickable { onClick() }
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Gray50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_exam),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = examName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Gray950,
            )
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = Gray900,
            )
        }

        if (dDay.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isUrgent) Negative else Brown950)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = dDay,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                    color = White
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeExamCardPreview() {
    QuiketTheme {
        HomeExamCard(
            examName = "정보처리기사",
            date = "2026.06.28",
            dDay = "D-60",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}