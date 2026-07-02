package com.f1.quiket.core.network.di

import android.util.Log
import com.f1.quiket.core.network.BuildConfig
import com.f1.quiket.core.network.auth.AuthTokenAuthenticator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okio.Buffer
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
interface NetworkInterceptorModule {
    @Multibinds
    fun bindInterceptors(): Set<Interceptor>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptors: Set<@JvmSuppressWildcards Interceptor>,
        authTokenAuthenticator: AuthTokenAuthenticator,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        interceptors.forEach(builder::addInterceptor)

        builder.authenticator(authTokenAuthenticator)
        builder.addInterceptor(provideLoggingInterceptor())

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.QUIKET_API_BASE_URL.ensureTrailingSlash())
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"

    private fun provideLoggingInterceptor(): Interceptor =
        SafeHttpLoggingInterceptor(enabled = BuildConfig.DEBUG)

    private class SafeHttpLoggingInterceptor(
        private val enabled: Boolean,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!enabled) return chain.proceed(chain.request())

            val request = chain.request()
            val exchangeId = NextExchangeId.incrementAndGet()
            val startedAt = System.nanoTime()

            logChunked(
                buildString {
                    appendLine()
                    appendLine("========== Quiket HTTP #$exchangeId ==========")
                    appendLine("[REQUEST] --> ${request.method} ${request.url}")
                    appendHeaders(request.headers)
                    request.body?.previewLogBody()?.let { body ->
                        appendLine("[REQUEST BODY]")
                        appendLine(body.prependIndent("  "))
                    }
                    append("==========================================")
                },
            )

            return try {
                val response = chain.proceed(request)
                val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
                logChunked(
                    buildString {
                        appendLine()
                        appendLine("========== Quiket HTTP #$exchangeId ==========")
                        appendLine("[RESPONSE] <-- ${response.code} ${response.message} (${tookMs}ms) ${response.request.url}")
                        appendHeaders(response.headers)
                        response.previewLogBody()?.let { body ->
                            appendLine("[RESPONSE BODY]")
                            appendLine(body.prependIndent("  "))
                        }
                        append("==========================================")
                    },
                )
                response
            } catch (throwable: Throwable) {
                logChunked(
                    buildString {
                        appendLine()
                        appendLine("========== Quiket HTTP #$exchangeId ==========")
                        appendLine("[FAILED] ${throwable.javaClass.simpleName}: ${throwable.message.orEmpty()}")
                        append("==========================================")
                    },
                )
                throw throwable
            }
        }

        private fun StringBuilder.appendHeaders(headers: Headers) {
            if (headers.size == 0) return

            appendLine("[HEADERS]")
            headers.forEach { (name, value) ->
                appendLine("  $name: ${value.redactHeaderValue(name)}")
            }
        }

        private fun String.redactHeaderValue(name: String): String =
            if (name.isSensitiveHeader()) "[REDACTED]" else redactSensitiveValues()

        private fun String.isSensitiveHeader(): Boolean =
            equals("Authorization", ignoreCase = true) ||
                equals("Cookie", ignoreCase = true) ||
                equals("Set-Cookie", ignoreCase = true) ||
                equals("X-Device-Id", ignoreCase = true) ||
                equals("X-Device-Name", ignoreCase = true)

        private fun RequestBody.previewLogBody(): String {
            val contentType = contentType()
            val contentLength = contentLengthOrNull()

            if (isDuplex() || isOneShot()) return "[body omitted: one-shot or duplex body]"
            if (!contentType.isTextLike()) return "[body omitted: ${contentType ?: "unknown content type"}]"
            if (contentLength != null && contentLength > MaxBodyBytes) {
                return "[body omitted: ${contentLength}B > ${MaxBodyBytes}B]"
            }

            return runCatching {
                val buffer = Buffer()
                writeTo(buffer)
                val body = buffer.readUtf8Capped()
                body.toPrettyBodyLog(truncated = buffer.size > MaxBodyBytes)
            }.getOrElse { throwable ->
                "[body omitted: ${throwable.javaClass.simpleName}]"
            }
        }

        private fun Response.previewLogBody(): String? {
            if (!hasInspectableBody()) return null

            val body = body ?: return null
            val contentType = body.contentType()
            if (!contentType.isTextLike()) return "[body omitted: ${contentType ?: "unknown content type"}]"

            val contentLength = body.contentLength()
            val preview = peekBody(MaxBodyBytes.toLong()).string()
            if (preview.isBlank()) return null

            return preview.toPrettyBodyLog(
                truncated = contentLength == -1L || contentLength > MaxBodyBytes,
            )
        }

        private fun Buffer.readUtf8Capped(): String {
            val bytesToRead = minOf(size, MaxBodyBytes.toLong())
            return readString(bytesToRead, Charsets.UTF_8)
        }

        private fun Response.hasInspectableBody(): Boolean =
            request.method != "HEAD" &&
                code !in 100..199 &&
                code != 204 &&
                code != 304

        private fun MediaType?.isTextLike(): Boolean {
            val type = this?.toString()?.lowercase().orEmpty()
            return type.contains("json") ||
                type.contains("text") ||
                type.contains("xml") ||
                type.contains("x-www-form-urlencoded")
        }

        private fun RequestBody.contentLengthOrNull(): Long? =
            runCatching { contentLength() }.getOrNull()?.takeIf { it >= 0 }

        private fun String.toPrettyBodyLog(truncated: Boolean): String {
            val redacted = redactSensitiveValues()
            val pretty = redacted.prettyJsonOrNull() ?: redacted
            return if (truncated) {
                "$pretty\n... [truncated at ${MaxBodyBytes}B]"
            } else {
                pretty
            }
        }

        private fun String.prettyJsonOrNull(): String? {
            val trimmed = trim()
            if (trimmed.length > MaxPrettyJsonLength) return null
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return null

            return runCatching {
                PrettyJson.encodeToString(
                    serializer = JsonElement.serializer(),
                    value = PrettyJson.parseToJsonElement(trimmed),
                )
            }.getOrNull()
        }

        private fun String.redactSensitiveValues(): String =
            replace(SensitiveJsonValueRegex) { result ->
                "${result.groupValues[1]}\"[REDACTED]\""
            }.replace(SensitiveFormValueRegex) { result ->
                "${result.groupValues[1]}[REDACTED]"
            }

        private fun logChunked(message: String) {
            val chunks = message.chunked(MaxLogMessageLength)
            chunks.forEachIndexed { index, chunk ->
                val text = if (chunks.size == 1) {
                    chunk
                } else {
                    "[part ${index + 1}/${chunks.size}]\n$chunk"
                }
                Log.d(NetworkLogTag, text)
            }
        }

        private companion object {
            private const val NetworkLogTag = "QuiketNetwork"
            private const val MaxLogMessageLength = 3_500
            private const val MaxBodyBytes = 16_384
            private const val MaxPrettyJsonLength = 16_384
            private val NextExchangeId = AtomicInteger(0)

            @OptIn(ExperimentalSerializationApi::class)
            private val PrettyJson = Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }
            private val SensitiveJsonValueRegex = Regex(
                pattern = """("(?i:password|passwordConfirm|newPassword|currentPassword|verificationCode|accessToken|refreshToken|idToken|oauthAccessToken|kakaoAccessToken|token)"\s*:\s*)"[^"]*"""",
            )
            private val SensitiveFormValueRegex = Regex(
                pattern = """((?i:password|passwordConfirm|newPassword|currentPassword|verificationCode|accessToken|refreshToken|idToken|oauthAccessToken|kakaoAccessToken|token)=)[^&\s]+""",
            )
        }
    }
}
