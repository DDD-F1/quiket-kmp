package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.util.runSuspendCatching

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.subject.domain.model.Certificate
import com.f1.quiket.composeapp.subject.domain.model.CreatedSubject
import com.f1.quiket.composeapp.subject.domain.model.SubjectCreateInput
import com.f1.quiket.composeapp.subject.domain.model.SubjectException
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases

internal class SubjectDetailsEditStateHolder(
    private val subjectUseCases: SubjectUseCases,
) {
    var certificates by mutableStateOf(emptyList<Certificate>())
        private set

    var isLoadingCertificates by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadCertificates(onSessionExpired: () -> Unit) {
        isLoadingCertificates = true
        runSuspendCatching {
            subjectUseCases.getCertificates()
        }.onSuccess { loadedCertificates ->
            certificates = loadedCertificates
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                certificates = emptyList()
            }
        }
        isLoadingCertificates = false
    }

    fun clearFeedback() {
        feedbackMessage = null
    }

    suspend fun updateSubjectDetails(
        subjectId: String,
        input: SubjectCreateInput,
        onSessionExpired: () -> Unit,
    ): CreatedSubject? {
        if (isSubmitting) return null

        isSubmitting = true
        feedbackMessage = null

        return runSuspendCatching {
            subjectUseCases.updateSubjectDetails(
                subjectId = subjectId,
                input = input,
            )
        }.fold(
            onSuccess = { updatedSubject ->
                isSubmitting = false
                updatedSubject
            },
            onFailure = { error ->
                if (error is SubjectException && error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    feedbackMessage = error.toUserFacingMessage("과목 유형을 수정하지 못했습니다.")
                }
                isSubmitting = false
                null
            },
        )
    }
}
