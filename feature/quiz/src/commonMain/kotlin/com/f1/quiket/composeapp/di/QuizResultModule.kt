package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.result.data.remote.QuizResultClient
import com.f1.quiket.composeapp.result.data.remote.QuizResultRemoteDataSource
import com.f1.quiket.composeapp.result.data.remote.QuizResultRemoteDataSourceImpl
import com.f1.quiket.composeapp.result.data.repository.QuizResultRepositoryImpl
import com.f1.quiket.composeapp.result.domain.repository.QuizResultRepository
import com.f1.quiket.composeapp.result.domain.usecase.QuizResultUseCases
import com.f1.quiket.composeapp.result.presentation.QuizResultStateHolder
import org.koin.dsl.module

val quizResultModule = module {
    single {
        QuizResultClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
        )
    }
    single<QuizResultRemoteDataSource> { QuizResultRemoteDataSourceImpl(client = get()) }
    single<QuizResultRepository> { QuizResultRepositoryImpl(remoteDataSource = get()) }
    single { QuizResultUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory {
        QuizResultStateHolder(
            quizResultUseCases = get(),
            quizPlayUseCases = get(),
        )
    }
}
