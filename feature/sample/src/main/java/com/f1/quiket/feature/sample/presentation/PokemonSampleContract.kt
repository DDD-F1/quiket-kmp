package com.f1.quiket.feature.sample.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState
import com.f1.quiket.feature.sample.domain.model.FavoritePokemon
import com.f1.quiket.feature.sample.domain.model.PokemonListItem

data class PokemonSampleState(
    val selectedTab: PokemonSampleTab = PokemonSampleTab.All,
    val favoriteIds: Set<Int> = emptySet(),
    val likedPokemon: List<FavoritePokemon> = emptyList(),
) : UiState

enum class PokemonSampleTab {
    All,
    Liked,
}

sealed interface PokemonSampleIntent : UiIntent {
    data class SelectTab(val tab: PokemonSampleTab) : PokemonSampleIntent
    data class ToggleFavorite(val item: PokemonListItem) : PokemonSampleIntent
}

sealed interface PokemonSampleEffect : UiEffect {
    data class ShowMessage(val message: String) : PokemonSampleEffect
}
