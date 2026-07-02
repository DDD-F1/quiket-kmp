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
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_detail_back
import quiket.composeapp.generated.resources.ic_detail_etc
import quiket.composeapp.generated.resources.ic_detail_lecture_list
import quiket.composeapp.generated.resources.ic_detail_edit
import quiket.composeapp.generated.resources.ic_detail_quiket
import quiket.composeapp.generated.resources.ic_detail_remove
import quiket.composeapp.generated.resources.ic_home_make
import quiket.composeapp.generated.resources.ic_home_upload
import quiket.composeapp.generated.resources.ic_star_off
import quiket.composeapp.generated.resources.ic_star_on

@Composable
internal fun SubjectDetailRoute(
    subjectId: String,
    subjectName: String,
    isStarred: Boolean,
    openUploadOnStart: Boolean = false,
    onOpenUploadConsumed: () -> Unit = {},
    onBackClick: () -> Unit,
    onCreateQuizClick: (String) -> Unit,
    onStarToggle: (Boolean) -> Unit,
    onSubjectDeleted: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateHolder = koinInject<SubjectDetailStateHolder>()
    var selectedPartId by remember(subjectId) { mutableStateOf<String?>(null) }
    var uploadVisible by remember(subjectId) { mutableStateOf(false) }
    var uploadTargetChapter by remember(subjectId) { mutableStateOf<ChapterWithParts?>(null) }
    var materialCheckLectureUploadId by remember(subjectId) { mutableStateOf<String?>(null) }
    var reloadKey by remember(subjectId) { mutableStateOf(0) }
    var showManageMenu by remember(subjectId) { mutableStateOf(false) }
    var subjectNameEditVisible by remember(subjectId) { mutableStateOf(false) }
    var subjectDetailsEditVisible by remember(subjectId) { mutableStateOf(false) }
    var isChapterEditMode by remember(subjectId) { mutableStateOf(false) }
    var isChapterDeleteMode by remember(subjectId) { mutableStateOf(false) }
    var editingChapter by remember(subjectId) { mutableStateOf<ChapterWithParts?>(null) }
    var deletingChapter by remember(subjectId) { mutableStateOf<ChapterWithParts?>(null) }
    var subjectDeleteVisible by remember(subjectId) { mutableStateOf(false) }
    var examScheduleEditVisible by remember(subjectId) { mutableStateOf(false) }
    var examScheduleDeleteVisible by remember(subjectId) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(subjectId, openUploadOnStart) {
        if (openUploadOnStart) {
            uploadVisible = true
            uploadTargetChapter = null
            onOpenUploadConsumed()
        }
    }

    LaunchedEffect(subjectId, reloadKey, stateHolder) {
        stateHolder.loadSubject(
            subjectId = subjectId,
            subjectName = subjectName,
            onSessionExpired = onSessionExpired,
        )
    }

    val successState = stateHolder.state as? SubjectDetailUiState.Success
    val currentMaterialCheckLectureUploadId = materialCheckLectureUploadId
    if (successState != null && currentMaterialCheckLectureUploadId != null) {
        MaterialCheckRoute(
            lectureUploadId = currentMaterialCheckLectureUploadId,
            onBackClick = {
                materialCheckLectureUploadId = null
                uploadVisible = false
                uploadTargetChapter = null
                selectedPartId = null
                reloadKey += 1
            },
            onComplete = { firstPartId ->
                materialCheckLectureUploadId = null
                uploadVisible = false
                uploadTargetChapter = null
                selectedPartId = firstPartId
                reloadKey += 1
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    if (successState != null && uploadVisible) {
        TextLectureUploadRoute(
            subject = successState.subject,
            targetChapter = uploadTargetChapter,
            onBackClick = {
                uploadVisible = false
                uploadTargetChapter = null
            },
            onUploadCompleted = { lectureUploadId ->
                materialCheckLectureUploadId = lectureUploadId
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    if (successState != null && subjectDetailsEditVisible) {
        SubjectDetailsEditRoute(
            subject = successState.subject,
            onBackClick = { subjectDetailsEditVisible = false },
            onSaved = {
                subjectDetailsEditVisible = false
                selectedPartId = null
                reloadKey += 1
                stateHolder.showManagementMessage("과목 유형이 수정됐어요")
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    val currentPartId = selectedPartId
    if (successState != null && currentPartId != null) {
        PartDetailRoute(
            subject = successState.subject,
            initialPartId = currentPartId,
            onBackClick = { selectedPartId = null },
            onPartUpdated = stateHolder::applyPartUpdate,
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    SubjectDetailScreen(
        state = stateHolder.state,
        showManageMenu = showManageMenu,
        isChapterEditMode = isChapterEditMode,
        isChapterDeleteMode = isChapterDeleteMode,
        managementMessage = stateHolder.managementMessage,
        isManagementBusy = stateHolder.isManagementBusy,
        isStarred = isStarred,
        onBackClick = onBackClick,
        onChapterClick = { chapter ->
            val firstPart = chapter.parts
                .sortedBy { part -> part.partNumber }
                .firstOrNull()
            if (firstPart == null) {
                stateHolder.showManagementMessage("아직 파트가 없어요. 파트를 추가해주세요.")
            } else {
                selectedPartId = firstPart.id
            }
        },
        onExamScheduleClick = { examScheduleEditVisible = true },
        onUploadClick = {
            uploadTargetChapter = null
            uploadVisible = true
        },
        onCreateQuizClick = { subject ->
            onCreateQuizClick(subject.id)
        },
        onStarClick = { onStarToggle(!isStarred) },
        onManageMenuClick = { showManageMenu = true },
        onManageMenuDismiss = { showManageMenu = false },
        onEditSubjectNameClick = {
            showManageMenu = false
            subjectNameEditVisible = true
            stateHolder.clearManagementMessage()
        },
        onEditSubjectDetailsClick = {
            showManageMenu = false
            subjectDetailsEditVisible = true
            isChapterEditMode = false
            isChapterDeleteMode = false
            stateHolder.clearManagementMessage()
        },
        onEditChapterNameModeClick = {
            showManageMenu = false
            isChapterEditMode = true
            isChapterDeleteMode = false
            stateHolder.clearManagementMessage()
        },
        onDeleteSubjectClick = {
            showManageMenu = false
            subjectDeleteVisible = true
            isChapterEditMode = false
            isChapterDeleteMode = false
            stateHolder.clearManagementMessage()
        },
        onDeleteChapterModeClick = {
            showManageMenu = false
            isChapterDeleteMode = true
            isChapterEditMode = false
            stateHolder.clearManagementMessage()
        },
        onExitChapterManageMode = {
            isChapterEditMode = false
            isChapterDeleteMode = false
            stateHolder.clearManagementMessage()
        },
        onChapterEditClick = { chapter ->
            editingChapter = chapter
            stateHolder.clearManagementMessage()
        },
        onChapterDeleteClick = { chapter ->
            deletingChapter = chapter
            stateHolder.clearManagementMessage()
        },
        modifier = modifier,
    )

    if (successState != null && subjectNameEditVisible) {
        SubjectNameEditDialog(
            title = "과목명 수정",
            value = successState.subject.name,
            hint = "과목명을 입력해주세요",
            isSaving = stateHolder.isManagementBusy,
            confirmLabel = "수정",
            onDismiss = {
                if (!stateHolder.isManagementBusy) {
                    subjectNameEditVisible = false
                }
            },
            onConfirm = { name ->
                coroutineScope.launch {
                    if (stateHolder.updateSubjectName(subjectId, name, onSessionExpired)) {
                        subjectNameEditVisible = false
                    }
                }
            },
        )
    }

    editingChapter?.let { chapter ->
        SubjectNameEditDialog(
            title = "챕터명 수정",
            value = chapter.name,
            hint = "챕터명을 입력해주세요",
            isSaving = stateHolder.isManagementBusy,
            confirmLabel = "수정",
            onDismiss = {
                if (!stateHolder.isManagementBusy) {
                    editingChapter = null
                }
            },
            onConfirm = { name ->
                coroutineScope.launch {
                    if (stateHolder.updateChapterName(chapter, name, onSessionExpired)) {
                        editingChapter = null
                        isChapterEditMode = false
                    }
                }
            },
        )
    }

    if (successState != null && examScheduleEditVisible) {
        SubjectExamScheduleDialog(
            subjectName = successState.subject.name,
            schedule = successState.subject.examSchedule,
            isSaving = stateHolder.isManagementBusy,
            onDismiss = {
                if (!stateHolder.isManagementBusy) {
                    examScheduleEditVisible = false
                }
            },
            onSave = { examName, examDate ->
                coroutineScope.launch {
                    if (stateHolder.upsertExamSchedule(successState.subject, examName, examDate, onSessionExpired)) {
                        examScheduleEditVisible = false
                    }
                }
            },
            onDeleteClick = {
                if (!stateHolder.isManagementBusy && successState.subject.examSchedule != null) {
                    examScheduleDeleteVisible = true
                }
            },
        )
    }

    if (successState != null && subjectDeleteVisible) {
        SubjectConfirmDialog(
            title = "과목 삭제",
            message = "'${successState.subject.name}' 과목을 삭제할까요?",
            isSaving = stateHolder.isManagementBusy,
            confirmLabel = "삭제",
            onDismiss = {
                if (!stateHolder.isManagementBusy) {
                    subjectDeleteVisible = false
                }
            },
            onConfirm = {
                coroutineScope.launch {
                    if (stateHolder.deleteSubject(successState.subject.id, onSessionExpired)) {
                        subjectDeleteVisible = false
                        onSubjectDeleted()
                    }
                }
            },
        )
    }

    if (successState != null && examScheduleDeleteVisible) {
        SubjectConfirmDialog(
            title = "시험 일정 삭제",
            message = "'${successState.subject.name}' 과목의 시험 일정을 삭제할까요?",
            isSaving = stateHolder.isManagementBusy,
            confirmLabel = "삭제",
            onDismiss = {
                if (!stateHolder.isManagementBusy) {
                    examScheduleDeleteVisible = false
                }
            },
            onConfirm = {
                coroutineScope.launch {
                    if (stateHolder.deleteExamSchedule(successState.subject, onSessionExpired)) {
                        examScheduleDeleteVisible = false
                        examScheduleEditVisible = false
                    }
                }
            },
        )
    }

    deletingChapter?.let { chapter ->
        SubjectConfirmDialog(
            title = "챕터 삭제",
            message = "'${chapter.name}' 챕터와 포함된 파트를 삭제할까요?",
            isSaving = stateHolder.isManagementBusy,
            confirmLabel = "삭제",
            onDismiss = {
                if (!stateHolder.isManagementBusy) {
                    deletingChapter = null
                }
            },
            onConfirm = {
                coroutineScope.launch {
                    if (stateHolder.deleteChapter(chapter, onSessionExpired)) {
                        deletingChapter = null
                        isChapterDeleteMode = false
                    }
                }
            },
        )
    }
}

@Composable
private fun SubjectDetailScreen(
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
private fun SubjectDetailContent(
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
                    icon = Res.drawable.ic_home_upload,
                    background = QuiketGray100,
                    onClick = onUploadClick,
                    modifier = Modifier
                        .height(103.dp)
                        .weight(1f),
                )
                SubjectActionCard(
                    title = "퀴즈 만들기",
                    icon = Res.drawable.ic_home_make,
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
private fun SubjectTopBar(
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
                    icon = Res.drawable.ic_detail_etc,
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
                        icon = Res.drawable.ic_detail_edit,
                        onDismiss = onManageMenuDismiss,
                        onClick = onEditSubjectDetailsClick,
                    )
                    SubjectDropdownItem(
                        title = "과목명 수정",
                        icon = Res.drawable.ic_detail_edit,
                        onDismiss = onManageMenuDismiss,
                        onClick = onEditSubjectNameClick,
                    )
                    SubjectDropdownItem(
                        title = "챕터명 수정",
                        icon = Res.drawable.ic_detail_edit,
                        onDismiss = onManageMenuDismiss,
                        onClick = onEditChapterNameModeClick,
                    )
                    SubjectDropdownItem(
                        title = "과목 삭제",
                        icon = Res.drawable.ic_detail_remove,
                        onDismiss = onManageMenuDismiss,
                        onClick = onDeleteSubjectClick,
                        color = QuiketNegative,
                    )
                    SubjectDropdownItem(
                        title = "챕터 삭제",
                        icon = Res.drawable.ic_detail_remove,
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
private fun SubjectImageIconButton(
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
private fun SubjectDropdownItem(
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
private fun SubjectBackButton(
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
private fun SubjectMessageScaffold(
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
private fun SubjectStatusSpacer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(QuiketWhite),
    )
}

@Composable
private fun SubjectHeaderSection(
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
private fun SubjectActionCard(
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
private fun SubjectExamCard(
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
                .dashedBorder(
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
private fun SubjectChapterSection(
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
private fun SubjectChapterCard(
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
private fun SubjectChapterChip(
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
private fun SubjectAddChapterCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(QuiketGray50)
            .dashedBorder(
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
private fun SubjectEmptyChapterCard(
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
private fun SubjectManagementMessage(
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
private fun SubjectNameEditDialog(
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
private fun SubjectConfirmDialog(
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
private fun SubjectExamScheduleDialog(
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

@Composable
private fun PartDetailRoute(
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
private fun PartDetailScreen(
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
private fun LectureViewTopBar(
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
                icon = Res.drawable.ic_detail_etc,
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
private fun LectureViewDropdownItem(
    title: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        leadingIcon = {
            Image(
                painter = painterResource(Res.drawable.ic_detail_edit),
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
private fun ImageIconButton(
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
private fun LectureViewBreadcrumb(
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
private fun PartTocPanel(
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
private fun PartTocChapter(
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
private fun PartTocRow(
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
private fun PartReadContent(
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
private fun PartEditContent(
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
private fun PartBottomBar(
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
private fun PartCircleNavigationButton(
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
private fun PartBarButton(
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
private fun PartMessage(
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

private data class PartEntry(
    val chapterId: String,
    val chapterName: String,
    val chapterNumber: Int,
    val part: PartSummary,
)

private val SubjectGreen = Color(0xFF465420)
private val SubjectGray500 = Color(0xFF96989B)
private val SubjectGray800 = Color(0xFF656668)
private val SubjectGray900 = Color(0xFF535355)
private val SubjectBrown100 = Color(0xFFE8E2D9)
private val SubjectBrown300 = Color(0xFFBAA38A)
private val SubjectBrown700 = Color(0xFF684C40)
private const val PartNameMaxLength = 30

private data class SubjectLabels(
    val h1: String,
    val h2: String,
)

private fun SubjectDetail.toSubjectLabels(): SubjectLabels {
    val purposeLabel = when (purpose.lowercase()) {
        "exam" -> "시험·자격증 대비"
        "review",
        "self_study",
        -> "자기계발·일반 복습"
        "other" -> "기타"
        else -> purpose
    }

    val detail = when (purpose.lowercase()) {
        "exam" -> examDetail?.examType?.toExamTypeLabel()
        "review",
        "self_study",
        -> reviewDetail?.field?.toStudyFieldLabel()
        "other" -> otherDetail?.usagePurpose?.toUsagePurposeLabel()
        else -> null
    }

    return SubjectLabels(
        h1 = purposeLabel,
        h2 = detail.orEmpty(),
    )
}

private fun String.toExamTypeLabel(): String = when (lowercase()) {
    "university" -> "대학시험"
    "middle_high" -> "중고등시험"
    "certificate" -> "자격증"
    "language" -> "어학"
    "civil_service",
    "civil_servant",
    -> "공무원 시험"
    "other_exam",
    "other",
    -> "기타"
    else -> this
}

private fun String.toStudyFieldLabel(): String = when (lowercase()) {
    "humanities" -> "인문"
    "korean_history" -> "한국사"
    "world_history" -> "세계사"
    "society" -> "사회"
    "politics" -> "정치"
    "economics" -> "경제"
    "business" -> "경영"
    "science_tech" -> "과학기술"
    "it" -> "IT"
    "culture_art" -> "문화예술"
    "psychology" -> "심리학"
    "custom" -> "직접 입력"
    else -> this
}

private fun String.toUsagePurposeLabel(): String = when (lowercase()) {
    "work" -> "업무·실무 활용"
    "personal" -> "개인 기록·정리"
    "hobby" -> "취미·가벼운 학습"
    "memory" -> "기억·암기 보조"
    "other" -> "기타"
    else -> this
}

private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp,
    dashLength: Dp,
    gapLength: Dp,
): Modifier = drawWithContent {
    drawContent()

    val strokeWidthPx = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
        size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            ),
            cap = StrokeCap.Round,
        ),
    )
}

private fun Int.toDDayLabel(): String = when {
    this > 0 -> "D-$this"
    this == 0 -> "D-Day"
    else -> "D+${-this}"
}
