package com.f1.quiket.feature.history.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.history.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : MviViewModel<HistoryState, HistoryIntent, HistoryEffect>(
    initialState = HistoryState(),
) {
    init {
        load(reset = true)
    }

    override fun handleIntent(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.Load -> load(reset = true)
            HistoryIntent.Refresh -> load(reset = true)
            HistoryIntent.LoadMore -> load(reset = false)
        }
    }

    private fun load(reset: Boolean) {
        val state = currentState
        if (state.isLoading || state.isLoadingMore) return
        if (!reset && !state.hasNext) return

        launch {
            val nextPage = if (reset) 0 else state.page + 1
            updateState {
                if (reset) {
                    copy(
                        isLoading = activities.isEmpty(),
                        isLoadingMore = false,
                        errorMessage = null,
                    )
                } else {
                    copy(
                        isLoadingMore = true,
                        errorMessage = null,
                    )
                }
            }

            when (val result = historyRepository.getRecentActivities(page = nextPage, size = PAGE_SIZE)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            isLoadingMore = false,
                            activities = if (reset) {
                                result.data.activities
                            } else {
                                activities + result.data.activities
                            },
                            page = result.data.page,
                            hasNext = result.data.hasNext,
                            errorMessage = null,
                        )
                    }
                }

                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = result.message,
                        )
                    }
                    sendEffect(HistoryEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
