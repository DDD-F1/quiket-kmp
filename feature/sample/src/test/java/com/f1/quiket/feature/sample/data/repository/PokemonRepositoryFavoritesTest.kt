package com.f1.quiket.feature.sample.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.database.db.QuiketDatabase
import com.f1.quiket.feature.sample.data.remote.PokeApi
import com.f1.quiket.feature.sample.data.remote.PokemonPageResponse
import com.f1.quiket.feature.sample.domain.model.PokemonListItem
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PokemonRepositoryFavoritesTest {
    private lateinit var database: QuiketDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuiketDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `toggle favorite stores ordered liked list and removes on second tap`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = PokemonRepositoryImpl(
            api = EmptyPokeApi,
            favoritePokemonDao = database.favoritePokemonDao(),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

        repository.toggleFavorite(PokemonListItem(id = 30, name = "nidorina"))
        repository.toggleFavorite(PokemonListItem(id = 10, name = "caterpie"))
        repository.toggleFavorite(PokemonListItem(id = 20, name = "raticate"))

        val favorites = repository.observeFavorites().first()
        assertThat(favorites.map { favorite -> favorite.pokemonId }).containsExactly(10, 20, 30).inOrder()

        repository.toggleFavorite(PokemonListItem(id = 20, name = "raticate"))

        assertThat(repository.observeFavoriteIds().first()).containsExactly(10, 30)
    }
}

private object EmptyPokeApi : PokeApi {
    override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPageResponse =
        PokemonPageResponse(
            count = 0,
            next = null,
            results = emptyList(),
        )
}
