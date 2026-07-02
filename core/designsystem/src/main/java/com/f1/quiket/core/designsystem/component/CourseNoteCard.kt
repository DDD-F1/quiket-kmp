package com.f1.quiket.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import com.f1.quiket.core.designsystem.modifier.dashedBorder
import com.f1.quiket.core.designsystem.theme.Brown100
import com.f1.quiket.core.designsystem.theme.Brown300
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown700
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.QuiketTheme

// 강의 노트 짧은 버전
@Composable
fun SubjectShortCard(
    title: String,
    chapter: String,
    isStarred: Boolean,
    onStarToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Brown50)
            .clickable { onClick() }
    ) {
        // 왼쪽 갈색 바
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
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = Gray900,
                    maxLines = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    painter = painterResource(
                        id = if (isStarred) {
                            R.drawable.ic_star_on
                        } else {
                            R.drawable.ic_star_off
                        }
                    ),
                    contentDescription = if (isStarred) "즐겨찾기 추가" else "즐겨찾기 해제",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onStarToggle() }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            ChapterChip(label = chapter)
        }
    }
}

// 강의 노트 긴 버전
@Composable
fun SubjectLongCard(
    title: String,
    chapter: String,
    part: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes trailingIconRes: Int = R.drawable.ic_subject_card_next,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Brown50)
            .clickable { onClick() }
    ) {
        // 왼쪽 갈색 바
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(95.dp)
                .background(Brown300)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, 8.dp)
                .fillMaxHeight()
        ) {
            ChapterChip(label = chapter)
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
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Icon(
                        contentDescription = null,
                        painter = painterResource(trailingIconRes),
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = part,
                style = MaterialTheme.typography.labelSmall,
                color = Gray800,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
fun ChapterChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Brown100)
            .padding(8.dp, 2.dp, 8.dp, 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Brown700
        )
    }
}


// 과목 추가 카드
@Composable
fun AddSubjectCard(
    title: String,
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
                strokeWidth = 2.dp,
                cornerRadius = 12.dp,
                dashLength = 7.dp,
                gapLength = 5.dp
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_subject_add),
                contentDescription = "과목 추가",
                tint = Color.Unspecified,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Gray500
            )
        }
    }
}

@Preview
@Composable
private fun SubjectCardPreview() {
    var isStarred by remember { mutableStateOf(false) }

    QuiketTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 강의명 한 줄
                SubjectShortCard(
                    title = "강의명 ",
                    chapter = "챕터 3",
                    isStarred = isStarred,
                    onStarToggle = { isStarred = !isStarred },
                    onClick = {},
                )

                // 강의명 두 줄
                SubjectShortCard(
                    title = "Android 앱 개발 with Kotlin",
                    chapter = "챕터 7",
                    isStarred = true,
                    onStarToggle = {},
                    onClick = {}
                )
            }

            Column(
                modifier = Modifier
                    .width(300.dp)
            ) {
                SubjectLongCard(
                    title = "강의명 최대 2줄까지 나오도록 테스트해보기",
                    chapter = "챕터 3",
                    onClick = {},
                    part = "파트 n개"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddSubjectCardPreview() {
    QuiketTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .width(150.dp),
        ) {
            AddSubjectCard(
                "과목 추가",
                onClick = {}
            )
        }
    }
}