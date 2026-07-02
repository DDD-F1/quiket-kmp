package com.f1.quiket.feature.sample.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.database.dao.FavoritePokemonDao
import com.f1.quiket.feature.sample.data.mapper.toEntity
import com.f1.quiket.feature.sample.data.mapper.toFavoritePokemon
import com.f1.quiket.feature.sample.data.remote.PokeApi
import com.f1.quiket.feature.sample.domain.model.FavoritePokemon
import com.f1.quiket.feature.sample.domain.model.PokemonListItem
import com.f1.quiket.feature.sample.domain.repository.PokemonRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApi,
    private val favoritePokemonDao: FavoritePokemonDao,
    private val dispatchers: AppDispatchers,
) : PokemonRepository {
    override fun pager(): Flow<PagingData<PokemonListItem>> = Pager(
        config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE,
            initialLoadSize = NETWORK_PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
        ),
        pagingSourceFactory = { PokemonPagingSource(api = api) },
    ).flow

    override fun observeFavoriteIds(): Flow<Set<Int>> = favoritePokemonDao.observeFavoriteIds()
        .map { ids -> ids.toSet() }

    override fun observeFavorites(): Flow<List<FavoritePokemon>> =
        favoritePokemonDao.observeAllOrderById()
            .map { entities -> entities.map { entity -> entity.toFavoritePokemon() } }

    override suspend fun toggleFavorite(item: PokemonListItem) = withContext(dispatchers.io) {
        if (favoritePokemonDao.isFavorite(item.id)) {
            favoritePokemonDao.deleteById(item.id)
        } else {
            favoritePokemonDao.upsert(item.toEntity())
        }
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 40
        const val PREFETCH_DISTANCE = 10
    }
}
