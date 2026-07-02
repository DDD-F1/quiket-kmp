package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.history.data.remote.HistoryClient
import com.f1.quiket.composeapp.history.data.remote.HistoryRemoteDataSource
import com.f1.quiket.composeapp.history.data.remote.HistoryRemoteDataSourceImpl
import com.f1.quiket.composeapp.history.data.repository.HistoryRepositoryImpl
import com.f1.quiket.composeapp.history.domain.repository.HistoryRepository
import com.f1.quiket.composeapp.history.domain.usecase.HistoryUseCases
import org.koin.dsl.module

internal val historyModule = module {
    single {
        HistoryClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
        )
    }
    single<HistoryRemoteDataSource> { HistoryRemoteDataSourceImpl(client = get()) }
    single<HistoryRepository> { HistoryRepositoryImpl(remoteDataSource = get()) }
    single { HistoryUseCases(authenticatedCallRunner = get(), repository = get()) }
}
