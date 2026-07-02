package com.f1.quiket.core.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.f1.quiket.core.database.dao.FavoritePokemonDao
import com.f1.quiket.core.database.entity.FavoritePokemonEntity

@Database(
    entities = [FavoritePokemonEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class QuiketDatabase : RoomDatabase() {
    abstract fun favoritePokemonDao(): FavoritePokemonDao
}
