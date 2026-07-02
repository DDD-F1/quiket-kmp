package com.f1.quiket.core.network.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthHeaderInterceptorModule {
    @Binds
    @IntoSet
    abstract fun bindAuthHeaderInterceptor(
        interceptor: AuthHeaderInterceptor,
    ): Interceptor
}
