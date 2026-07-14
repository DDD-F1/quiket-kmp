package com.f1.quiket.composeapp.di

import org.koin.core.module.Module
import org.koin.dsl.module

val appShellModule: Module = module {
    includes(
        networkModule,
        authModule,
        homeModule,
        historyModule,
        myPageModule,
        mainModule,
        quizModule,
        quizResultModule,
        subjectModule,
    )
}
