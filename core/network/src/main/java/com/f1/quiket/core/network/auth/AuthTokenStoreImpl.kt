package com.f1.quiket.core.network.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AuthTokenStoreImpl @Inject constructor(
    @param:AuthTokenDataStore private val dataStore: DataStore<Preferences>,
) : AuthTokenStore {
    override fun observeTokenPair(): Flow<TokenPair?> = dataStore.data
        .map { preferences -> preferences.toTokenPair() }

    override suspend fun getTokenPair(): TokenPair? = observeTokenPair().first()

    override suspend fun saveTokenPair(tokenPair: TokenPair) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = tokenPair.accessToken
            preferences[Keys.REFRESH_TOKEN] = tokenPair.refreshToken
            preferences[Keys.TOKEN_TYPE] = tokenPair.tokenType
            preferences[Keys.ACCESS_TOKEN_EXPIRES_IN] = tokenPair.accessTokenExpiresIn
            preferences[Keys.REFRESH_TOKEN_EXPIRES_IN] = tokenPair.refreshTokenExpiresIn
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
            preferences.remove(Keys.REFRESH_TOKEN)
            preferences.remove(Keys.TOKEN_TYPE)
            preferences.remove(Keys.ACCESS_TOKEN_EXPIRES_IN)
            preferences.remove(Keys.REFRESH_TOKEN_EXPIRES_IN)
        }
    }

    private fun Preferences.toTokenPair(): TokenPair? {
        val accessToken = this[Keys.ACCESS_TOKEN] ?: return null
        val refreshToken = this[Keys.REFRESH_TOKEN] ?: return null
        val tokenType = this[Keys.TOKEN_TYPE] ?: return null
        val accessTokenExpiresIn = this[Keys.ACCESS_TOKEN_EXPIRES_IN] ?: return null
        val refreshTokenExpiresIn = this[Keys.REFRESH_TOKEN_EXPIRES_IN] ?: return null

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            accessTokenExpiresIn = accessTokenExpiresIn,
            refreshTokenExpiresIn = refreshTokenExpiresIn,
        )
    }

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        val TOKEN_TYPE = stringPreferencesKey("auth_token_type")
        val ACCESS_TOKEN_EXPIRES_IN = longPreferencesKey("auth_access_token_expires_in")
        val REFRESH_TOKEN_EXPIRES_IN = longPreferencesKey("auth_refresh_token_expires_in")
    }
}
