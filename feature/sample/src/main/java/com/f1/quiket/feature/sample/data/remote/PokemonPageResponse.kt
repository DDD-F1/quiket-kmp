package com.f1.quiket.feature.sample.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PokemonPageResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<PokemonPageItemResponse>,
)

@Serializable
data class PokemonPageItemResponse(
    val name: String,
    val url: String,
)
