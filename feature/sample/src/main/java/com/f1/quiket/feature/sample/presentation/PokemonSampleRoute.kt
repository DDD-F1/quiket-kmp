package com.f1.quiket.feature.sample.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun PokemonSampleRoute(
    viewModel: PokemonSampleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val allPokemon = viewModel.allPokemon.collectAsLazyPagingItems()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PokemonSampleEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    PokemonSampleScreen(
        state = state,
        allPokemon = allPokemon,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}
