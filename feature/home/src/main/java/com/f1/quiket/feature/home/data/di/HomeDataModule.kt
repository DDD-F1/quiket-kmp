package com.f1.quiket.feature.home.data.di

import com.f1.quiket.feature.home.data.remote.HomeApi
import com.f1.quiket.feature.home.data.remote.QuizPlayApi
import com.f1.quiket.feature.home.data.remote.QuizGenerationApi
import com.f1.quiket.feature.home.data.repository.HomeRepositoryImpl
import com.f1.quiket.feature.home.data.repository.QuizPlayRepositoryImpl
import com.f1.quiket.feature.home.data.repository.QuizGenerationRepositoryImpl
import com.f1.quiket.feature.home.domain.repository.HomeRepository
import com.f1.quiket.feature.home.domain.repository.QuizPlayRepository
import com.f1.quiket.feature.home.domain.repository.QuizGenerationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        repository: HomeRepositoryImpl,
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindQuizGenerationRepository(
        repository: QuizGenerationRepositoryImpl,
    ): QuizGenerationRepository

    @Binds
    @Singleton
    abstract fun bindQuizPlayRepository(
        repository: QuizPlayRepositoryImpl,
    ): QuizPlayRepository
}

@Module
@InstallIn(SingletonComponent::class)
object HomeNetworkModule {
    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi =
        retrofit.create(HomeApi::class.java)

    @Provides
    @Singleton
    fun provideQuizGenerationApi(retrofit: Retrofit): QuizGenerationApi =
        retrofit.create(QuizGenerationApi::class.java)

    @Provides
    @Singleton
    fun provideQuizPlayApi(retrofit: Retrofit): QuizPlayApi =
        retrofit.create(QuizPlayApi::class.java)
}
