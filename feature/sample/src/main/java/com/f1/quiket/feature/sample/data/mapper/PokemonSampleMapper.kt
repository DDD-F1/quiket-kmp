package com.f1.quiket.feature.sample.data.mapper

import com.f1.quiket.core.database.entity.FavoritePokemonEntity
import com.f1.quiket.feature.sample.data.remote.PokemonPageItemResponse
import com.f1.quiket.feature.sample.domain.model.FavoritePokemon
import com.f1.quiket.feature.sample.domain.model.PokemonListItem

fun PokemonPageItemResponse.toPokemonListItem(): PokemonListItem = PokemonListItem(
    id = url.toPokemonId(),
    name = name,
)

fun FavoritePokemonEntity.toFavoritePokemon(): FavoritePokemon = FavoritePokemon(
    pokemonId = pokemonId,
    name = name,
)

fun PokemonListItem.toEntity(): FavoritePokemonEntity = FavoritePokemonEntity(
    pokemonId = id,
    name = name,
)

fun String.toPokemonId(): Int = trimEnd('/')
    .substringAfterLast('/')
    .toIntOrNull()
    ?: error("Unable to parse pokemon id from url: $this")
