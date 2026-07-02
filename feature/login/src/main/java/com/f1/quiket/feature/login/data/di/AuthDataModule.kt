package com.f1.quiket.feature.login.data.di

import com.f1.quiket.core.session.SessionRepository
import com.f1.quiket.feature.login.data.kakao.KakaoLoginClient
import com.f1.quiket.feature.login.data.kakao.KakaoLoginClientImpl
import com.f1.quiket.feature.login.data.remote.AuthApi
import com.f1.quiket.feature.login.data.repository.AuthRepositoryImpl
import com.f1.quiket.feature.login.data.session.AuthSessionRepository
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        repository: AuthSessionRepository,
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindKakaoLoginClient(
        client: KakaoLoginClientImpl,
    ): KakaoLoginClient
}

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)
}
