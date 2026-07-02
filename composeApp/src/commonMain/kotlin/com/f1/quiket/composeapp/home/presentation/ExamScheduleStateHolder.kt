package com.f1.quiket.composeapp.home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.home.domain.model.HomeException
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import com.f1.quiket.composeapp.home.domain.usecase.HomeUseCases
import com.f1.quiket.composeapp.network.toUserFacingMessage

internal class ExamScheduleStateHolder(
    private val homeUseCases: HomeUseCases,
) {
    var state by mutableStateOf<ExamScheduleUiState>(ExamScheduleUiState.Loading)
        private set

    var editorMode by mutableStateOf<ExamEditorMode?>(null)
        private set

    var deleteTarget by mutableStateOf<ExamScheduleItem?>(null)
        private set

    var isMutating by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadSubjects(onSessionExpired: () -> Unit) {
        state = ExamScheduleUiState.Loading
        feedbackMessage = null
        runCatching {
            homeUseCases.getSubjects()
        }.onSuccess { subjects ->
            applySubjects(subjects = subjects)
        }.onFailure { error ->
            if (error is HomeException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = ExamScheduleUiState.Error(
                    error.toUserFacingMessage("시험 일정을 불러오지 못했습니다."),
                )
            }
        }
    }

    fun requestAddExam() {
        val successState = state as? ExamScheduleUiState.Success ?: return
        if (successState.exams.size >= 5) {
            feedbackMessage = "시험은 최대 5개까지 등록할 수 있어요. 기존 일정을 삭제 후 등록해주세요"
        } else {
            feedbackMessage = null
            editorMode = ExamEditorMode.Add
        }
    }

    fun requestEditExam(exam: ExamScheduleItem) {
        feedbackMessage = null
        editorMode = ExamEditorMode.Edit(exam)
    }

    fun requestDeleteExam(exam: ExamScheduleItem) {
        feedbackMessage = null
        deleteTarget = exam
    }

    fun dismissEditor() {
        if (!isMutating) editorMode = null
    }

    fun dismissDeleteDialog() {
        if (!isMutating) deleteTarget = null
    }

    suspend fun saveExam(
        subjectId: String,
        examName: String,
        examDate: String,
        onSessionExpired: () -> Unit,
    ) {
        isMutating = true
        feedbackMessage = null
        runCatching {
            homeUseCases.upsertExamSchedule(
                subjectId = subjectId,
                examName = examName.ifBlank { null },
                examDate = examDate,
            )
            homeUseCases.getSubjects()
        }.onSuccess { subjects ->
            applySubjects(subjects = subjects)
            editorMode = null
            feedbackMessage = "시험 일정이 저장됐어요"
        }.onFailure { error ->
            handleFailure(
                error = error,
                fallbackMessage = "시험 일정을 저장하지 못했습니다.",
                onSessionExpired = onSessionExpired,
            )
        }
        isMutating = false
    }

    suspend fun deleteExam(
        exam: ExamScheduleItem,
        onSessionExpired: () -> Unit,
    ) {
        isMutating = true
        feedbackMessage = null
        runCatching {
            homeUseCases.deleteExamSchedule(subjectId = exam.subjectId)
            homeUseCases.getSubjects()
        }.onSuccess { subjects ->
            applySubjects(subjects = subjects)
            deleteTarget = null
            feedbackMessage = "시험 일정이 삭제됐어요"
        }.onFailure { error ->
            handleFailure(
                error = error,
                fallbackMessage = "시험 일정을 삭제하지 못했습니다.",
                onSessionExpired = onSessionExpired,
            )
        }
        isMutating = false
    }

    private fun applySubjects(subjects: List<SubjectSummary>) {
        state = ExamScheduleUiState.Success(
            exams = subjects.toExamScheduleItems(),
            subjects = subjects,
        )
    }

    private fun handleFailure(
        error: Throwable,
        fallbackMessage: String,
        onSessionExpired: () -> Unit,
    ) {
        if (error is HomeException && error.isUnauthorized) {
            onSessionExpired()
        } else {
            feedbackMessage = error.toUserFacingMessage(fallbackMessage)
        }
    }
}
