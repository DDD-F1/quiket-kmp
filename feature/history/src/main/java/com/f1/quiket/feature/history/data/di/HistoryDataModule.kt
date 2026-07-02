package com.f1.quiket.feature.history.data.di

import com.f1.quiket.feature.history.data.remote.HistoryApi
import com.f1.quiket.feature.history.data.repository.HistoryRepositoryImpl
import com.f1.quiket.feature.history.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        repository: HistoryRepositoryImpl,
    ): HistoryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object HistoryNetworkModule {
    @Provides
    @Singleton
    fun provideHistoryApi(retrofit: Retrofit): HistoryApi =
        retrofit.create(HistoryApi::class.java)
}
