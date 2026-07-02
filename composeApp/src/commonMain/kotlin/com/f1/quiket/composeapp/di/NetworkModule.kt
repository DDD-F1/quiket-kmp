package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.network.ApiConfig
import com.f1.quiket.composeapp.network.apiJson
import com.f1.quiket.composeapp.network.createHttpClient
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val baseUrlQualifier = named("baseUrl")

internal val networkModule = module {
    single<Json> { apiJson }
    single(baseUrlQualifier) { ApiConfig.ApiBaseUrl }
    single<HttpClient> { createHttpClient(json = get()) }
}
