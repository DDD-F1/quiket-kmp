package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Blue100
import com.f1.quiket.core.designsystem.theme.Blue300
import com.f1.quiket.core.designsystem.theme.Blue800
import com.f1.quiket.core.designsystem.theme.Brown100
import com.f1.quiket.core.designsystem.theme.Brown700
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Green100
import com.f1.quiket.core.designsystem.theme.Green300
import com.f1.quiket.core.designsystem.theme.Green800
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.presentation.route.createQuizSubjectSamples

@Composable
fun CreateQuizScopeScreen(
    subject: QuizSubjectUiModel,
    selectedPartIds: Set<String>,
    expandedChapterId: String?,
    onBackClick: () -> Unit,
    onChapterExpandClick: (String) -> Unit,
    onChapterSelectionClick: (QuizScopeChapterUiModel) -> Unit,
    onPartClick: (QuizScopePartUiModel) -> Unit,
    onClearAllClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedChapterCount = subject.chapters.count { chapter ->
        chapter.parts.any { part -> part.id in selectedPartIds }
    }
    val selectedPartCount = selectedPartIds.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 92.dp),
        ) {
            CreateQuizTopBar(
                title = "범위 선택",
                onBackClick = onBackClick,
            )
            CreateQuizStepHeader(
                currentStep = 2,
                breadcrumbItems = listOf("선택된 과목", subject.name),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ScopeSelectionSection(
                    subject = subject,
                    selectedPartIds = selectedPartIds,
                    expandedChapterId = expandedChapterId,
                    onChapterExpandClick = onChapterExpandClick,
                    onChapterSelectionClick = onChapterSelectionClick,
                    onPartClick = onPartClick,
                    onClearAllClick = onClearAllClick,
                )
                ScopeSelectionSummary(
                    allSelected = selectedChapterCount == subject.chapterCount &&
                        selectedPartCount == subject.partCount,
                    selectedChapterCount = selectedChapterCount,
                    selectedPartCount = selectedPartCount,
                )
            }
        }

        QuiketPrimaryButton(
            text = "다음",
            enabled = selectedPartIds.isNotEmpty(),
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )
    }
}

@Composable
private fun ScopeSelectionSection(
    subject: QuizSubjectUiModel,
    selectedPartIds: Set<String>,
    expandedChapterId: String?,
    onChapterExpandClick: (String) -> Unit,
    onChapterSelectionClick: (QuizScopeChapterUiModel) -> Unit,
    onPartClick: (QuizScopePartUiModel) -> Unit,
    onClearAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(27.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "출제 범위",
                color = Gray950,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "전체 해제",
                color = Gray600,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = onClearAllClick,
                ),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            subject.chapters.forEach { chapter ->
                val chapterSelectedPartIds = chapter.parts
                    .map { part -> part.id }
                    .filter { partId -> partId in selectedPartIds }
                    .toSet()
                ScopeChapterCard(
                    chapter = chapter,
                    expanded = expandedChapterId == chapter.id,
                    selectedPartIds = chapterSelectedPartIds,
                    onExpandClick = { onChapterExpandClick(chapter.id) },
                    onChapterSelectionClick = { onChapterSelectionClick(chapter) },
                    onPartClick = onPartClick,
                )
            }
        }
    }
}

