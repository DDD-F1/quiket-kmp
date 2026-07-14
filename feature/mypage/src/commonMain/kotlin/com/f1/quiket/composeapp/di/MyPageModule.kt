package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.mypage.data.remote.MyPageClient
import com.f1.quiket.composeapp.mypage.data.remote.MyPageRemoteDataSource
import com.f1.quiket.composeapp.mypage.data.remote.MyPageRemoteDataSourceImpl
import com.f1.quiket.composeapp.mypage.data.repository.MyPageRepositoryImpl
import com.f1.quiket.composeapp.mypage.domain.repository.MyPageRepository
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.mypage.presentation.AccountSettingsStateHolder
import com.f1.quiket.composeapp.mypage.presentation.InquiryStateHolder
import com.f1.quiket.composeapp.mypage.presentation.NotificationSettingsStateHolder
import org.koin.dsl.module

val myPageModule = module {
    single {
        MyPageClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
        )
    }
    single<MyPageRemoteDataSource> { MyPageRemoteDataSourceImpl(client = get()) }
    single<MyPageRepository> { MyPageRepositoryImpl(remoteDataSource = get()) }
    single { MyPageUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory { AccountSettingsStateHolder(myPageUseCases = get()) }
    factory { InquiryStateHolder(myPageUseCases = get()) }
    factory { NotificationSettingsStateHolder(myPageUseCases = get()) }
}
