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
fun SubjectDetailRoute(
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

internal data class PartEntry(
    val chapterId: String,
    val chapterName: String,
    val chapterNumber: Int,
    val part: PartSummary,
)

internal val SubjectGreen = Color(0xFF465420)
internal val SubjectGray500 = Color(0xFF96989B)
internal val SubjectGray800 = Color(0xFF656668)
internal val SubjectGray900 = Color(0xFF535355)
internal val SubjectBrown100 = Color(0xFFE8E2D9)
internal val SubjectBrown300 = Color(0xFFBAA38A)
internal val SubjectBrown700 = Color(0xFF684C40)
internal const val PartNameMaxLength = 30

internal data class SubjectLabels(
    val h1: String,
    val h2: String,
)

internal fun SubjectDetail.toSubjectLabels(): SubjectLabels {
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

internal fun String.toExamTypeLabel(): String = when (lowercase()) {
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

internal fun String.toStudyFieldLabel(): String = when (lowercase()) {
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

internal fun String.toUsagePurposeLabel(): String = when (lowercase()) {
    "work" -> "업무·실무 활용"
    "personal" -> "개인 기록·정리"
    "hobby" -> "취미·가벼운 학습"
    "memory" -> "기억·암기 보조"
    "other" -> "기타"
    else -> this
}

internal fun Modifier.subjectDashedBorder(
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

internal fun Int.toDDayLabel(): String = when {
    this > 0 -> "D-$this"
    this == 0 -> "D-Day"
    else -> "D+${-this}"
}
