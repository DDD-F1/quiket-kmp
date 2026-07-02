package com.f1.quiket.composeapp.subject.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadStatus
import com.f1.quiket.composeapp.subject.domain.model.SubjectException
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import com.f1.quiket.composeapp.subject.toSubjectNetworkAwareMessage
import com.f1.quiket.composeapp.subject.withSubjectNetworkRetryGuide
import kotlinx.coroutines.delay

internal class MaterialCheckStateHolder(
    private val subjectUseCases: SubjectUseCases,
) {
    var state by mutableStateOf<MaterialCheckUiState>(MaterialCheckUiState.Loading)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadUploadStatus(
        lectureUploadId: String,
        onSessionExpired: () -> Unit,
    ) {
        state = MaterialCheckUiState.Loading
        feedbackMessage = null
        var consecutiveFailureCount = 0

        repeat(MaterialCheckPollingAttempts) { attempt ->
            runCatching {
                subjectUseCases.getLectureUploadStatus(lectureUploadId = lectureUploadId)
            }.onSuccess { progress ->
                consecutiveFailureCount = 0
                when (progress.status) {
                    LectureUploadStatus.Completed -> {
                        state = MaterialCheckUiState.Success(progress)
                        return
                    }

                    LectureUploadStatus.Failed -> {
                        state = MaterialCheckUiState.Error(
                            progress.failMessage ?: progress.failReason ?: "자료 정리에 실패했어요.",
                        )
                        return
                    }

                    else -> {
                        if (attempt < MaterialCheckPollingAttempts - 1) {
                            delay(MaterialCheckPollingIntervalMillis)
                        }
                    }
                }
            }.onFailure { error ->
                if (error is SubjectException && error.isUnauthorized) {
                    onSessionExpired()
                    return
                }
                consecutiveFailureCount += 1
                if (consecutiveFailureCount >= MaterialCheckMaxPollingFailures) {
                    state = MaterialCheckUiState.Error(
                        error.toSubjectNetworkAwareMessage(
                            fallback = "자료 상태를 불러오지 못했습니다.",
                            timeoutMessage = "자료 상태를 불러오지 못했습니다.".withSubjectNetworkRetryGuide(),
                        ),
                    )
                    return
                }
                if (attempt < MaterialCheckPollingAttempts - 1) {
                    delay(MaterialCheckPollingIntervalMillis)
                }
            }
        }

        state = MaterialCheckUiState.Error("자료 분석 시간이 예상보다 길어지고 있어요. 잠시 후 과목 화면에서 다시 확인해주세요.")
    }

    suspend fun updateChapterName(
        name: String,
        onSessionExpired: () -> Unit,
    ) {
        val current = (state as? MaterialCheckUiState.Success)?.progress ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            feedbackMessage = "챕터명을 입력해주세요"
            return
        }
        if (trimmedName.length > MaterialNameMaxLength) {
            feedbackMessage = "챕터명은 ${MaterialNameMaxLength}자 이하로 입력해주세요"
            return
        }

        isSaving = true
        feedbackMessage = null
        runCatching {
            subjectUseCases.updateChapterName(
                chapterId = current.chapterId,
                name = trimmedName,
            )
        }.onSuccess { chapter ->
            state = MaterialCheckUiState.Success(current.copy(chapterName = chapter.name))
            feedbackMessage = "챕터명이 수정됐어요"
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                feedbackMessage = error.toSubjectNetworkAwareMessage(
                    fallback = "챕터명을 수정하지 못했습니다.",
                    timeoutMessage = "챕터명을 수정하지 못했습니다.".withSubjectNetworkRetryGuide(),
                )
            }
        }
        isSaving = false
    }

    suspend fun updatePartName(
        partId: String,
        name: String,
        onSessionExpired: () -> Unit,
    ) {
        val current = (state as? MaterialCheckUiState.Success)?.progress ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            feedbackMessage = "파트명을 입력해주세요"
            return
        }
        if (trimmedName.length > MaterialNameMaxLength) {
            feedbackMessage = "파트명은 ${MaterialNameMaxLength}자 이하로 입력해주세요"
            return
        }

        isSaving = true
        feedbackMessage = null
        runCatching {
            val part = subjectUseCases.getPart(partId = partId)
            subjectUseCases.updatePart(
                partId = partId,
                name = trimmedName,
                content = part.content.orEmpty(),
            )
        }.onSuccess { updatedPart ->
            state = MaterialCheckUiState.Success(
                current.copy(
                    parts = current.parts.map { part ->
                        if (part.id == partId) {
                            part.copy(name = updatedPart.name)
                        } else {
                            part
                        }
                    },
                ),
            )
            feedbackMessage = "파트명이 수정됐어요"
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                feedbackMessage = error.toSubjectNetworkAwareMessage(
                    fallback = "파트명을 수정하지 못했습니다.",
                    timeoutMessage = "파트명을 수정하지 못했습니다.".withSubjectNetworkRetryGuide(),
                )
            }
        }
        isSaving = false
    }
}

private const val MaterialCheckPollingIntervalMillis = 2_000L
private const val MaterialCheckPollingAttempts = 60
private const val MaterialCheckMaxPollingFailures = 3
private const val MaterialNameMaxLength = 30
