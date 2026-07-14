package com.f1.quiket.composeapp.history.presentation

import com.f1.quiket.composeapp.history.domain.model.HistoryActivity

data class HistoryUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasLoaded: Boolean = false,
    val activities: List<HistoryActivity> = emptyList(),
    val page: Int = 0,
    val hasNext: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val PageSize = 20
    }
}
