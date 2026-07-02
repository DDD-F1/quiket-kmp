package com.f1.quiket.core.network.retrofit

import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.ErrorDataResponse
import com.f1.quiket.core.network.model.NetworkResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Response

class NetworkErrorMapper @Inject constructor(
    private val json: Json,
) {
    fun map(response: Response<*>): NetworkResult.Failure =
        map(httpCode = response.code(), errorBody = response.errorBody()?.string())

    fun map(
        httpCode: Int?,
        errorBody: String?,
    ): NetworkResult.Failure {
        val errorResponse = errorBody
            ?.takeIf(String::isNotBlank)
            ?.let(::parseErrorResponse)

        return NetworkResult.Failure(
            code = errorResponse?.code ?: fallbackCode(httpCode),
            message = errorResponse?.message ?: fallbackMessage(httpCode),
            httpCode = httpCode,
            fieldErrors = errorResponse?.data?.fieldErrors
                ?.mapNotNull { fieldError -> fieldError.toNetworkFieldError() }
                ?: emptyList(),
        )
    }

    fun map(throwable: Throwable): NetworkResult.Failure = NetworkResult.Failure(
        code = when (throwable) {
            is IOException -> "NETWORK_ERROR"
            is SerializationException -> "RESPONSE_PARSE_ERROR"
            else -> "UNKNOWN_ERROR"
        },
        message = throwable.message ?: "요청 처리 중 오류가 발생했습니다.",
        cause = throwable,
    )

    fun mapEnvelopeFailure(
        httpCode: Int?,
        code: String,
        message: String,
    ): NetworkResult.Failure = NetworkResult.Failure(
        code = code.ifBlank { fallbackCode(httpCode) },
        message = message.ifBlank { fallbackMessage(httpCode) },
        httpCode = httpCode,
    )

    private fun parseErrorResponse(errorBody: String): ApiResponse<ErrorDataResponse>? =
        runCatching {
            json.decodeFromString<ApiResponse<ErrorDataResponse>>(errorBody)
        }.getOrNull()

    private fun fallbackCode(httpCode: Int?): String = when (httpCode) {
        400 -> "COMMON_BAD_REQUEST"
        401 -> "COMMON_UNAUTHORIZED"
        403 -> "COMMON_FORBIDDEN"
        404 -> "COMMON_NOT_FOUND"
        409 -> "COMMON_CONFLICT"
        else -> "COMMON_HTTP_ERROR"
    }

    private fun fallbackMessage(httpCode: Int?): String = when (httpCode) {
        400 -> "요청 값이 올바르지 않습니다."
        401 -> "인증이 필요합니다."
        403 -> "요청 권한이 없습니다."
        404 -> "요청한 리소스를 찾을 수 없습니다."
        409 -> "요청이 현재 상태와 충돌합니다."
        else -> "요청에 실패했습니다."
    }
}
