package com.f1.quiket.feature.floating.presentation.component.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.ChapterChip
import com.f1.quiket.core.designsystem.theme.Brown300
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900

@Composable
fun LectureShortCard(
    title: String,
    purpose: String,
    chapterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Brown50)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(95.dp)
                .background(Brown300)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxHeight()
        ) {
            ChapterChip(label = purpose)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Gray900,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp, end = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "챕터 ${chapterCount}개",
                style = MaterialTheme.typography.labelSmall,
                color = Gray800,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
        }
    }
}