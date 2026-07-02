package com.f1.quiket.feature.home.presentation.contract

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.SubjectExamSchedule

data class ExamScheduleState(
    val isLoading: Boolean = true,
    val exams: List<SubjectExamSchedule> = emptyList(),
    val subjects: List<QuizSubjectSummary> = emptyList(),
    val errorMessage: String? = null,
) : UiState

sealed interface ExamScheduleIntent : UiIntent {
    data object Load : ExamScheduleIntent
    data object AddExamClick : ExamScheduleIntent
    data class RegisterExam(
        val subjectId: String,
        val examName: String,
        val examDate: String,
    ) : ExamScheduleIntent
    data class DeleteExam(val subjectId: String) : ExamScheduleIntent
}

sealed interface ExamScheduleSideEffect : UiEffect {
    data class ShowToast(val message: String) : ExamScheduleSideEffect
    data object ShowAddExamDialog : ExamScheduleSideEffect
    data object ExamRegistered : ExamScheduleSideEffect
    data object ExamDeleted : ExamScheduleSideEffect
}