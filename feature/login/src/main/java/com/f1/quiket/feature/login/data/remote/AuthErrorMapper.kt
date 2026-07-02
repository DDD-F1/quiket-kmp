package com.f1.quiket.feature.login.data.remote

import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.feature.login.data.mapper.toDomain
import com.f1.quiket.feature.login.domain.model.AuthResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.Response

class AuthErrorMapper @Inject constructor(
    private val json: Json,
) {
    fun map(response: Response<*>): AuthResult.Failure =
        map(httpCode = response.code(), errorBody = response.errorBody()?.string())

    fun map(
        httpCode: Int?,
        errorBody: String?,
    ): AuthResult.Failure {
        val errorResponse = errorBody
            ?.takeIf(String::isNotBlank)
            ?.let { body -> parseErrorResponse(body) }

        return AuthResult.Failure(
            code = errorResponse?.code ?: fallbackCode(httpCode),
            message = errorResponse?.message ?: fallbackMessage(httpCode),
            httpCode = httpCode,
            fieldErrors = errorResponse?.data?.fieldErrors
                ?.mapNotNull { fieldError -> fieldError.toDomain() }
                ?: emptyList(),
            email = errorResponse?.data?.email,
            failedLoginCount = errorResponse?.data?.failedLoginCount,
            remainingAttempts = errorResponse?.data?.remainingAttempts,
            passwordResetRequired = errorResponse?.data?.passwordResetRequired,
            nextAction = errorResponse?.data?.nextAction,
            resetCodeSent = errorResponse?.data?.resetCodeSent,
        )
    }

    fun map(throwable: Throwable): AuthResult.Failure = AuthResult.Failure(
        code = when (throwable) {
            is IOException -> "NETWORK_ERROR"
            is SerializationException -> "AUTH_RESPONSE_PARSE_ERROR"
            else -> "AUTH_UNKNOWN_ERROR"
        },
        message = throwable.message ?: "인증 요청 처리 중 오류가 발생했습니다.",
        cause = throwable,
    )

    fun mapEnvelopeFailure(
        httpCode: Int?,
        code: String,
        message: String,
    ): AuthResult.Failure = AuthResult.Failure(
        code = code,
        message = message,
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
        else -> "AUTH_HTTP_ERROR"
    }

    private fun fallbackMessage(httpCode: Int?): String = when (httpCode) {
        400 -> "요청 값이 올바르지 않습니다."
        401 -> "인증이 필요합니다."
        403 -> "요청 권한이 없습니다."
        404 -> "요청한 리소스를 찾을 수 없습니다."
        409 -> "요청이 현재 상태와 충돌합니다."
        else -> "인증 요청에 실패했습니다."
    }
}
