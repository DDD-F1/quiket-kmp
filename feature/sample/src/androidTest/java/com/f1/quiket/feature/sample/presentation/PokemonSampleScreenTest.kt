package com.f1.quiket.feature.sample.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.sample.domain.model.PokemonListItem
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class PokemonSampleScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allTabRendersGridItems() {
        composeRule.setContent {
            QuiketTheme {
                val allPokemon = flowOf(
                    PagingData.from(
                        listOf(
                            PokemonListItem(id = 1, name = "bulbasaur"),
                            PokemonListItem(id = 4, name = "charmander"),
                        ),
                    ),
                ).collectAsLazyPagingItems()

                PokemonSampleScreen(
                    state = PokemonSampleState(),
                    allPokemon = allPokemon,
                    snackbarHostState = SnackbarHostState(),
                    onIntent = {},
                )
            }
        }

        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
    }

    @Test
    fun likedTabShowsEmptyState() {
        composeRule.setContent {
            QuiketTheme {
                val allPokemon = flowOf(PagingData.from(emptyList<PokemonListItem>()))
                    .collectAsLazyPagingItems()

                PokemonSampleScreen(
                    state = PokemonSampleState(selectedTab = PokemonSampleTab.Liked),
                    allPokemon = allPokemon,
                    snackbarHostState = SnackbarHostState(),
                    onIntent = {},
                )
            }
        }

        composeRule.onNodeWithText("좋아요한 포켓몬이 없습니다.").assertIsDisplayed()
    }
}
