package com.f1.quiket.core.database.di

import android.content.Context
import androidx.room.Room
import com.f1.quiket.core.database.db.QuiketDatabase
import com.f1.quiket.core.database.dao.FavoritePokemonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideQuiketDatabase(
        @ApplicationContext context: Context,
    ): QuiketDatabase = Room.databaseBuilder(
        context,
        QuiketDatabase::class.java,
        "quiket.db",
    ).build()

    @Provides
    fun provideFavoritePokemonDao(
        database: QuiketDatabase,
    ): FavoritePokemonDao = database.favoritePokemonDao()
}