@Composable
private fun ScopeChapterCard(
    chapter: QuizScopeChapterUiModel,
    expanded: Boolean,
    selectedPartIds: Set<String>,
    onExpandClick: () -> Unit,
    onChapterSelectionClick: () -> Unit,
    onPartClick: (QuizScopePartUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val allPartsSelected = chapter.parts.isNotEmpty() &&
        chapter.parts.all { part -> part.id in selectedPartIds }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (expanded) 185.dp else 70.dp)
            .clip(shape)
            .background(if (expanded) White else Gray50)
            .then(
                if (expanded) {
                    Modifier.border(
                        width = 2.dp,
                        color = Brown950,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        ScopeChapterHeader(
            chapter = chapter,
            expanded = expanded,
            selected = allPartsSelected,
            onExpandClick = onExpandClick,
            onSelectionClick = onChapterSelectionClick,
        )
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                chapter.parts.forEach { part ->
                    ScopePartRow(
                        part = part,
                        selected = part.id in selectedPartIds,
                        onClick = { onPartClick(part) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeChapterHeader(
    chapter: QuizScopeChapterUiModel,
    expanded: Boolean,
    selected: Boolean,
    onExpandClick: () -> Unit,
    onSelectionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScopeExpandIcon(
            expanded = expanded,
            modifier = Modifier
                .size(24.dp)
                .semantics {
                    contentDescription = if (expanded) {
                        "${chapter.title} 접기"
                    } else {
                        "${chapter.title} 펼치기"
                    }
                }
                .clickable(
                    role = Role.Button,
                    onClick = onExpandClick,
                ),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = chapter.title,
                color = Gray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChapterChip(
                    count = chapter.chapterNumber,
                    selected = expanded,
                )
                Text(
                    text = "파트 ${chapter.parts.size}개 포함",
                    color = Gray600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
        ScopeCheckbox(
            checked = selected,
            contentDescription = if (selected) {
                "${chapter.title} 선택 해제"
            } else {
                "${chapter.title} 선택"
            },
            onClick = onSelectionClick,
        )
    }
}

@Composable
private fun ScopePartRow(
    part: QuizScopePartUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(37.dp)
            .background(Brown100)
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = part.title,
            color = Gray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.weight(1f),
        )
        ScopeCheckMark(
            color = if (selected) Brown950 else Gray400,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ScopeSelectionSummary(
    allSelected: Boolean,
    selectedChapterCount: Int,
    selectedPartCount: Int,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (allSelected) Green100 else Blue100
    val borderColor = if (allSelected) Green300 else Blue300
    val accentColor = if (allSelected) Green800 else Blue800

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(73.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .dashedBorder(
                color = borderColor,
                cornerRadius = 12.dp,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = if (allSelected) "전체 범위 선택" else "부분 범위 선택",
            color = Gray950,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "챕터 ${selectedChapterCount}개",
                color = accentColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(1000.dp))
                    .background(accentColor),
            )
            Text(
                text = "파트 ${selectedPartCount}개를 선택했어요",
                color = accentColor,
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
private fun ScopeCheckbox(
    checked: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brown950),
                contentAlignment = Alignment.Center,
            ) {
                ScopeCheckMark(
                    color = White,
                    modifier = Modifier.size(14.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = Gray500,
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

@Composable
private fun ScopeCheckMark(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.53f),
            end = Offset(size.width * 0.42f, size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.75f),
            end = Offset(size.width * 0.82f, size.height * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ScopeExpandIcon(
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        if (expanded) {
            drawLine(
                color = Gray600,
                start = Offset(size.width * 0.2f, size.height * 0.38f),
                end = Offset(size.width * 0.5f, size.height * 0.66f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Gray600,
                start = Offset(size.width * 0.5f, size.height * 0.66f),
                end = Offset(size.width * 0.8f, size.height * 0.38f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        } else {
            drawLine(
                color = Gray600,
                start = Offset(size.width * 0.36f, size.height * 0.18f),
                end = Offset(size.width * 0.66f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Gray600,
                start = Offset(size.width * 0.66f, size.height * 0.5f),
                end = Offset(size.width * 0.36f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateQuizScopeScreenAllSelectedPreview() {
    val subject = createQuizSubjectSamples()[0]

    QuiketTheme {
        CreateQuizScopeScreen(
            subject = subject,
            selectedPartIds = subject.chapters.flatMap { chapter ->
                chapter.parts.map { part -> part.id }
            }.toSet(),
            expandedChapterId = null,
            onBackClick = {},
            onChapterExpandClick = {},
            onChapterSelectionClick = {},
            onPartClick = {},
            onClearAllClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateQuizScopeScreenExpandedPreview() {
    val subject = createQuizSubjectSamples()[0]

    QuiketTheme {
        CreateQuizScopeScreen(
            subject = subject,
            selectedPartIds = subject.chapters.flatMap { chapter ->
                chapter.parts.map { part -> part.id }
            }.toSet(),
            expandedChapterId = "sqld-basic",
            onBackClick = {},
            onChapterExpandClick = {},
            onChapterSelectionClick = {},
            onPartClick = {},
            onClearAllClick = {},
            onNextClick = {},
        )
    }
}
