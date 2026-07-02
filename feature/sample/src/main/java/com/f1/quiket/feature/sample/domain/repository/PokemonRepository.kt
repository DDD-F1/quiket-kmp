package com.f1.quiket.feature.sample.domain.repository

import androidx.paging.PagingData
import com.f1.quiket.feature.sample.domain.model.FavoritePokemon
import com.f1.quiket.feature.sample.domain.model.PokemonListItem
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun pager(): Flow<PagingData<PokemonListItem>>

    fun observeFavoriteIds(): Flow<Set<Int>>

    fun observeFavorites(): Flow<List<FavoritePokemon>>

    suspend fun toggleFavorite(item: PokemonListItem)
}
