package com.f1.quiket.feature.history.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState
import com.f1.quiket.feature.history.domain.model.RecentActivity

data class HistoryState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val activities: List<RecentActivity> = emptyList(),
    val page: Int = 0,
    val hasNext: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface HistoryIntent : UiIntent {
    data object Load : HistoryIntent
    data object Refresh : HistoryIntent
    data object LoadMore : HistoryIntent
}

sealed interface HistoryEffect : UiEffect {
    data class ShowMessage(val message: String) : HistoryEffect
}
