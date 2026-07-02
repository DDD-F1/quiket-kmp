package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.home.data.remote.HomeClient
import com.f1.quiket.composeapp.home.data.remote.HomeRemoteDataSource
import com.f1.quiket.composeapp.home.data.remote.HomeRemoteDataSourceImpl
import com.f1.quiket.composeapp.home.data.repository.HomeRepositoryImpl
import com.f1.quiket.composeapp.home.domain.repository.HomeRepository
import com.f1.quiket.composeapp.home.domain.usecase.HomeUseCases
import com.f1.quiket.composeapp.home.presentation.ExamScheduleStateHolder
import org.koin.dsl.module

internal val homeModule = module {
    single {
        HomeClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
        )
    }
    single<HomeRemoteDataSource> { HomeRemoteDataSourceImpl(client = get()) }
    single<HomeRepository> { HomeRepositoryImpl(remoteDataSource = get()) }
    single { HomeUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory { ExamScheduleStateHolder(homeUseCases = get()) }
}
