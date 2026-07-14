package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import org.koin.dsl.module

val mainModule = module {
    factory {
        MainStateHolder(
            homeUseCases = get(),
            historyUseCases = get(),
            myPageUseCases = get(),
            readSessionUseCase = get(),
            saveHomeGuideCompletedUseCase = get(),
        )
    }
}
