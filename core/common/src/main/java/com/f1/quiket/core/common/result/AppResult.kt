package com.f1.quiket.core.common.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(
        val throwable: Throwable,
        val message: String = throwable.message ?: "Unknown error",
    ) : AppResult<Nothing>
}
