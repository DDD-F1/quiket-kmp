package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.quiz.QuizPlayClient
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayRemoteDataSource
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayRemoteDataSourceImpl
import com.f1.quiket.composeapp.quiz.data.repository.QuizPlayRepositoryImpl
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository
import com.f1.quiket.composeapp.quiz.domain.usecase.BuildQuizCreateRequestUseCase
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizPlayStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizStartStateHolder
import org.koin.dsl.module

internal val quizModule = module {
    single {
        QuizPlayClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
        )
    }
    single<QuizPlayRemoteDataSource> { QuizPlayRemoteDataSourceImpl(client = get()) }
    single<QuizPlayRepository> { QuizPlayRepositoryImpl(remoteDataSource = get()) }
    single { BuildQuizCreateRequestUseCase() }
    single { QuizPlayUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory {
        QuizCreateStateHolder(
            subjectUseCases = get(),
            quizPlayUseCases = get(),
            buildQuizCreateRequest = get(),
        )
    }
    factory { QuizPlayStateHolder(quizPlayUseCases = get()) }
    factory { QuizStartStateHolder(quizPlayUseCases = get()) }
}
