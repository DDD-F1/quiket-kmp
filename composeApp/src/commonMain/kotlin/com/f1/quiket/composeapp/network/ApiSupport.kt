package com.f1.quiket.composeapp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import io.ktor.serialization.kotlinx.json.json as ktorJson

internal object ApiConfig {
    const val ApiBaseUrl = "http://43.201.222.243:8080/api/v1/"
}

@Serializable
internal data class ApiEnvelope(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: JsonElement? = null,
)

internal sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>

    data class Failure(
        val error: ApiException,
    ) : ApiResult<Nothing>
}

internal class ApiException(
    message: String,
    val statusCode: Int? = null,
    val code: String? = null,
    val isUnauthorized: Boolean = false,
) : Exception(message)

internal val apiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

internal fun createHttpClient(json: Json = apiJson): HttpClient = HttpClient {
    expectSuccess = false
    install(ContentNegotiation) {
        ktorJson(json)
    }
}

internal fun Json.decodeApiEnvelope(
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

internal inline fun <reified T> Json.decodeApiData(
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

internal fun ApiEnvelope.requireSuccessful(
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

internal fun String.ensureTrailingSlash(): String =
    if (endsWith("/")) this else "$this/"

private const val HttpUnauthorized = 401
