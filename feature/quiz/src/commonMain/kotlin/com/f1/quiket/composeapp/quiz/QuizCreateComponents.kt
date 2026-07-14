package com.f1.quiket.composeapp.quiz

import org.koin.compose.koinInject
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.composeapp.designsystem.QuiketBrown100
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray500
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray800
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStep
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizTypeOption
import com.f1.quiket.composeapp.quiz.presentation.icon
import com.f1.quiket.composeapp.subject.domain.model.PartSummary
import com.f1.quiket.composeapp.subject.domain.model.SubjectListItem
import com.f1.quiket.composeapp.util.hidePlatformKeyboard
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.feature.quiz.resources.Res
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_acorn
import com.f1.quiket.core.designsystem.resources.ic_detail_edit
import com.f1.quiket.core.designsystem.resources.ic_qring_profile


@Composable
internal fun QuizStepScaffold(
    title: String,
    onBackClick: () -> Unit,
    bottomButtonText: String,
    bottomButtonEnabled: Boolean,
    onBottomButtonClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
        ) {
            QuizCreateTopBar(
                title = title,
                onBackClick = onBackClick,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .background(QuiketWhite)
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 28.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            QuiketPrimaryButton(
                text = bottomButtonText,
                enabled = bottomButtonEnabled,
                onClick = onBottomButtonClick,
            )
        }
    }
}

@Composable
internal fun QuizStepHeader(
    currentStep: Int,
    breadcrumbItems: List<String> = emptyList(),
) {
    val totalSteps = 3
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(21.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (index < currentStep) QuiketBrown950 else QuiketGray100),
                )
            }
            Text(
                text = "$currentStep/$totalSteps",
                color = QuiketGray900,
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
        if (breadcrumbItems.isNotEmpty()) {
            QuizBreadcrumb(items = breadcrumbItems)
        }
    }
}

@Composable
internal fun QuizBreadcrumb(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .quizHorizontalFullBleed()
            .fillMaxWidth()
            .height(37.dp)
            .background(QuiketGray50)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            Text(
                text = item,
                color = if (index == 0) QuiketGray500 else QuiketGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = if (index == 0) FontWeight.Medium else FontWeight.Normal,
                ),
                modifier = if (isLast) Modifier.weight(1f) else Modifier,
            )
            if (!isLast) {
                QuizChevronRight(
                    color = QuiketGray400,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

internal fun Modifier.quizHorizontalFullBleed(): Modifier = layout { measurable, constraints ->
    val insetPx = 16.dp.roundToPx()
    val extraWidth = insetPx * 2
    val expandedMaxWidth = if (constraints.hasBoundedWidth) {
        constraints.maxWidth + extraWidth
    } else {
        constraints.maxWidth
    }
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = (constraints.minWidth + extraWidth).coerceAtMost(expandedMaxWidth),
            maxWidth = expandedMaxWidth,
        ),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.placeRelative(-insetPx, 0)
    }
}

