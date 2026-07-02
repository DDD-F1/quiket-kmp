package com.f1.quiket.feature.floating.presentation.screen.addsubject

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.f1.quiket.core.designsystem.R as DesignSystemR
import com.f1.quiket.core.designsystem.component.QuiketToast
import com.f1.quiket.feature.floating.domain.model.AddSubjectState
import com.f1.quiket.feature.floating.domain.model.Chapter
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.UsagePurpose
import com.f1.quiket.feature.floating.presentation.screen.UploadScreen
import com.f1.quiket.feature.floating.presentation.screen.lectureview.LectureViewScreen
import com.f1.quiket.feature.floating.presentation.screen.materialcheck.MaterialCheckScreen
import com.f1.quiket.feature.floating.presentation.screen.subjectdetail.SubjectDetailScreen
import com.f1.quiket.feature.floating.presentation.viewmodel.AddSubjectViewModel
import kotlinx.coroutines.delay

@Composable
fun AddSubjectScreen(
    onFinish: () -> Unit = {},
    onDismiss: () -> Unit = {},
    viewModel: AddSubjectViewModel = hiltViewModel(),
) {
    val createdSubjectId by viewModel.createdSubjectId.collectAsState()
    val existingSubjectNames by viewModel.existingSubjectNames.collectAsState()
    val isSavingSubject by viewModel.isSavingSubject.collectAsState()
    val subjectSaveErrorMessage by viewModel.subjectSaveErrorMessage.collectAsState()
    var state by remember { mutableStateOf(AddSubjectState()) }
    var depth by remember { mutableStateOf(1) }
    var skippedFromStep by remember { mutableStateOf(0) }
    var nextChapterNumber by remember { mutableStateOf(1) }
    var subjectDetailRefreshTrigger by remember { mutableIntStateOf(0) }
    var visibleSaveErrorMessage by remember { mutableStateOf<String?>(null) }

    // 업로드 성공 후 MaterialCheckScreen/LectureViewScreen에 넘길 정보
    var uploadedLectureUploadId by remember { mutableStateOf("") }
    var uploadedChapterId by remember { mutableStateOf("") }
    var uploadedChapterName by remember { mutableStateOf("") }
    var uploadedChapterNumber by remember { mutableStateOf(1) }
    var uploadedPartCount by remember { mutableStateOf(0) }


    LaunchedEffect(viewModel) {
        viewModel.updateDetailsSuccess.collect {
            subjectDetailRefreshTrigger++
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.subjectDetailReady.collect {
            depth = 4
        }
    }

    LaunchedEffect(subjectSaveErrorMessage) {
        val message = subjectSaveErrorMessage ?: return@LaunchedEffect
        visibleSaveErrorMessage = message
        delay(SAVE_ERROR_TOAST_DURATION_MILLIS)
        visibleSaveErrorMessage = null
        viewModel.clearSubjectSaveError()
    }

    // depth=4(SubjectDetailScreen)에서 시스템 백버튼도 onFinish로 처리
    BackHandler(enabled = depth == 4) { onFinish() }
    // depth=7(LectureViewScreen)에서 시스템 백버튼 → SubjectDetail로
    BackHandler(enabled = depth == 7) { depth = 4 }


    fun goToSubjectDetail(currentState: AddSubjectState) {
        if (isSavingSubject) return

        val existingId = createdSubjectId
        if (existingId == null) {
            viewModel.createSubject(currentState)
        } else {
            viewModel.updateSubjectDetails(existingId, currentState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (depth) {
            1 -> AddSubjectStep1Screen(
                initialSubjectName = state.subjectName,
                existingSubjectNames = existingSubjectNames,
                onBackClick = onDismiss,
                onSkipClick = { name ->
                    state = state.copy(subjectName = name)
                    skippedFromStep = 1
                    goToSubjectDetail(state.copy(subjectName = name))
                },
                onNextClick = { name, purpose ->
                    state = state.copy(subjectName = name, studyPurpose = purpose)
                    depth = 2
                },
            )

            2 -> AddSubjectStep2Screen(
                studyPurpose = state.studyPurpose ?: StudyPurpose.EXAM,
                onBackClick = { depth = 1 },
                onSkipClick = {
                    skippedFromStep = 2
                    goToSubjectDetail(state)
                },
                onNextClick = { selection ->
                    when (selection) {
                        is ExamType -> state = state.copy(examType = selection)
                        is StudyField -> state = state.copy(studyField = selection)
                        is UsagePurpose -> state = state.copy(usagePurpose = selection)
                    }
                    depth = 3
                },
            )

            3 -> AddSubjectStep3Screen(
                studyPurpose = state.studyPurpose ?: StudyPurpose.EXAM,
                examType = state.examType,
                studyField = state.studyField,
                usagePurpose = state.usagePurpose,
                onBackClick = { depth = 2 },
                onSkipClick = {
                    skippedFromStep = 3
                    goToSubjectDetail(state)
                },
                onCreateClick = { label, updater ->
                    val updated = updater(state).copy(step3Label = label)
                    state = updated
                    goToSubjectDetail(updated)
                },
            )

            4 -> SubjectDetailScreen(
                subjectId = createdSubjectId ?: "",
                subjectName = state.subjectName.ifBlank { "새 과목" },
                refreshTrigger = subjectDetailRefreshTrigger,
                studyPurposeLabel = state.studyPurpose?.title ?: "",
                examTypeLabel = state.examType?.label ?: state.studyField?.label ?: state.usagePurpose?.title ?: "",
                onBackClick = onFinish,
                onChapterAddClick = { n -> nextChapterNumber = n; depth = 5 },
                onUploadClick = { n -> nextChapterNumber = n; depth = 5 },
                onEditSubjectType = { depth = if (skippedFromStep > 0) skippedFromStep else 3 },
                onSubjectNameChanged = { newName -> state = state.copy(subjectName = newName) },
                onSubjectDeleted = onFinish,
            )

            5 -> UploadScreen(
                subjectId = createdSubjectId,
                chapterTitle = state.subjectName.ifBlank { "새 과목" },
                lecturePurpose = state.studyPurpose?.title,
                chapterCount = nextChapterNumber,
                onBackClick = { depth = 4 },
                onNextClick = { depth = 4 },
                onUploadSuccess = { lectureUploadId, chapterNum ->
                    uploadedLectureUploadId = lectureUploadId
                    uploadedChapterNumber = chapterNum
                    depth = 6
                },
            )

            6 -> MaterialCheckScreen(
                lectureUploadId = uploadedLectureUploadId,
                chapterNumber = uploadedChapterNumber,
                onBackClick = { depth = 4 },
                onComplete = { _, chapterId, finalChapterName, partCount ->
                    uploadedChapterId = chapterId
                    uploadedChapterName = finalChapterName
                    uploadedPartCount = partCount
                    depth = 7
                },
            )

            7 -> LectureViewScreen(
                subjectId = createdSubjectId ?: "",
                chapter = Chapter(
                    id = uploadedChapterId,
                    number = uploadedChapterNumber,
                    name = uploadedChapterName,
                    partCount = uploadedPartCount,
                ),
                onBackClick = { depth = 4 },
            )
        }

        visibleSaveErrorMessage?.let { message ->
            QuiketToast(
                message = message,
                icon = DesignSystemR.drawable.ic_upload_fail,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            )
        }
    }
}

private const val SAVE_ERROR_TOAST_DURATION_MILLIS = 2_500L
