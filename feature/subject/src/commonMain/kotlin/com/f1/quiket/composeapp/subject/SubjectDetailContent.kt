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
internal fun SubjectDetailScreen(
    state: SubjectDetailUiState,
    showManageMenu: Boolean,
    isChapterEditMode: Boolean,
    isChapterDeleteMode: Boolean,
    managementMessage: String?,
    isManagementBusy: Boolean,
    isStarred: Boolean,
    onBackClick: () -> Unit,
    onChapterClick: (ChapterWithParts) -> Unit,
    onExamScheduleClick: () -> Unit,
    onUploadClick: () -> Unit,
    onCreateQuizClick: (SubjectDetail) -> Unit,
    onStarClick: () -> Unit,
    onManageMenuClick: () -> Unit,
    onManageMenuDismiss: () -> Unit,
    onEditSubjectNameClick: () -> Unit,
    onEditSubjectDetailsClick: () -> Unit,
    onEditChapterNameModeClick: () -> Unit,
    onDeleteSubjectClick: () -> Unit,
    onDeleteChapterModeClick: () -> Unit,
    onExitChapterManageMode: () -> Unit,
    onChapterEditClick: (ChapterWithParts) -> Unit,
    onChapterDeleteClick: (ChapterWithParts) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = QuiketBrown50,
    ) {
        when (state) {
            is SubjectDetailUiState.Loading -> SubjectMessageScaffold(
                title = state.subjectName,
                message = "과목 정보를 불러오는 중이에요",
                onBackClick = onBackClick,
            )

            is SubjectDetailUiState.Error -> SubjectMessageScaffold(
                title = state.subjectName,
                message = state.message,
                onBackClick = onBackClick,
            )

            is SubjectDetailUiState.Success -> SubjectDetailContent(
                subject = state.subject,
                showManageMenu = showManageMenu,
                isChapterEditMode = isChapterEditMode,
                isChapterDeleteMode = isChapterDeleteMode,
                managementMessage = managementMessage,
                isManagementBusy = isManagementBusy,
                isStarred = isStarred,
                onBackClick = onBackClick,
                onChapterClick = onChapterClick,
                onExamScheduleClick = onExamScheduleClick,
                onUploadClick = onUploadClick,
                onCreateQuizClick = onCreateQuizClick,
                onStarClick = onStarClick,
                onManageMenuClick = onManageMenuClick,
                onManageMenuDismiss = onManageMenuDismiss,
                onEditSubjectNameClick = onEditSubjectNameClick,
                onEditSubjectDetailsClick = onEditSubjectDetailsClick,
                onEditChapterNameModeClick = onEditChapterNameModeClick,
                onDeleteSubjectClick = onDeleteSubjectClick,
                onDeleteChapterModeClick = onDeleteChapterModeClick,
                onExitChapterManageMode = onExitChapterManageMode,
                onChapterEditClick = onChapterEditClick,
                onChapterDeleteClick = onChapterDeleteClick,
            )
        }
    }
}

