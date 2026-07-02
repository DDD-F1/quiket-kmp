package com.f1.quiket.composeapp.subject.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.subject.PartDetail
import com.f1.quiket.composeapp.subject.PartDetailUiState
import com.f1.quiket.composeapp.subject.SubjectException
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases

internal class PartDetailStateHolder(
    private val subjectUseCases: SubjectUseCases,
) {
    var state by mutableStateOf<PartDetailUiState>(PartDetailUiState.Loading)
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var draftName by mutableStateOf("")
        private set

    var draftContent by mutableStateOf("")
        private set

    var isSaving by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadPart(
        partId: String,
        onSessionExpired: () -> Unit,
    ) {
        state = PartDetailUiState.Loading
        feedbackMessage = null
        runCatching {
            subjectUseCases.getPart(partId = partId)
        }.onSuccess { part ->
            state = PartDetailUiState.Success(part)
            draftName = part.name.take(PartNameMaxLength)
            draftContent = part.content.orEmpty()
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = PartDetailUiState.Error(error.toUserFacingMessage("파트 정보를 불러오지 못했습니다."))
            }
        }
    }

    fun enterEditMode() {
        val part = (state as? PartDetailUiState.Success)?.part ?: return
        draftName = part.name.take(PartNameMaxLength)
        draftContent = part.content.orEmpty()
        feedbackMessage = null
        isEditMode = true
    }

    fun cancelEdit() {
        val part = (state as? PartDetailUiState.Success)?.part
        draftName = part?.name.orEmpty().take(PartNameMaxLength)
        draftContent = part?.content.orEmpty()
        isEditMode = false
        feedbackMessage = null
    }

    fun updateDraftName(name: String) {
        draftName = name.take(PartNameMaxLength)
    }

    fun updateDraftContent(content: String) {
        draftContent = content
    }

    suspend fun saveEdit(
        partId: String,
        onSessionExpired: () -> Unit,
    ): PartDetail? {
        val trimmedName = draftName.trim()
        if (trimmedName.isBlank()) {
            feedbackMessage = "파트명을 입력해주세요"
            return null
        }
        if (trimmedName.length > PartNameMaxLength) {
            feedbackMessage = "파트명은 ${PartNameMaxLength}자 이하로 입력해주세요"
            return null
        }

        isSaving = true
        feedbackMessage = null
        return runCatching {
            subjectUseCases.updatePart(
                partId = partId,
                name = trimmedName,
                content = draftContent,
            )
        }.fold(
            onSuccess = { part ->
                val savedPart = part.copy(
                    name = trimmedName,
                    content = draftContent,
                    contentPreview = draftContent
                        .take(PartContentPreviewMaxLength)
                        .takeIf { it.isNotBlank() },
                )
                state = PartDetailUiState.Success(savedPart)
                draftName = savedPart.name.take(PartNameMaxLength)
                draftContent = savedPart.content.orEmpty()
                isEditMode = false
                feedbackMessage = "파트 내용이 저장됐어요"
                isSaving = false
                savedPart
            },
            onFailure = { error ->
                if (error is SubjectException && error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    feedbackMessage = error.toUserFacingMessage("파트 정보를 저장하지 못했습니다.")
                }
                isSaving = false
                null
            },
        )
    }
}

private const val PartContentPreviewMaxLength = 80
private const val PartNameMaxLength = 30
