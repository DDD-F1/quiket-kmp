package com.f1.quiket.feature.sample.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.f1.quiket.feature.sample.data.mapper.toPokemonListItem
import com.f1.quiket.feature.sample.data.remote.PokeApi
import com.f1.quiket.feature.sample.domain.model.PokemonListItem

class PokemonPagingSource(
    private val api: PokeApi,
) : PagingSource<Int, PokemonListItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonListItem> {
        val offset = params.key ?: 0
        val limit = params.loadSize

        return try {
            val response = api.getPokemonPage(limit = limit, offset = offset)

            LoadResult.Page(
                data = response.results.map { item -> item.toPokemonListItem() },
                prevKey = if (offset == 0) null else (offset - limit).coerceAtLeast(0),
                nextKey = response.next?.let { offset + limit },
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PokemonListItem>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null

        return closestPage.prevKey?.plus(state.config.pageSize)
            ?: closestPage.nextKey?.minus(state.config.pageSize)
    }
}
