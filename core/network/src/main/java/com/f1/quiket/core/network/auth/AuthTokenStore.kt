package com.f1.quiket.core.network.auth

import kotlinx.coroutines.flow.Flow

interface AuthTokenStore {
    fun observeTokenPair(): Flow<TokenPair?>

    suspend fun getTokenPair(): TokenPair?

    suspend fun saveTokenPair(tokenPair: TokenPair)

    suspend fun clear()
}