@Composable
internal fun QuizChevronRight(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height * 0.2f),
            end = Offset(size.width * 0.64f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun QuizCreateTitle(
    title: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = description,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun QuizAddSubjectCard(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(QuiketGray50)
            .border(1.dp, QuiketGray300, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+ 과목 추가",
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
internal fun QuizScopeChapterCard(
    chapterName: String,
    chapterNumber: Int,
    parts: List<PartSummary>,
    selectedPartIds: Set<String>,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    onChapterClick: () -> Unit,
    onPartClick: (PartSummary) -> Unit,
) {
    val allSelected = parts.isNotEmpty() && parts.all { part -> part.id in selectedPartIds }
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (expanded) 185.dp else 70.dp)
            .clip(shape)
            .background(if (expanded) QuiketWhite else QuiketGray50)
            .border(if (expanded) 2.dp else 0.dp, if (expanded) QuiketBrown950 else Color.Transparent, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuizScopeExpandIcon(
                expanded = expanded,
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        contentDescription = if (expanded) {
                            "$chapterName 접기"
                        } else {
                            "$chapterName 펼치기"
                        }
                    }
                    .clickable(role = Role.Button, onClick = onExpandClick),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = chapterName,
                    color = QuiketGray950,
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
                    QuizScopeChapterChip(
                        count = chapterNumber,
                        selected = expanded,
                    )
                    Text(
                        text = "파트 ${parts.size}개 포함",
                        color = QuiketGray600,
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
            QuizCheckbox(
                checked = allSelected,
                onClick = onChapterClick,
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            parts.forEach { part ->
                QuizPartRow(
                    part = part,
                    selected = part.id in selectedPartIds,
                    onClick = { onPartClick(part) },
                )
            }
        }
    }
}

@Composable
internal fun QuizScopeChapterChip(
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(RoundedCornerShape(1000.dp))
            .background(if (selected) QuiketBrown100 else QuiketGray100)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "챕터 $count",
            color = if (selected) QuizBrown700 else QuiketGray800,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
internal fun QuizPartRow(
    part: PartSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(37.dp)
            .background(QuiketBrown100)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = part.name,
            color = QuiketGray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.weight(1f),
        )
        QuizCheckMark(
            color = if (selected) QuiketBrown950 else QuiketGray400,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
internal fun QuizCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable(role = Role.Checkbox, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(QuiketBrown950),
                contentAlignment = Alignment.Center,
            ) {
                QuizCheckMark(
                    color = QuiketWhite,
                    modifier = Modifier.size(14.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, QuiketGray500, RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Composable
internal fun QuizCheckMark(
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
internal fun QuizScopeSummary(
    allSelected: Boolean,
    selectedChapterCount: Int,
    selectedPartCount: Int,
) {
    val backgroundColor = if (allSelected) QuizGreen100 else QuizBlue100
    val borderColor = if (allSelected) QuizGreen300 else QuizBlue300
    val accentColor = if (allSelected) QuizGreen800 else QuizBlue800

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(73.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .quizDashedBorder(
                color = borderColor,
                cornerRadius = 12.dp,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = if (allSelected) "전체 범위 선택" else "부분 범위 선택",
            color = QuiketGray950,
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

internal fun Modifier.quizDashedBorder(
    color: Color,
    cornerRadius: Dp,
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(8.dp.toPx(), 4.dp.toPx()),
            ),
        ),
    )
}

@Composable
internal fun QuizScopeExpandIcon(
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        if (expanded) {
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.2f, size.height * 0.38f),
                end = Offset(size.width * 0.5f, size.height * 0.66f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.5f, size.height * 0.66f),
                end = Offset(size.width * 0.8f, size.height * 0.38f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        } else {
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.36f, size.height * 0.18f),
                end = Offset(size.width * 0.66f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.66f, size.height * 0.5f),
                end = Offset(size.width * 0.36f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun QuizCreateTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        Text(
            text = "‹",
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 50.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onBackClick)
                .semantics { contentDescription = "뒤로" },
        )
        Text(
            text = title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp),
        )
    }
}

@Composable
internal fun QuizCreateSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(27.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        trailingContent?.invoke()
    }
}

@Composable
internal fun QuizTypeCard(
    option: QuizTypeOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .height(100.dp)
            .clip(shape)
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = shape,
            )
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) QuiketBrown100 else QuiketGray100),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(option.icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = option.title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun QuizTypeInfoButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(QuiketGray400)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "퀴즈 유형 설명 보기" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "i",
            color = QuiketWhite,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
internal fun QuizTypeTooltip(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray950,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "퀴즈 유형",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "x",
                    color = QuiketGray300,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .semantics { contentDescription = "퀴즈 유형 설명 닫기" },
                )
            }
            QuizTypeTooltipRow(
                title = "객관식 설명",
                description = "보기 중 정답을 선택해요.",
            )
            QuizTypeTooltipRow(
                title = "O/X 퀴즈 설명",
                description = "참/거짓 중 정답을 선택해요.",
            )
            QuizTypeTooltipRow(
                title = "플래시카드 설명",
                description = "빠르게 넘기며 암기해요.",
            )
            QuizTypeTooltipRow(
                title = "쪽지시험 설명",
                description = "단답형, 빈칸 등 답을 직접 입력해요.",
            )
        }
    }
}

@Composable
internal fun QuizTypeTooltipRow(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            color = QuiketWhite,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = description,
            color = QuiketWhite,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun QuizSubjectCard(
    subject: SubjectListItem,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(shape)
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = QuiketBrown950,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = subject.name,
                color = QuiketGray950,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuizSubjectChapterChip(
                    count = subject.chapterCount,
                    selected = selected,
                )
                Text(
                    text = "파트 ${subject.partCount}",
                    color = QuiketGray600,
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
    }
}

@Composable
internal fun QuizSubjectChapterChip(
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "챕터 $count",
        color = QuiketGray600,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
        ),
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) QuiketWhite else QuiketGray100)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
internal fun QuizSelectableCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) QuiketBrown950 else Color.Transparent
    val backgroundColor = if (selected) QuiketBrown50 else QuiketGray50
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) QuiketBrown950 else QuiketWhite)
                .border(1.dp, if (selected) QuiketBrown950 else QuiketGray300, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = description,
                color = QuiketGray600,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun QuizChoiceChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) QuiketBrown950 else QuiketGray50)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = if (selected) QuiketWhite else QuiketGray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(4.dp))
                trailingContent()
            }
        }
    }
}

@Composable
internal fun QuizCreateInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = description,
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall,
            )
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = QuiketBrown950,
                    trackColor = QuiketGray100,
                )
            }
            if (actionText != null && onActionClick != null) {
                QuiketPrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                )
            }
        }
    }
}

internal fun QuizScope.allParts(): List<PartSummary> =
    chapters
        .sortedBy { chapter -> chapter.displayOrder }
        .flatMap { chapter -> chapter.parts.sortedBy { part -> part.partNumber } }

internal fun QuizDifficulty.displayLabel(): String =
    when (this) {
        QuizDifficulty.Easy -> "쉬움"
        QuizDifficulty.Medium -> "보통"
        QuizDifficulty.Hard -> "어려움"
    }
