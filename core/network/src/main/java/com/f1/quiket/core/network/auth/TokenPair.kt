package com.f1.quiket.core.network.auth

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
)

internal fun TokenPair.toAuthorizationHeader(): String = "$tokenType $accessToken"
