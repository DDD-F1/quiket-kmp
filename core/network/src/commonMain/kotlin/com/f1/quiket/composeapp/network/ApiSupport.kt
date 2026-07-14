package com.f1.quiket.composeapp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import io.ktor.serialization.kotlinx.json.json as ktorJson

object ApiConfig {
    const val ApiBaseUrl = "http://43.201.222.243:8080/api/v1/"
}

@Serializable
data class ApiEnvelope(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: JsonElement? = null,
)

sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>

    data class Failure(
        val error: ApiException,
    ) : ApiResult<Nothing>
}

class ApiException(
    message: String,
    val statusCode: Int? = null,
    val code: String? = null,
    val isUnauthorized: Boolean = false,
) : Exception(message)

val apiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun createHttpClient(json: Json = apiJson): HttpClient = HttpClient {
    expectSuccess = false
    install(ContentNegotiation) {
        ktorJson(json)
    }
}

fun Json.decodeApiEnvelope(
    body: String,
    failureMessage: String = "서버 응답을 해석하지 못했습니다.",
): ApiEnvelope =
    runCatching {
        decodeFromString<ApiEnvelope>(body)
    }.getOrElse { error ->
        if (error is SerializationException) {
            throw ApiException(failureMessage)
        }
        throw error
    }

inline fun <reified T> Json.decodeApiData(
    envelope: ApiEnvelope,
    missingMessage: String,
    failureMessage: String = "서버 응답을 해석하지 못했습니다.",
): T {
    val data = envelope.data ?: throw ApiException(
        message = missingMessage,
        code = envelope.code,
    )

    return runCatching {
        decodeFromJsonElement<T>(data)
    }.getOrElse { error ->
        if (error is SerializationException) {
            throw ApiException(
                message = failureMessage,
                code = envelope.code,
            )
        }
        throw error
    }
}

fun ApiEnvelope.requireSuccessful(
    statusCode: Int,
    failureMessage: String,
) {
    if (statusCode == HttpUnauthorized) {
        throw ApiException(
            message = message.ifBlank { "로그인이 만료되었습니다." },
            statusCode = statusCode,
            code = code,
            isUnauthorized = true,
        )
    }
    if (statusCode !in 200..299 || !success) {
        throw ApiException(
            message = message.ifBlank { failureMessage },
            statusCode = statusCode,
            code = code,
        )
    }
}

suspend fun HttpResponse.decodeApiEnvelope(
    json: Json,
    failureMessage: String = "서버 응답을 해석하지 못했습니다.",
): ApiEnvelope =
    json.decodeApiEnvelope(
        body = bodyAsText(),
        failureMessage = failureMessage,
    )

suspend fun HttpResponse.requireApiSuccess(
    json: Json,
    failureMessage: String,
    envelopeFailureMessage: String = "서버 응답을 해석하지 못했습니다.",
): ApiEnvelope {
    val envelope = decodeApiEnvelope(
        json = json,
        failureMessage = envelopeFailureMessage,
    )
    envelope.requireSuccessful(
        statusCode = status.value,
        failureMessage = failureMessage,
    )
    return envelope
}

suspend inline fun <reified T> HttpResponse.decodeApiData(
    json: Json,
    missingMessage: String,
    failureMessage: String,
    envelopeFailureMessage: String = "서버 응답을 해석하지 못했습니다.",
    dataFailureMessage: String = "서버 응답을 해석하지 못했습니다.",
): T {
    val envelope = requireApiSuccess(
        json = json,
        failureMessage = failureMessage,
        envelopeFailureMessage = envelopeFailureMessage,
    )
    return json.decodeApiData(
        envelope = envelope,
        missingMessage = missingMessage,
        failureMessage = dataFailureMessage,
    )
}

fun String.ensureTrailingSlash(): String =
    if (endsWith("/")) this else "$this/"

private const val HttpUnauthorized = 401
