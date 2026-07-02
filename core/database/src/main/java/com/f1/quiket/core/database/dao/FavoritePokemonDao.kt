package com.f1.quiket.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.f1.quiket.core.database.entity.FavoritePokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {
    @Query("SELECT * FROM favorite_pokemon ORDER BY pokemon_id ASC")
    fun observeAllOrderById(): Flow<List<FavoritePokemonEntity>>

    @Query("SELECT pokemon_id FROM favorite_pokemon ORDER BY pokemon_id ASC")
    fun observeFavoriteIds(): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pokemon WHERE pokemon_id = :pokemonId)")
    suspend fun isFavorite(pokemonId: Int): Boolean

    @Upsert
    suspend fun upsert(favorite: FavoritePokemonEntity)

    @Query("DELETE FROM favorite_pokemon WHERE pokemon_id = :pokemonId")
    suspend fun deleteById(pokemonId: Int)
}