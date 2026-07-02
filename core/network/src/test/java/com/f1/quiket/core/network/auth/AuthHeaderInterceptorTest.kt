package com.f1.quiket.core.network.auth

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class AuthHeaderInterceptorTest {
    @Test
    fun intercept_whenMarkerIsPresent_addsAuthorizationAndRemovesMarker() {
        val tokenStore = FakeAuthTokenStore(
            tokenPair = TokenPair(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                tokenType = "Bearer",
                accessTokenExpiresIn = 3600,
                refreshTokenExpiresIn = 86400,
            ),
        )
        val interceptor = AuthHeaderInterceptor(tokenStore)
        val chain = RecordingChain(
            Request.Builder()
                .url("https://quiket.test/home")
                .header(AuthenticatedRequest.HEADER_NAME, AuthenticatedRequest.HEADER_VALUE)
                .build(),
        )

        interceptor.intercept(chain)

        assertThat(chain.proceededRequest.header("Authorization")).isEqualTo("Bearer access-token")
        assertThat(chain.proceededRequest.header(AuthenticatedRequest.HEADER_NAME)).isNull()
    }

    @Test
    fun intercept_whenTokenIsMissing_onlyRemovesMarker() {
        val interceptor = AuthHeaderInterceptor(FakeAuthTokenStore(tokenPair = null))
        val chain = RecordingChain(
            Request.Builder()
                .url("https://quiket.test/home")
                .header(AuthenticatedRequest.HEADER_NAME, AuthenticatedRequest.HEADER_VALUE)
                .build(),
        )

        interceptor.intercept(chain)

        assertThat(chain.proceededRequest.header("Authorization")).isNull()
        assertThat(chain.proceededRequest.header(AuthenticatedRequest.HEADER_NAME)).isNull()
    }

    @Test
    fun intercept_whenMarkerIsMissing_leavesRequestUntouched() {
        val interceptor = AuthHeaderInterceptor(FakeAuthTokenStore(tokenPair = null))
        val chain = RecordingChain(
            Request.Builder()
                .url("https://quiket.test/home")
                .header("X-Custom", "kept")
                .build(),
        )

        interceptor.intercept(chain)

        assertThat(chain.proceededRequest.header("X-Custom")).isEqualTo("kept")
    }

    @Test
    fun intercept_whenAuthenticatedRequestReturnsUnauthorized_clearsToken() {
        val tokenStore = FakeAuthTokenStore(tokenPair = tokenPair())
        val interceptor = AuthHeaderInterceptor(tokenStore)
        val chain = RecordingChain(
            initialRequest = Request.Builder()
                .url("https://quiket.test/home")
                .header(AuthenticatedRequest.HEADER_NAME, AuthenticatedRequest.HEADER_VALUE)
                .build(),
            responseCode = 401,
        )

        interceptor.intercept(chain)

        assertThat(tokenStore.tokenPair).isNull()
    }

    @Test
    fun intercept_whenUnauthenticatedRequestReturnsUnauthorized_keepsToken() {
        val tokenStore = FakeAuthTokenStore(tokenPair = tokenPair())
        val interceptor = AuthHeaderInterceptor(tokenStore)
        val chain = RecordingChain(
            initialRequest = Request.Builder()
                .url("https://quiket.test/auth/login")
                .build(),
            responseCode = 401,
        )

        interceptor.intercept(chain)

        assertThat(tokenStore.tokenPair).isEqualTo(tokenPair())
    }

    private fun tokenPair() = TokenPair(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 86400,
    )
}

private class FakeAuthTokenStore(
    tokenPair: TokenPair?,
) : AuthTokenStore {
    private val tokenPairs = MutableStateFlow(tokenPair)

    val tokenPair: TokenPair?
        get() = tokenPairs.value

    override fun observeTokenPair(): Flow<TokenPair?> = tokenPairs

    override suspend fun getTokenPair(): TokenPair? = tokenPairs.value

    override suspend fun saveTokenPair(tokenPair: TokenPair) {
        tokenPairs.value = tokenPair
    }

    override suspend fun clear() {
        tokenPairs.value = null
    }
}

private class RecordingChain(
    private val initialRequest: Request,
    private val responseCode: Int = 200,
) : Interceptor.Chain {
    lateinit var proceededRequest: Request
        private set

    override fun request(): Request = initialRequest

    override fun proceed(request: Request): Response {
        proceededRequest = request
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(responseCode)
            .message(if (responseCode == 401) "Unauthorized" else "OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    override fun connection(): Connection? = null

    override fun call(): Call {
        throw UnsupportedOperationException("Not needed for interceptor unit test.")
    }

    override fun connectTimeoutMillis(): Int = TIMEOUT_MILLIS

    override fun withConnectTimeout(
        timeout: Int,
        unit: TimeUnit,
    ): Interceptor.Chain = this

    override fun readTimeoutMillis(): Int = TIMEOUT_MILLIS

    override fun withReadTimeout(
        timeout: Int,
        unit: TimeUnit,
    ): Interceptor.Chain = this

    override fun writeTimeoutMillis(): Int = TIMEOUT_MILLIS

    override fun withWriteTimeout(
        timeout: Int,
        unit: TimeUnit,
    ): Interceptor.Chain = this

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
    }
}
