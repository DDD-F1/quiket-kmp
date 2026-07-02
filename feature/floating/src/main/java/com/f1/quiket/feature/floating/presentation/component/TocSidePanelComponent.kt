package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.Selected
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R
import com.f1.quiket.feature.floating.domain.model.TocChapter
import com.f1.quiket.feature.floating.domain.model.TocPart

@Composable
fun TocSidePanel(
    chapters: List<TocChapter>,
    selectedPartId: String?,
    onPartClick: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedChapterIds by remember { mutableStateOf(chapters.map { it.id }.toSet()) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .width(280.dp)
            .background(White)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "목차",
                style = MaterialTheme.typography.titleSmall,
                color = Gray950,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_addsubject_close),
                    contentDescription = "닫기",
                    tint = Gray700,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        HorizontalDivider(color = Gray100, thickness = 1.dp)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            chapters.forEach { chapter ->
                val isExpanded = chapter.id in expandedChapterIds
                val isChapterSelected = chapter.parts.any { it.id == selectedPartId }
                item(key = "chapter_${chapter.id}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isChapterSelected) Selected else Color.Transparent)
                            .clickable {
                                expandedChapterIds = if (isExpanded) {
                                    expandedChapterIds - chapter.id
                                } else {
                                    expandedChapterIds + chapter.id
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isChapterSelected) Brown950 else Gray700,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "챕터${chapter.number} ${chapter.title}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChapterSelected) Brown950 else Gray950,
                            ),
                        )
                    }
                }
                if (isExpanded) {
                    items(chapter.parts, key = { "part_${it.id}" }) { part ->
                        val isSelected = part.id == selectedPartId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) Selected else Color.Transparent)
                                .clickable { onPartClick(part.id) }
                                .padding(start = 44.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "파트 ${part.partNumber}  ${part.title}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) Brown950 else Gray700,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TocSidePanelPreview() {
    val sampleChapters = listOf(
        TocChapter(
            id = "1",
            number = 1,
            title = "SQLD 기본",
            parts = listOf(
                TocPart("1", 1, "데이터베이스 개념"),
                TocPart("2", 2, "데이터 모델링"),
                TocPart("3", 3, "정규화"),
            ),
        ),
        TocChapter(
            id = "2",
            number = 2,
            title = "데이터 모델",
            parts = listOf(
                TocPart("4", 1, "엔터티"),
                TocPart("5", 2, "속성"),
                TocPart("6", 3, "관계"),
                TocPart("7", 4, "식별자"),
            ),
        ),
    )
    QuiketTheme {
        TocSidePanel(
            chapters = sampleChapters,
            selectedPartId = "2",
            onPartClick = {},
            onClose = {},
        )
    }
}
