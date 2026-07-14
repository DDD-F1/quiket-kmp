package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.subject.domain.model.*

import org.koin.compose.koinInject
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.designsystem.QuiketBlack
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.subject.presentation.PartDetailStateHolder
import com.f1.quiket.composeapp.subject.presentation.PartDetailUiState
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailUiState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_detail_etc
import com.f1.quiket.core.designsystem.resources.ic_detail_edit
import com.f1.quiket.core.designsystem.resources.ic_detail_remove
import com.f1.quiket.core.designsystem.resources.ic_home_make
import com.f1.quiket.core.designsystem.resources.ic_home_upload
import com.f1.quiket.feature.subject.resources.Res
import com.f1.quiket.feature.subject.resources.ic_detail_back
import com.f1.quiket.feature.subject.resources.ic_detail_lecture_list
import com.f1.quiket.feature.subject.resources.ic_detail_quiket
import com.f1.quiket.feature.subject.resources.ic_star_off
import com.f1.quiket.feature.subject.resources.ic_star_on


@Composable
internal fun PartDetailRoute(
    subject: SubjectDetail,
    initialPartId: String,
    onBackClick: () -> Unit,
    onPartUpdated: (PartDetail) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val partEntries = remember(subject) {
        subject.chapters
            .sortedBy { it.displayOrder }
            .flatMap { chapter ->
                chapter.parts.sortedBy { it.partNumber }.map { part ->
                    PartEntry(
                        chapterId = chapter.id,
                        chapterName = chapter.name,
                        chapterNumber = chapter.displayOrder,
                        part = part,
                    )
                }
            }
    }
    var currentPartId by remember(subject.id, initialPartId) { mutableStateOf(initialPartId) }
    var isTocVisible by remember(subject.id) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<PartDetailStateHolder>()

    val currentEntry = partEntries.firstOrNull { it.part.id == currentPartId }
    val currentIndex = partEntries.indexOfFirst { it.part.id == currentPartId }

    LaunchedEffect(currentPartId, stateHolder) {
        stateHolder.loadPart(
            partId = currentPartId,
            onSessionExpired = onSessionExpired,
        )
    }

    PartDetailScreen(
        subjectName = subject.name,
        chapters = subject.chapters,
        entry = currentEntry,
        state = stateHolder.state,
        isEditMode = stateHolder.isEditMode,
        draftName = stateHolder.draftName,
        draftContent = stateHolder.draftContent,
        isSaving = stateHolder.isSaving,
        feedbackMessage = stateHolder.feedbackMessage,
        hasPrevious = currentIndex > 0,
        hasNext = currentIndex >= 0 && currentIndex < partEntries.lastIndex,
        currentIndex = currentIndex.coerceAtLeast(0),
        totalCount = partEntries.size.coerceAtLeast(1),
        currentPartId = currentPartId,
        isTocVisible = isTocVisible,
        onBackClick = onBackClick,
        onTocClick = { isTocVisible = true },
        onTocDismiss = { isTocVisible = false },
        onTocPartClick = { partId ->
            currentPartId = partId
            isTocVisible = false
        },
        onRetryClick = {
            coroutineScope.launch {
                stateHolder.loadPart(
                    partId = currentPartId,
                    onSessionExpired = onSessionExpired,
                )
            }
        },
        onPreviousClick = {
            if (currentIndex > 0) {
                currentPartId = partEntries[currentIndex - 1].part.id
            }
        },
        onNextClick = {
            if (currentIndex >= 0 && currentIndex < partEntries.lastIndex) {
                currentPartId = partEntries[currentIndex + 1].part.id
            }
        },
        onEditClick = stateHolder::enterEditMode,
        onCancelEdit = stateHolder::cancelEdit,
        onDraftNameChange = stateHolder::updateDraftName,
        onDraftContentChange = stateHolder::updateDraftContent,
        onSaveEdit = {
            coroutineScope.launch {
                stateHolder.saveEdit(
                    partId = currentPartId,
                    onSessionExpired = onSessionExpired,
                )?.let(onPartUpdated)
            }
        },
        modifier = modifier,
    )
}
@Composable
internal fun PartDetailScreen(
    subjectName: String,
    chapters: List<ChapterWithParts>,
    entry: PartEntry?,
    state: PartDetailUiState,
    isEditMode: Boolean,
    draftName: String,
    draftContent: String,
    isSaving: Boolean,
    feedbackMessage: String?,
    hasPrevious: Boolean,
    hasNext: Boolean,
    currentIndex: Int,
    totalCount: Int,
    currentPartId: String,
    isTocVisible: Boolean,
    onBackClick: () -> Unit,
    onTocClick: () -> Unit,
    onTocDismiss: () -> Unit,
    onTocPartClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onEditClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onDraftNameChange: (String) -> Unit,
    onDraftContentChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            LectureViewTopBar(
                title = subjectName,
                onBackClick = onBackClick,
                onTocClick = onTocClick,
                onEditPartNameClick = onEditClick,
                onEditContentClick = onEditClick,
            )

            if (!feedbackMessage.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    color = QuiketWhite,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = feedbackMessage,
                        color = if (feedbackMessage.contains("입력")) QuiketNegative else QuiketGray700,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LectureViewBreadcrumb(
                        chapterNumber = entry?.chapterNumber,
                        partNumber = entry?.part?.partNumber,
                        partTitle = entry?.part?.name.orEmpty(),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(QuiketBrown50),
                    ) {
                        when (state) {
                            PartDetailUiState.Loading -> PartMessage(
                                message = "파트 내용을 불러오는 중이에요",
                                modifier = Modifier.align(Alignment.Center),
                            )

                            is PartDetailUiState.Error -> PartMessage(
                                message = state.message,
                                buttonText = "다시 시도",
                                onButtonClick = onRetryClick,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 20.dp),
                            )

                            is PartDetailUiState.Success -> {
                                if (isEditMode) {
                                    PartEditContent(
                                        draftName = draftName,
                                        draftContent = draftContent,
                                        onDraftNameChange = onDraftNameChange,
                                        onDraftContentChange = onDraftContentChange,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    PartReadContent(
                                        part = state.part,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PartBottomBar(
                isEditMode = isEditMode,
                isSaving = isSaving,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                currentIndex = currentIndex,
                totalCount = totalCount,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                onCancelEdit = onCancelEdit,
                onSaveEdit = onSaveEdit,
            )
        }

        if (isTocVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(onClick = onTocDismiss)
                    .zIndex(1f),
            )
        }

        AnimatedVisibility(
            visible = isTocVisible,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .zIndex(2f),
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
        ) {
            PartTocPanel(
                chapters = chapters,
                selectedPartId = currentPartId,
                onPartClick = onTocPartClick,
                onCloseClick = onTocDismiss,
            )
        }
    }
}

@Composable
internal fun LectureViewTopBar(
    title: String,
    onBackClick: () -> Unit,
    onTocClick: () -> Unit,
    onEditPartNameClick: () -> Unit,
    onEditContentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(QuiketWhite)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ImageIconButton(
            icon = Res.drawable.ic_detail_back,
            contentDescription = "뒤로가기",
            onClick = onBackClick,
        )
        Text(
            text = title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        ImageIconButton(
            icon = Res.drawable.ic_detail_lecture_list,
            contentDescription = "목차",
            onClick = onTocClick,
        )
        Box {
            ImageIconButton(
                icon = DesignSystemRes.drawable.ic_detail_etc,
                contentDescription = "더보기",
                onClick = { showMoreMenu = true },
            )
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                containerColor = QuiketWhite,
                modifier = Modifier.padding(4.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                LectureViewDropdownItem(
                    title = "파트명 수정",
                    onDismiss = { showMoreMenu = false },
                    onClick = onEditPartNameClick,
                )
                LectureViewDropdownItem(
                    title = "내용 수정",
                    onDismiss = { showMoreMenu = false },
                    onClick = onEditContentClick,
                )
            }
        }
    }
}

@Composable
internal fun LectureViewDropdownItem(
    title: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        leadingIcon = {
            Image(
                painter = painterResource(DesignSystemRes.drawable.ic_detail_edit),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        text = {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
            )
        },
        onClick = {
            onDismiss()
            onClick()
        },
    )
}

@Composable
internal fun ImageIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription },
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = QuiketGray700,
        ),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
internal fun LectureViewBreadcrumb(
    chapterNumber: Int?,
    partNumber: Int?,
    partTitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(QuiketGray50)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "챕터 ${chapterNumber ?: "-"}",
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
        Text(
            text = ">",
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
        Text(
            text = "파트 ${partNumber ?: "-"}",
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
        Text(
            text = partTitle,
            color = QuiketGray600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PartTocPanel(
    chapters: List<ChapterWithParts>,
    selectedPartId: String,
    onPartClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(304.dp),
        color = QuiketWhite,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "목차",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "닫기",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onCloseClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                chapters
                    .sortedBy { it.displayOrder }
                    .forEach { chapter ->
                        PartTocChapter(
                            chapter = chapter,
                            selectedPartId = selectedPartId,
                            onPartClick = onPartClick,
                        )
                    }
            }
        }
    }
}

@Composable
internal fun PartTocChapter(
    chapter: ChapterWithParts,
    selectedPartId: String,
    onPartClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "챕터 ${chapter.displayOrder} ${chapter.name}",
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        )

        if (chapter.parts.isEmpty()) {
            Text(
                text = "파트가 없어요",
                color = QuiketGray600,
                style = MaterialTheme.typography.labelSmall,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                chapter.parts.sortedBy { it.partNumber }.forEach { part ->
                    PartTocRow(
                        part = part,
                        selected = part.id == selectedPartId,
                        onClick = { onPartClick(part.id) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun PartTocRow(
    part: PartSummary,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) QuiketBrown50 else QuiketGray50
    val labelColor = if (selected) QuiketBrown950 else QuiketGray600
    val titleColor = if (selected) QuiketBrown950 else QuiketGray950

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "파트 ${part.partNumber}",
            color = labelColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = part.name,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PartReadContent(
    part: PartDetail,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(
            text = part.content?.takeIf { it.isNotBlank() }
                ?: part.contentPreview?.takeIf { it.isNotBlank() }
                ?: "표시할 파트 내용이 없어요.",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            ),
        )
    }
}

@Composable
internal fun PartEditContent(
    draftName: String,
    draftContent: String,
    onDraftNameChange: (String) -> Unit,
    onDraftContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "파트명",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        QuiketTextField(
            value = draftName,
            onValueChange = onDraftNameChange,
            hint = "파트명을 입력해주세요",
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${draftName.trim().length}/${PartNameMaxLength}자",
            color = QuiketGray600,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End),
        )
        Text(
            text = "내용",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = QuiketWhite,
            shape = RoundedCornerShape(14.dp),
        ) {
            BasicTextField(
                value = draftContent,
                onValueChange = onDraftContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(14.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = QuiketGray950,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(QuiketBrown950),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (draftContent.isBlank()) {
                            Text(
                                text = "내용을 입력해주세요",
                                color = QuiketGray600,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
internal fun PartBottomBar(
    isEditMode: Boolean,
    isSaving: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    currentIndex: Int,
    totalCount: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        color = QuiketWhite,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PartBarButton(
                        text = "취소",
                        enabled = !isSaving,
                        primary = false,
                        onClick = onCancelEdit,
                        modifier = Modifier.weight(1f),
                    )
                    PartBarButton(
                        text = if (isSaving) "저장 중" else "저장",
                        enabled = !isSaving,
                        primary = true,
                        onClick = onSaveEdit,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                PartCircleNavigationButton(
                    label = "<",
                    contentDescription = "이전 파트",
                    enabled = hasPrevious,
                    onClick = onPreviousClick,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(QuiketBrown50)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "파트 ${currentIndex + 1}/$totalCount",
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    )
                }
                PartCircleNavigationButton(
                    label = ">",
                    contentDescription = "다음 파트",
                    enabled = hasNext,
                    onClick = onNextClick,
                )
            }
        }
    }
}

@Composable
internal fun PartCircleNavigationButton(
    label: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) QuiketBrown950 else QuiketGray100)
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = QuiketWhite,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
internal fun PartBarButton(
    text: String,
    enabled: Boolean,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        primary && enabled -> QuiketBrown950
        primary -> QuiketGray100
        else -> QuiketWhite
    }
    val content = when {
        primary && enabled -> QuiketWhite
        primary -> QuiketGray600
        enabled -> QuiketBrown950
        else -> QuiketGray600
    }
    Surface(
        modifier = modifier
            .height(44.dp)
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        color = background,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (primary) Color.Transparent else QuiketGray100,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = content,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
internal fun PartMessage(
    message: String,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = message,
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        )
        if (buttonText != null) {
            QuiketPrimaryButton(
                text = buttonText,
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
