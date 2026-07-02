package com.f1.quiket.composeapp.subject.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.subject.Certificate
import com.f1.quiket.composeapp.subject.CreatedSubject
import com.f1.quiket.composeapp.subject.SubjectCreateInput
import com.f1.quiket.composeapp.subject.SubjectException
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases

internal class SubjectCreateStateHolder(
    private val subjectUseCases: SubjectUseCases,
) {
    var existingSubjectNames by mutableStateOf(emptyList<String>())
        private set

    var certificates by mutableStateOf(emptyList<Certificate>())
        private set

    var isLoadingNames by mutableStateOf(true)
        private set

    var isLoadingCertificates by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadInitialData(onSessionExpired: () -> Unit) {
        isLoadingNames = true
        runCatching {
            subjectUseCases.getSubjects().map { it.name }
        }.onSuccess { names ->
            existingSubjectNames = names
            feedbackMessage = null
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                feedbackMessage = error.toUserFacingMessage("기존 과목을 불러오지 못했습니다.")
            }
        }
        isLoadingNames = false

        isLoadingCertificates = true
        runCatching {
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

    fun isDuplicateSubjectName(name: String): Boolean =
        name.isNotBlank() && existingSubjectNames.any { it.equals(name, ignoreCase = true) }

    fun clearFeedback() {
        feedbackMessage = null
    }

    suspend fun createSubject(
        input: SubjectCreateInput,
        onSessionExpired: () -> Unit,
    ): CreatedSubject? {
        if (isSubmitting) return null

        isSubmitting = true
        feedbackMessage = null

        return runCatching {
            subjectUseCases.createSubject(input)
        }.fold(
            onSuccess = { subject ->
                isSubmitting = false
                subject
            },
            onFailure = { error ->
                if (error is SubjectException && error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    feedbackMessage = error.toUserFacingMessage("과목을 만들지 못했습니다.")
                }
                isSubmitting = false
                null
            },
        )
    }
}
