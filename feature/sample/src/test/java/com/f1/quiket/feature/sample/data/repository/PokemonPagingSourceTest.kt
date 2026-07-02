package com.f1.quiket.feature.sample.data.repository

import androidx.paging.PagingSource
import com.f1.quiket.feature.sample.data.remote.PokeApi
import com.f1.quiket.feature.sample.data.remote.PokemonPageItemResponse
import com.f1.quiket.feature.sample.data.remote.PokemonPageResponse
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PokemonPagingSourceTest {
    @Test
    fun `first page parses pokemon ids and keeps next offset`() = runTest {
        val pagingSource = PokemonPagingSource(
            api = FakePokeApi(
                pages = mapOf(
                    0 to PokemonPageResponse(
                        count = 1000,
                        next = "https://pokeapi.co/api/v2/pokemon?offset=40&limit=40",
                        results = listOf(
                            PokemonPageItemResponse(
                                name = "bulbasaur",
                                url = "https://pokeapi.co/api/v2/pokemon/1/",
                            ),
                            PokemonPageItemResponse(
                                name = "ivysaur",
                                url = "https://pokeapi.co/api/v2/pokemon/2/",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 40,
                placeholdersEnabled = false,
            ),
        )

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(40)
        assertThat(page.data.map { item -> item.id }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `paging error is surfaced as load result error`() = runTest {
        val pagingSource = PokemonPagingSource(
            api = object : PokeApi {
                override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPageResponse {
                    error("network down")
                }
            },
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 40,
                loadSize = 40,
                placeholdersEnabled = false,
            ),
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        assertThat((result as PagingSource.LoadResult.Error).throwable)
            .hasMessageThat()
            .contains("network down")
    }
}

private class FakePokeApi(
    private val pages: Map<Int, PokemonPageResponse>,
) : PokeApi {
    override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPageResponse =
        pages[offset] ?: PokemonPageResponse(
            count = 1000,
            next = null,
            results = emptyList(),
        )
}
