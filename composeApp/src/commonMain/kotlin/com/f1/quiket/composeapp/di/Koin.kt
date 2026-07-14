package com.f1.quiket.composeapp.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

fun initKoin(
    appDeclaration: KoinApplication.() -> Unit = {},
) {
    if (KoinPlatform.getKoinOrNull() != null) return

    startKoin {
        appDeclaration()
        modules(platformModule, commonModule)
    }
}

internal expect val platformModule: Module

internal val commonModule: Module = module {
    includes(appShellModule)
}
