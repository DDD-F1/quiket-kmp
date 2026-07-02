package com.f1.quiket.feature.home.presentation.viewmodel

import com.f1.quiket.feature.home.presentation.contract.*

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import com.f1.quiket.feature.home.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExamScheduleViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val subjectRepository: SubjectRepository,
) : MviViewModel<ExamScheduleState, ExamScheduleIntent, ExamScheduleSideEffect>(
    initialState = ExamScheduleState(),
) {
    init {
        loadExams()
    }

    override fun handleIntent(intent: ExamScheduleIntent) {
        when (intent) {
            ExamScheduleIntent.Load -> loadExams()
            ExamScheduleIntent.AddExamClick -> handleAddExamClick()
            is ExamScheduleIntent.RegisterExam -> handleRegisterExam(intent)
            is ExamScheduleIntent.DeleteExam -> handleDeleteExam(intent)
        }
    }

    private fun loadExams() {
        launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            when (val result = homeRepository.getSubjects()) {
                is NetworkResult.Success -> {
                    val allSubjects = result.data
                    val exams = allSubjects
                        .mapNotNull { it.examSchedule }
                        .filter { (it.dDay ?: 0) >= 0 }
                        .distinctBy { it.subjectId }
                        .sortedBy { it.dDay ?: Int.MAX_VALUE }
                    updateState { copy(isLoading = false, exams = exams, subjects = allSubjects) }
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun handleAddExamClick() {
        launch {
            if (currentState.exams.size >= 5) {
                sendEffect(
                    ExamScheduleSideEffect.ShowToast(
                        "시험은 최대 5개까지 등록할 수 있어요. 기존 일정을 삭제 후 등록해주세요"
                    )
                )
            } else {
                sendEffect(ExamScheduleSideEffect.ShowAddExamDialog)
            }
        }
    }

    private fun handleRegisterExam(intent: ExamScheduleIntent.RegisterExam) {
        launch {
            subjectRepository.upsertExamSchedule(
                subjectId = intent.subjectId,
                examName = intent.examName.ifBlank { null },
                examDate = intent.examDate,
            )
            loadExams()
            sendEffect(ExamScheduleSideEffect.ExamRegistered)
        }
    }

    private fun handleDeleteExam(intent: ExamScheduleIntent.DeleteExam) {
        launch {
            subjectRepository.deleteExamSchedule(subjectId = intent.subjectId)
            loadExams()
            sendEffect(ExamScheduleSideEffect.ExamDeleted)
        }
    }
}