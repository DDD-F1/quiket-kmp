package com.f1.quiket.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDataResponse(
    val fieldErrors: List<FieldErrorResponse> = emptyList(),
)

@Serializable
data class FieldErrorResponse(
    val field: String? = null,
    val reason: String? = null,
) {
    fun toNetworkFieldError(): NetworkFieldError? {
        val safeField = field?.takeIf(String::isNotBlank) ?: return null
        val safeReason = reason?.takeIf(String::isNotBlank) ?: return null
        return NetworkFieldError(
            field = safeField,
            reason = safeReason,
        )
    }
}
