package com.f1.quiket.feature.sample.data.di

import com.f1.quiket.feature.sample.data.remote.PokeApi
import com.f1.quiket.feature.sample.data.repository.PokemonRepositoryImpl
import com.f1.quiket.feature.sample.domain.repository.PokemonRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class PokemonSampleRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        repository: PokemonRepositoryImpl,
    ): PokemonRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PokemonSampleNetworkModule {
    @Provides
    @Singleton
    @Named("pokemonSampleRetrofit")
    fun providePokemonSampleRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun providePokeApi(
        @Named("pokemonSampleRetrofit") retrofit: Retrofit,
    ): PokeApi = retrofit.create(PokeApi::class.java)
}
