package com.f1.quiket.composeapp.home.presentation

import com.f1.quiket.composeapp.home.domain.model.HomeData

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
