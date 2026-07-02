package com.f1.quiket.feature.sample.presentation

import androidx.paging.PagingData
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.sample.domain.model.FavoritePokemon
import com.f1.quiket.feature.sample.domain.model.PokemonListItem
import com.f1.quiket.feature.sample.domain.repository.PokemonRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonSampleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state selects all tab`() = runTest {
        val viewModel = PokemonSampleViewModel(FakePokemonRepository())

        advanceUntilIdle()

        assertThat(viewModel.state.value.selectedTab).isEqualTo(PokemonSampleTab.All)
    }

    @Test
    fun `select tab intent updates selected tab`() = runTest {
        val viewModel = PokemonSampleViewModel(FakePokemonRepository())

        viewModel.onIntent(PokemonSampleIntent.SelectTab(PokemonSampleTab.Liked))
        advanceUntilIdle()

        assertThat(viewModel.state.value.selectedTab).isEqualTo(PokemonSampleTab.Liked)
    }

    @Test
    fun `toggle favorite updates favorite ids and liked list`() = runTest {
        val repository = FakePokemonRepository()
        val viewModel = PokemonSampleViewModel(repository)
        val item = PokemonListItem(id = 25, name = "pikachu")

        viewModel.onIntent(PokemonSampleIntent.ToggleFavorite(item))
        advanceUntilIdle()

        assertThat(viewModel.state.value.favoriteIds).contains(25)
        assertThat(viewModel.state.value.likedPokemon).contains(
            FavoritePokemon(
                pokemonId = 25,
                name = "pikachu",
            ),
        )
    }
}

private class FakePokemonRepository : PokemonRepository {
    private val favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    private val likedPokemon = MutableStateFlow<List<FavoritePokemon>>(emptyList())

    override fun pager(): Flow<PagingData<PokemonListItem>> = flowOf(PagingData.empty())

    override fun observeFavoriteIds(): Flow<Set<Int>> = favoriteIds

    override fun observeFavorites(): Flow<List<FavoritePokemon>> = likedPokemon

    override suspend fun toggleFavorite(item: PokemonListItem) {
        if (favoriteIds.value.contains(item.id)) {
            favoriteIds.value = favoriteIds.value - item.id
            likedPokemon.value = likedPokemon.value.filterNot { favorite -> favorite.pokemonId == item.id }
        } else {
            favoriteIds.value = favoriteIds.value + item.id
            likedPokemon.value = (likedPokemon.value + FavoritePokemon(item.id, item.name))
                .sortedBy { favorite -> favorite.pokemonId }
        }
    }
}
