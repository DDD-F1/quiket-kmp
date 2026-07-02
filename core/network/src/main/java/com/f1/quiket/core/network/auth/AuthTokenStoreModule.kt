package com.f1.quiket.core.network.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthTokenDataStore

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthTokenStoreBindingModule {
    @Binds
    @Singleton
    abstract fun bindAuthTokenStore(
        store: AuthTokenStoreImpl,
    ): AuthTokenStore
}

@Module
@InstallIn(SingletonComponent::class)
object AuthTokenStoreProviderModule {
    @Provides
    @Singleton
    @AuthTokenDataStore
    fun provideAuthTokenDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(AUTH_TOKEN_DATA_STORE_NAME) },
    )

    private const val AUTH_TOKEN_DATA_STORE_NAME = "auth_tokens"
}
