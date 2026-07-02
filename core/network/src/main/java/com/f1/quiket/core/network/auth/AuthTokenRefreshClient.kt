package com.f1.quiket.core.network.auth

interface AuthTokenRefreshClient {
    suspend fun refresh(refreshToken: String): AuthTokenRefreshResult
}

sealed interface AuthTokenRefreshResult {
    data class Success(
        val tokenPair: TokenPair,
    ) : AuthTokenRefreshResult

    data object InvalidRefreshToken : AuthTokenRefreshResult

    data object Failure : AuthTokenRefreshResult
}
