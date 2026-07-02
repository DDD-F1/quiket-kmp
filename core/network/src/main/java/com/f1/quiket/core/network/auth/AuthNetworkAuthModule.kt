package com.f1.quiket.core.network.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthNetworkAuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthTokenRefreshClient(
        client: AuthTokenRefreshClientImpl,
    ): AuthTokenRefreshClient

    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(
        provider: AndroidDeviceInfoProvider,
    ): DeviceInfoProvider
}