@Composable
internal fun SubjectDetailContent(
    subject: SubjectDetail,
    showManageMenu: Boolean,
    isChapterEditMode: Boolean,
    isChapterDeleteMode: Boolean,
    managementMessage: String?,
    isManagementBusy: Boolean,
    isStarred: Boolean,
    onBackClick: () -> Unit,
    onChapterClick: (ChapterWithParts) -> Unit,
    onExamScheduleClick: () -> Unit,
    onUploadClick: () -> Unit,
    onCreateQuizClick: (SubjectDetail) -> Unit,
    onStarClick: () -> Unit,
    onManageMenuClick: () -> Unit,
    onManageMenuDismiss: () -> Unit,
    onEditSubjectNameClick: () -> Unit,
    onEditSubjectDetailsClick: () -> Unit,
    onEditChapterNameModeClick: () -> Unit,
    onDeleteSubjectClick: () -> Unit,
    onDeleteChapterModeClick: () -> Unit,
    onExitChapterManageMode: () -> Unit,
    onChapterEditClick: (ChapterWithParts) -> Unit,
    onChapterDeleteClick: (ChapterWithParts) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SubjectStatusSpacer()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SubjectGreen),
        ) {
            SubjectTopBar(
                title = subject.name,
                onBackClick = onBackClick,
                isStarred = isStarred,
                onStarClick = onStarClick,
                showManageMenu = showManageMenu,
                onManageMenuDismiss = onManageMenuDismiss,
                isManageEnabled = !isManagementBusy,
                onManageMenuClick = onManageMenuClick,
                onEditSubjectNameClick = onEditSubjectNameClick,
                onEditSubjectDetailsClick = onEditSubjectDetailsClick,
                onEditChapterNameModeClick = onEditChapterNameModeClick,
                onDeleteSubjectClick = onDeleteSubjectClick,
                onDeleteChapterModeClick = onDeleteChapterModeClick,
            )
            SubjectHeaderSection(
                subject = subject,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(QuiketWhite, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SubjectActionCard(
                    title = "자료 업로드",
                    icon = DesignSystemRes.drawable.ic_home_upload,
                    background = QuiketGray100,
                    onClick = onUploadClick,
                    modifier = Modifier
                        .height(103.dp)
                        .weight(1f),
                )
                SubjectActionCard(
                    title = "퀴즈 만들기",
                    icon = DesignSystemRes.drawable.ic_home_make,
                    background = QuiketOrange500,
                    onClick = { onCreateQuizClick(subject) },
                    modifier = Modifier
                        .height(103.dp)
                        .weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(QuiketBrown50),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SubjectExamCard(
                    schedule = subject.examSchedule,
                    fallbackName = subject.name,
                    onClick = onExamScheduleClick,
                    modifier = Modifier.padding(16.dp),
                )

                managementMessage?.takeIf { it.isNotBlank() }?.let { message ->
                    SubjectManagementMessage(
                        message = message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                SubjectChapterSection(
                    chapters = subject.chapters.sortedBy { it.displayOrder },
                    isEditMode = isChapterEditMode,
                    isDeleteMode = isChapterDeleteMode,
                    onExitChapterManageMode = onExitChapterManageMode,
                    onChapterClick = onChapterClick,
                    onChapterEditClick = onChapterEditClick,
                    onChapterDeleteClick = onChapterDeleteClick,
                    onChapterAddClick = onUploadClick,
                )
            }
        }
    }
}

@Composable
internal fun SubjectTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = QuiketWhite,
    iconColor: Color = QuiketWhite,
    isStarred: Boolean = false,
    onStarClick: (() -> Unit)? = null,
    showManageMenu: Boolean = false,
    onManageMenuDismiss: () -> Unit = {},
    isManageEnabled: Boolean = true,
    onManageMenuClick: (() -> Unit)? = null,
    onEditSubjectNameClick: () -> Unit = {},
    onEditSubjectDetailsClick: () -> Unit = {},
    onEditChapterNameModeClick: () -> Unit = {},
    onDeleteSubjectClick: () -> Unit = {},
    onDeleteChapterModeClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubjectBackButton(
            onClick = onBackClick,
            iconColor = iconColor,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = title,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .padding(start = 20.dp)
                .weight(1f),
        )

        if (onStarClick != null) {
            SubjectImageIconButton(
                icon = if (isStarred) Res.drawable.ic_star_on else Res.drawable.ic_star_off,
                contentDescription = if (isStarred) "즐겨찾기 해제" else "즐겨찾기 추가",
                onClick = onStarClick,
            )
        }

        if (onManageMenuClick != null) {
            Box {
                SubjectImageIconButton(
                    icon = DesignSystemRes.drawable.ic_detail_etc,
                    contentDescription = "메뉴",
                    enabled = isManageEnabled,
                    onClick = onManageMenuClick,
                )
                DropdownMenu(
                    expanded = showManageMenu,
                    onDismissRequest = onManageMenuDismiss,
                    containerColor = QuiketWhite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(4.dp),
                ) {
                    SubjectDropdownItem(
                        title = "과목 유형 수정",
                        icon = DesignSystemRes.drawable.ic_detail_edit,
                        onDismiss = onManageMenuDismiss,
                        onClick = onEditSubjectDetailsClick,
                    )
                    SubjectDropdownItem(
                        title = "과목명 수정",
                        icon = DesignSystemRes.drawable.ic_detail_edit,
                        onDismiss = onManageMenuDismiss,
                        onClick = onEditSubjectNameClick,
                    )
                    SubjectDropdownItem(
                        title = "챕터명 수정",
                        icon = DesignSystemRes.drawable.ic_detail_edit,
                        onDismiss = onManageMenuDismiss,
                        onClick = onEditChapterNameModeClick,
                    )
                    SubjectDropdownItem(
                        title = "과목 삭제",
                        icon = DesignSystemRes.drawable.ic_detail_remove,
                        onDismiss = onManageMenuDismiss,
                        onClick = onDeleteSubjectClick,
                        color = QuiketNegative,
                    )
                    SubjectDropdownItem(
                        title = "챕터 삭제",
                        icon = DesignSystemRes.drawable.ic_detail_remove,
                        onDismiss = onManageMenuDismiss,
                        onClick = onDeleteChapterModeClick,
                        color = QuiketNegative,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SubjectImageIconButton(
    icon: DrawableResource,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription },
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = QuiketWhite,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = QuiketWhite.copy(alpha = 0.45f),
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
internal fun SubjectDropdownItem(
    title: String,
    icon: DrawableResource,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    color: Color = QuiketGray950,
) {
    DropdownMenuItem(
        leadingIcon = {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        text = {
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        onClick = {
            onDismiss()
            onClick()
        },
    )
}

@Composable
internal fun SubjectBackButton(
    onClick: () -> Unit,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = "뒤로가기" },
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = iconColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = iconColor,
        ),
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.32f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun SubjectMessageScaffold(
    title: String,
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SubjectStatusSpacer()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SubjectGreen),
        ) {
            SubjectTopBar(
                title = title,
                onBackClick = onBackClick,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                color = QuiketGray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
internal fun SubjectStatusSpacer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(QuiketWhite),
    )
}

@Composable
internal fun SubjectHeaderSection(
    subject: SubjectDetail,
    modifier: Modifier = Modifier,
) {
    val labels = subject.toSubjectLabels()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SubjectGreen)
            .height(120.dp)
            .padding(start = 20.dp, top = 4.dp),
    ) {
        Column(modifier = Modifier.padding(end = 220.dp)) {
            Text(
                text = labels.h1.ifBlank { "학습 목적을 입력해주세요." },
                color = QuiketGray100,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = labels.h2.ifBlank { "과목 유형을 선택해주세요." },
                color = QuiketGray100,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Image(
            painter = painterResource(Res.drawable.ic_detail_quiket),
            contentDescription = null,
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(220f / 120f)
                .align(Alignment.BottomEnd),
        )
    }
}

@Composable
internal fun SubjectActionCard(
    title: String,
    icon: DrawableResource,
    background: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(QuiketGray50),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = QuiketBlack,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
internal fun SubjectExamCard(
    schedule: SubjectExamSchedule?,
    fallbackName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (schedule == null) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = QuiketGray50,
                    shape = RoundedCornerShape(1000.dp),
                )
                .subjectDashedBorder(
                    color = QuiketGray300,
                    strokeWidth = 2.dp,
                    cornerRadius = 100.dp,
                    dashLength = 7.dp,
                    gapLength = 5.dp,
                )
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = "시험 일정이 아직 없어요",
                color = SubjectGray800,
                style = MaterialTheme.typography.labelSmall,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+",
                    color = SubjectGray900,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "일정을 추가하고 D-Day를 확인해 보세요",
                    color = SubjectGray900,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(1000.dp))
                .background(QuiketWhite)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(QuiketGray50),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "□",
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.examName.ifBlank { fallbackName },
                    color = QuiketGray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = schedule.examDate,
                    color = SubjectGray900,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Text(
                text = schedule.dDay?.toDDayLabel() ?: "-",
                color = QuiketWhite,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(QuiketBrown950)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
internal fun SubjectChapterSection(
    chapters: List<ChapterWithParts>,
    isEditMode: Boolean,
    isDeleteMode: Boolean,
    onExitChapterManageMode: () -> Unit,
    onChapterClick: (ChapterWithParts) -> Unit,
    onChapterEditClick: (ChapterWithParts) -> Unit,
    onChapterDeleteClick: (ChapterWithParts) -> Unit,
    onChapterAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(QuiketWhite, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    isDeleteMode -> "삭제할 챕터를 선택해주세요"
                    isEditMode -> "수정할 챕터를 선택해주세요"
                    else -> "내 자료"
                },
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            if (isEditMode || isDeleteMode) {
                Text(
                    text = "완료",
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onExitChapterManageMode)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            chapters.forEach { chapter ->
                SubjectChapterCard(
                    chapter = chapter,
                    isEditMode = isEditMode,
                    isDeleteMode = isDeleteMode,
                    onChapterClick = { onChapterClick(chapter) },
                    onChapterEditClick = onChapterEditClick,
                    onChapterDeleteClick = onChapterDeleteClick,
                )
            }
        }

        SubjectAddChapterCard(
            onClick = onChapterAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
internal fun SubjectChapterCard(
    chapter: ChapterWithParts,
    isEditMode: Boolean,
    isDeleteMode: Boolean,
    onChapterClick: () -> Unit,
    onChapterEditClick: (ChapterWithParts) -> Unit,
    onChapterDeleteClick: (ChapterWithParts) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardClick = {
        when {
            isDeleteMode -> onChapterDeleteClick(chapter)
            isEditMode -> onChapterEditClick(chapter)
            else -> onChapterClick()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(QuiketBrown50)
            .clickable(role = Role.Button, onClick = cardClick),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(SubjectBrown300),
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            SubjectChapterChip(label = "챕터 ${chapter.displayOrder}")
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chapter.name,
                    color = SubjectGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp, end = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
                when {
                    isDeleteMode -> Text(
                        text = "삭제",
                        color = QuiketNegative,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    )
                    isEditMode -> Text(
                        text = "수정",
                        color = QuiketBrown950,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    )
                    else -> Text(
                        text = "→",
                        color = QuiketBrown950,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "파트 ${chapter.parts.size}개",
                color = SubjectGray800,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
internal fun SubjectChapterChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(SubjectBrown100)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            color = SubjectBrown700,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
internal fun SubjectAddChapterCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(QuiketGray50)
            .subjectDashedBorder(
                color = QuiketGray300,
                strokeWidth = 2.dp,
                cornerRadius = 12.dp,
                dashLength = 7.dp,
                gapLength = 5.dp,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "+",
                color = SubjectGray500,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "챕터 추가",
                color = SubjectGray500,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
internal fun SubjectEmptyChapterCard(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketWhite,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "아직 챕터가 없어요",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "자료를 업로드하면 챕터와 파트가 여기에 정리돼요.",
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun SubjectManagementMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    val isError = message.contains("못") ||
        message.contains("입력") ||
        message.contains("만료") ||
        message.contains("필요")

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isError) Color(0xFFFFECEC) else QuiketBrown50,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = message,
            color = if (isError) QuiketNegative else QuiketBrown950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
internal fun SubjectNameEditDialog(
    title: String,
    value: String,
    hint: String,
    isSaving: Boolean,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember(value) { mutableStateOf(value) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            QuiketTextField(
                value = draft,
                onValueChange = { draft = it },
                hint = hint,
                modifier = Modifier.semantics { contentDescription = "$title 입력" },
                enabled = !isSaving,
            )
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.clearAndSetSemantics { contentDescription = "$title $confirmLabel" },
                enabled = !isSaving && draft.trim().isNotEmpty(),
                onClick = { onConfirm(draft) },
            ) {
                Text(confirmLabel, color = QuiketOrange500)
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.clearAndSetSemantics { contentDescription = "$title 취소" },
                enabled = !isSaving,
                onClick = onDismiss,
            ) {
                Text("취소", color = QuiketGray700)
            }
        },
    )
}

@Composable
internal fun SubjectConfirmDialog(
    title: String,
    message: String,
    isSaving: Boolean,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Text(
                text = message,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onConfirm,
            ) {
                Text(
                    text = if (isSaving) "처리 중" else confirmLabel,
                    color = QuiketNegative,
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss,
            ) {
                Text("취소", color = QuiketGray700)
            }
        },
    )
}

@Composable
internal fun SubjectExamScheduleDialog(
    subjectName: String,
    schedule: SubjectExamSchedule?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (examName: String, examDate: String) -> Unit,
    onDeleteClick: () -> Unit,
) {
    var examName by remember(schedule?.id) {
        mutableStateOf(schedule?.examName?.takeIf { it.isNotBlank() } ?: subjectName)
    }
    var examDate by remember(schedule?.id) { mutableStateOf(schedule?.examDate.orEmpty()) }
    val canSave = examDate.trim().isNotEmpty() && !isSaving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = if (schedule == null) "시험 일정 등록" else "시험 일정 수정",
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "시험명",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
                QuiketTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    hint = subjectName,
                    enabled = !isSaving,
                )

                Text(
                    text = "시험 날짜",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
                QuiketTextField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    hint = "예: 2026-07-01",
                    enabled = !isSaving,
                )
                Text(
                    text = "년-월-일 형식으로 입력해주세요.",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall,
                )

                if (schedule != null) {
                    TextButton(
                        enabled = !isSaving,
                        onClick = onDeleteClick,
                    ) {
                        Text("일정 삭제", color = QuiketNegative)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onSave(examName, examDate) },
            ) {
                Text(
                    text = if (isSaving) "저장 중" else "적용",
                    color = QuiketOrange500,
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss,
            ) {
                Text("취소", color = QuiketGray700)
            }
        },
    )
}
