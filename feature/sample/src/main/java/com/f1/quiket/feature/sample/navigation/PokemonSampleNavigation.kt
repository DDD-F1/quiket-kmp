package com.f1.quiket.feature.sample.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.f1.quiket.feature.sample.presentation.PokemonSampleRoute

fun NavGraphBuilder.pokemonSampleGraph() {
    composable(route = PokemonSampleDestination.route) {
        PokemonSampleRoute()
    }
}
