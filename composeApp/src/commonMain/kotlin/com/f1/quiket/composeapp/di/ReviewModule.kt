package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.review.ReviewClient
import org.koin.dsl.module

internal val reviewModule = module {
    single {
        ReviewClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
        )
    }
}
