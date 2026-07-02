package com.f1.quiket.feature.sample.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.feature.sample.domain.model.PokemonListItem
import com.f1.quiket.feature.sample.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

@HiltViewModel
class PokemonSampleViewModel @Inject constructor(
    private val repository: PokemonRepository,
) : MviViewModel<PokemonSampleState, PokemonSampleIntent, PokemonSampleEffect>(PokemonSampleState()) {
    val allPokemon: Flow<PagingData<PokemonListItem>> = repository.pager()
        .cachedIn(viewModelScope)

    init {
        observeFavorites()
    }

    override fun handleIntent(intent: PokemonSampleIntent) {
        when (intent) {
            is PokemonSampleIntent.SelectTab -> updateState { copy(selectedTab = intent.tab) }
            is PokemonSampleIntent.ToggleFavorite -> toggleFavorite(intent.item)
        }
    }

    private fun observeFavorites() {
        launch {
            combine(
                repository.observeFavoriteIds(),
                repository.observeFavorites(),
            ) { favoriteIds, likedPokemon ->
                favoriteIds to likedPokemon
            }.collect { (favoriteIds, likedPokemon) ->
                updateState {
                    copy(
                        favoriteIds = favoriteIds,
                        likedPokemon = likedPokemon,
                    )
                }
            }
        }
    }

    private fun toggleFavorite(item: PokemonListItem) {
        launch {
            runCatching {
                repository.toggleFavorite(item)
            }.onFailure { throwable ->
                Timber.e(throwable, "Failed to toggle favorite pokemon %s", item.id)
                sendEffect(PokemonSampleEffect.ShowMessage("좋아요 상태를 저장하지 못했습니다."))
            }
        }
    }
}
