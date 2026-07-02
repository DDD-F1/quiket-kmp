package com.f1.quiket.feature.mypage.data.di

import com.f1.quiket.feature.mypage.data.remote.MyPageApi
import com.f1.quiket.feature.mypage.data.repository.MyPageRepositoryImpl
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class MyPageRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMyPageRepository(
        repository: MyPageRepositoryImpl,
    ): MyPageRepository
}

@Module
@InstallIn(SingletonComponent::class)
object MyPageNetworkModule {
    @Provides
    @Singleton
    fun provideMyPageApi(retrofit: Retrofit): MyPageApi =
        retrofit.create(MyPageApi::class.java)
}
