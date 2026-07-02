package com.f1.quiket.feature.sample.navigation

import com.f1.quiket.core.navigation.QuiketDestination

data object PokemonSampleDestination : QuiketDestination {
    override val route: String = "sample/pokemon"
}
