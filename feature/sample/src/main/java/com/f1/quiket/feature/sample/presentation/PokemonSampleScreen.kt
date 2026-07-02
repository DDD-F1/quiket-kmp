package com.f1.quiket.feature.sample.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.f1.quiket.core.designsystem.component.PokemonPrimaryButton
import com.f1.quiket.core.designsystem.component.PokemonTopBar
import com.f1.quiket.core.designsystem.theme.quiketSpacing
import com.f1.quiket.feature.sample.R
import com.f1.quiket.feature.sample.domain.model.PokemonListItem

@Composable
fun PokemonSampleScreen(
    state: PokemonSampleState,
    allPokemon: LazyPagingItems<PokemonListItem>,
    snackbarHostState: SnackbarHostState,
    onIntent: (PokemonSampleIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { PokemonTopBar(title = stringResource(id = R.string.pokemon_sample_title)) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                    PokemonSampleTab.entries.forEach { tab ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { onIntent(PokemonSampleIntent.SelectTab(tab)) },
                            text = {
                                Text(
                                    text = when (tab) {
                                        PokemonSampleTab.All -> stringResource(R.string.pokemon_sample_tab_all)
                                        PokemonSampleTab.Liked -> stringResource(R.string.pokemon_sample_tab_liked)
                                    },
                                )
                            },
                        )
                    }
                }

                when (state.selectedTab) {
                    PokemonSampleTab.All -> PokemonAllGrid(
                        allPokemon = allPokemon,
                        favoriteIds = state.favoriteIds,
                        onToggleFavorite = { item -> onIntent(PokemonSampleIntent.ToggleFavorite(item)) },
                    )

                    PokemonSampleTab.Liked -> PokemonLikedGrid(
                        state = state,
                        onToggleFavorite = { item -> onIntent(PokemonSampleIntent.ToggleFavorite(item)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonAllGrid(
    allPokemon: LazyPagingItems<PokemonListItem>,
    favoriteIds: Set<Int>,
    onToggleFavorite: (PokemonListItem) -> Unit,
) {
    val spacing = quiketSpacing
    val refreshState = allPokemon.loadState.refresh

    when {
        refreshState is LoadState.Loading -> FullScreenMessage {
            CircularProgressIndicator()
        }

        refreshState is LoadState.Error && allPokemon.itemCount == 0 -> FullScreenMessage {
            EmptyOrErrorState(
                message = stringResource(id = R.string.pokemon_sample_error_loading),
                buttonLabel = stringResource(id = R.string.pokemon_sample_retry),
                onClick = allPokemon::retry,
            )
        }

        refreshState is LoadState.NotLoading && allPokemon.itemCount == 0 -> FullScreenMessage {
            Text(
                text = stringResource(id = R.string.pokemon_sample_empty_all),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                items(
                    count = allPokemon.itemCount,
                    key = allPokemon.itemKey { item -> item.id },
                ) { index ->
                    val item = allPokemon[index] ?: return@items

                    PokemonSampleCard(
                        pokemonId = item.id,
                        name = item.name,
                        isFavorite = favoriteIds.contains(item.id),
                        onToggleFavorite = { onToggleFavorite(item) },
                    )
                }

                when (val appendState = allPokemon.loadState.append) {
                    is LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.small),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyOrErrorState(
                                message = stringResource(id = R.string.pokemon_sample_error_loading_more),
                                buttonLabel = stringResource(id = R.string.pokemon_sample_retry),
                                onClick = allPokemon::retry,
                            )
                        }
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun PokemonLikedGrid(
    state: PokemonSampleState,
    onToggleFavorite: (PokemonListItem) -> Unit,
) {
    val spacing = quiketSpacing

    if (state.likedPokemon.isEmpty()) {
        FullScreenMessage {
            Text(
                text = stringResource(id = R.string.pokemon_sample_empty_liked),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        items(
            items = state.likedPokemon,
            key = { item -> item.pokemonId },
        ) { item ->
            PokemonSampleCard(
                pokemonId = item.pokemonId,
                name = item.name,
                isFavorite = true,
                onToggleFavorite = {
                    onToggleFavorite(
                        PokemonListItem(
                            id = item.pokemonId,
                            name = item.name,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun PokemonSampleCard(
    pokemonId: Int,
    name: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    val spacing = quiketSpacing

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = stringResource(id = R.string.pokemon_sample_id_format, pokemonId),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = name.toPokemonDisplayName(),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = if (isFavorite) {
                        stringResource(id = R.string.pokemon_sample_unlike)
                    } else {
                        stringResource(id = R.string.pokemon_sample_like)
                    },
                )
            }
        }
    }
}

@Composable
private fun FullScreenMessage(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun EmptyOrErrorState(
    message: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    val spacing = quiketSpacing

    Column(
        modifier = Modifier.padding(horizontal = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
        PokemonPrimaryButton(
            label = buttonLabel,
            onClick = onClick,
        )
    }
}

private fun String.toPokemonDisplayName(): String = split('-')
    .joinToString(separator = " ") { token ->
        token.replaceFirstChar { char ->
            if (char.isLowerCase()) {
                char.titlecase()
            } else {
                char.toString()
            }
        }
    }
