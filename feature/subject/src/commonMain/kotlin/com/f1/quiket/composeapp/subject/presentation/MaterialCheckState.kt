package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.subject.domain.model.LectureUploadProgress

internal sealed interface MaterialCheckUiState {
    data object Loading : MaterialCheckUiState
    data class Success(val progress: LectureUploadProgress) : MaterialCheckUiState
    data class Error(val message: String) : MaterialCheckUiState
}
