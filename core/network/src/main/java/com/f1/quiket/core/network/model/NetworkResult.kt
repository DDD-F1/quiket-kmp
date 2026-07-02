package com.f1.quiket.core.network.model

sealed interface NetworkResult<out T> {
    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>

    data class Failure(
        val code: String,
        val message: String,
        val httpCode: Int? = null,
        val fieldErrors: List<NetworkFieldError> = emptyList(),
        val cause: Throwable? = null,
    ) : NetworkResult<Nothing>
}

data class NetworkFieldError(
    val field: String,
    val reason: String,
)
