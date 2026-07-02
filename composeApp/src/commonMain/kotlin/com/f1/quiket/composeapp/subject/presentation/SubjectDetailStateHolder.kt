package com.f1.quiket.composeapp.subject.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.subject.domain.model.ChapterWithParts
import com.f1.quiket.composeapp.subject.domain.model.PartDetail
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail
import com.f1.quiket.composeapp.subject.SubjectDetailUiState
import com.f1.quiket.composeapp.subject.domain.model.SubjectException
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import com.f1.quiket.composeapp.subject.withRenamedChapter
import com.f1.quiket.composeapp.subject.withUpdatedPart
import com.f1.quiket.composeapp.subject.withoutChapter

internal class SubjectDetailStateHolder(
    private val subjectUseCases: SubjectUseCases,
) {
    var state by mutableStateOf<SubjectDetailUiState>(SubjectDetailUiState.Loading(""))
        private set

    var managementMessage by mutableStateOf<String?>(null)
        private set

    var isManagementBusy by mutableStateOf(false)
        private set

    suspend fun loadSubject(
        subjectId: String,
        subjectName: String,
        onSessionExpired: () -> Unit,
    ) {
        state = SubjectDetailUiState.Loading(subjectName)
        runCatching {
            subjectUseCases.getSubject(subjectId = subjectId)
        }.onSuccess { subject ->
            state = SubjectDetailUiState.Success(subject)
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = SubjectDetailUiState.Error(
                    subjectName = subjectName,
                    message = error.toUserFacingMessage("과목 정보를 불러오지 못했습니다."),
                )
            }
        }
    }

    fun clearManagementMessage() {
        managementMessage = null
    }

    fun showManagementMessage(message: String) {
        managementMessage = message
    }

    fun applyPartUpdate(updatedPart: PartDetail) {
        updateSubjectState { subject -> subject.withUpdatedPart(updatedPart) }
    }

    suspend fun updateSubjectName(
        subjectId: String,
        name: String,
        onSessionExpired: () -> Unit,
    ): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            managementMessage = "과목명을 입력해주세요"
            return false
        }

        isManagementBusy = true
        managementMessage = null
        return runCatching {
            subjectUseCases.updateSubjectName(
                subjectId = subjectId,
                name = trimmedName,
            )
        }.fold(
            onSuccess = { updatedSubject ->
                updateSubjectState { subject -> subject.copy(name = updatedSubject.name) }
                managementMessage = "과목명이 수정됐어요"
                isManagementBusy = false
                true
            },
            onFailure = { error ->
                handleManagementFailure(error, fallbackMessage = "과목명을 수정하지 못했습니다.", onSessionExpired)
                isManagementBusy = false
                false
            },
        )
    }

    suspend fun upsertExamSchedule(
        subject: SubjectDetail,
        examName: String,
        examDate: String,
        onSessionExpired: () -> Unit,
    ): Boolean {
        val trimmedDate = examDate.trim()
        if (trimmedDate.isBlank()) {
            managementMessage = "시험 날짜를 입력해주세요"
            return false
        }

        isManagementBusy = true
        managementMessage = null
        return runCatching {
            subjectUseCases.upsertExamSchedule(
                subjectId = subject.id,
                examName = examName.trim().ifBlank { null },
                examDate = trimmedDate,
            )
        }.fold(
            onSuccess = { schedule ->
                updateSubjectState { currentSubject -> currentSubject.copy(examSchedule = schedule) }
                managementMessage = "시험 일정이 저장됐어요"
                isManagementBusy = false
                true
            },
            onFailure = { error ->
                handleManagementFailure(error, fallbackMessage = "시험 일정을 저장하지 못했습니다.", onSessionExpired)
                isManagementBusy = false
                false
            },
        )
    }

    suspend fun deleteExamSchedule(
        subject: SubjectDetail,
        onSessionExpired: () -> Unit,
    ): Boolean {
        isManagementBusy = true
        managementMessage = null
        return runCatching {
            subjectUseCases.deleteExamSchedule(subjectId = subject.id)
        }.fold(
            onSuccess = {
                updateSubjectState { currentSubject -> currentSubject.copy(examSchedule = null) }
                managementMessage = "시험 일정이 삭제됐어요"
                isManagementBusy = false
                true
            },
            onFailure = { error ->
                handleManagementFailure(error, fallbackMessage = "시험 일정을 삭제하지 못했습니다.", onSessionExpired)
                isManagementBusy = false
                false
            },
        )
    }

    suspend fun updateChapterName(
        chapter: ChapterWithParts,
        name: String,
        onSessionExpired: () -> Unit,
    ): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            managementMessage = "챕터명을 입력해주세요"
            return false
        }

        isManagementBusy = true
        managementMessage = null
        return runCatching {
            subjectUseCases.updateChapterName(
                chapterId = chapter.id,
                name = trimmedName,
            )
        }.fold(
            onSuccess = { updatedChapter ->
                updateSubjectState { subject ->
                    subject.withRenamedChapter(
                        chapterId = chapter.id,
                        name = updatedChapter.name,
                    )
                }
                managementMessage = "챕터명이 수정됐어요"
                isManagementBusy = false
                true
            },
            onFailure = { error ->
                handleManagementFailure(error, fallbackMessage = "챕터명을 수정하지 못했습니다.", onSessionExpired)
                isManagementBusy = false
                false
            },
        )
    }

    suspend fun deleteSubject(
        subjectId: String,
        onSessionExpired: () -> Unit,
    ): Boolean {
        isManagementBusy = true
        managementMessage = null
        return runCatching {
            subjectUseCases.deleteSubject(subjectId = subjectId)
        }.fold(
            onSuccess = {
                isManagementBusy = false
                true
            },
            onFailure = { error ->
                handleManagementFailure(error, fallbackMessage = "과목을 삭제하지 못했습니다.", onSessionExpired)
                isManagementBusy = false
                false
            },
        )
    }

    suspend fun deleteChapter(
        chapter: ChapterWithParts,
        onSessionExpired: () -> Unit,
    ): Boolean {
        isManagementBusy = true
        managementMessage = null
        return runCatching {
            subjectUseCases.deleteChapter(chapterId = chapter.id)
        }.fold(
            onSuccess = {
                updateSubjectState { subject -> subject.withoutChapter(chapter.id) }
                managementMessage = "챕터가 삭제됐어요"
                isManagementBusy = false
                true
            },
            onFailure = { error ->
                handleManagementFailure(error, fallbackMessage = "챕터를 삭제하지 못했습니다.", onSessionExpired)
                isManagementBusy = false
                false
            },
        )
    }

    private fun updateSubjectState(transform: (SubjectDetail) -> SubjectDetail) {
        state = when (val currentState = state) {
            is SubjectDetailUiState.Success -> currentState.copy(
                subject = transform(currentState.subject),
            )

            else -> currentState
        }
    }

    private fun handleManagementFailure(
        error: Throwable,
        fallbackMessage: String,
        onSessionExpired: () -> Unit,
    ) {
        if (error is SubjectException && error.isUnauthorized) {
            onSessionExpired()
        } else {
            managementMessage = error.toUserFacingMessage(fallbackMessage)
        }
    }
}
