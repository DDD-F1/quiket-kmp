package com.f1.quiket.core.network.retrofit

import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class ApiResponseHandler @Inject constructor(
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun <Dto : Any, Domain> execute(
        call: suspend () -> Response<ApiResponse<Dto>>,
        mapper: (Dto) -> Domain,
    ): NetworkResult<Domain> {
        return try {
            val response = call()
            if (!response.isSuccessful) {
                return errorMapper.map(response)
            }

            val body = response.body()
                ?: return emptyBodyFailure(response.code())

            if (!body.success) {
                return errorMapper.mapEnvelopeFailure(
                    httpCode = response.code(),
                    code = body.code,
                    message = body.message,
                )
            }

            val data = body.data
                ?: return emptyDataFailure(response.code(), body.code, body.message)
            NetworkResult.Success(mapper(data))
        } catch (throwable: Throwable) {
            throwable.throwIfCancellation()
            errorMapper.map(throwable)
        }
    }

    suspend fun executeEmpty(
        call: suspend () -> Response<ApiResponse<Unit>>,
    ): NetworkResult<Unit> {
        return try {
            val response = call()
            if (!response.isSuccessful) {
                return errorMapper.map(response)
            }

            val body = response.body()
                ?: return emptyBodyFailure(response.code())

            if (!body.success) {
                return errorMapper.mapEnvelopeFailure(
                    httpCode = response.code(),
                    code = body.code,
                    message = body.message,
                )
            }

            NetworkResult.Success(Unit)
        } catch (throwable: Throwable) {
            throwable.throwIfCancellation()
            errorMapper.map(throwable)
        }
    }

    private fun emptyBodyFailure(httpCode: Int): NetworkResult.Failure = NetworkResult.Failure(
        code = "COMMON_EMPTY_RESPONSE",
        message = "서버 응답 본문이 비어 있습니다.",
        httpCode = httpCode,
    )

    private fun emptyDataFailure(
        httpCode: Int,
        code: String,
        message: String,
    ): NetworkResult.Failure = NetworkResult.Failure(
        code = code.ifBlank { "COMMON_EMPTY_DATA" },
        message = message.ifBlank { "서버 응답 데이터가 비어 있습니다." },
        httpCode = httpCode,
    )

    private fun Throwable.throwIfCancellation() {
        if (this is CancellationException) {
            throw this
        }
    }
}
