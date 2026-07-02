package com.f1.quiket.core.network.auth

import com.f1.quiket.core.network.BuildConfig
import com.f1.quiket.core.network.model.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class AuthTokenRefreshClientImpl @Inject constructor(
    private val json: Json,
    private val deviceInfoProvider: DeviceInfoProvider,
) : AuthTokenRefreshClient {
    private val client = OkHttpClient.Builder().build()

    override suspend fun refresh(refreshToken: String): AuthTokenRefreshResult =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${BuildConfig.QUIKET_API_BASE_URL.ensureTrailingSlash()}auth/token/refresh")
                .post(
                    json.encodeToString(RefreshTokenRequest(refreshToken = refreshToken))
                        .toRequestBody(JsonMediaType),
                )
                .header(DeviceIdHeader, deviceInfoProvider.deviceId)
                .header(DeviceNameHeader, deviceInfoProvider.deviceName)
                .build()

            runCatching {
                client.newCall(request).execute().use { response ->
                    when {
                        response.code == 401 -> AuthTokenRefreshResult.InvalidRefreshToken
                        !response.isSuccessful -> AuthTokenRefreshResult.Failure
                        else -> response.body
                            ?.string()
                            ?.takeIf(String::isNotBlank)
                            ?.let(::parseTokenResponse)
                            ?: AuthTokenRefreshResult.Failure
                    }
                }
            }.getOrDefault(AuthTokenRefreshResult.Failure)
        }

    private fun parseTokenResponse(body: String): AuthTokenRefreshResult =
        runCatching {
            val response = json.decodeFromString<ApiResponse<AuthTokenDataResponse>>(body)
            val data = response.data

            if (response.success && data != null) {
                AuthTokenRefreshResult.Success(data.toTokenPair())
            } else if (response.code.isInvalidRefreshTokenCode()) {
                AuthTokenRefreshResult.InvalidRefreshToken
            } else {
                AuthTokenRefreshResult.Failure
            }
        }.getOrDefault(AuthTokenRefreshResult.Failure)

    private fun String.isInvalidRefreshTokenCode(): Boolean =
        contains("REFRESH", ignoreCase = true) &&
            (contains("EXPIRED", ignoreCase = true) || contains("INVALID", ignoreCase = true))

    private fun AuthTokenDataResponse.toTokenPair(): TokenPair = TokenPair(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresIn = accessTokenExpiresIn,
        refreshTokenExpiresIn = refreshTokenExpiresIn,
    )

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"

    @Serializable
    private data class RefreshTokenRequest(
        val refreshToken: String,
    )

    @Serializable
    private data class AuthTokenDataResponse(
        val accessToken: String,
        val refreshToken: String,
        val tokenType: String,
        val accessTokenExpiresIn: Long,
        val refreshTokenExpiresIn: Long,
    )

    private companion object {
        val JsonMediaType = "application/json".toMediaType()
        const val DeviceIdHeader = "X-Device-Id"
        const val DeviceNameHeader = "X-Device-Name"
    }
}
