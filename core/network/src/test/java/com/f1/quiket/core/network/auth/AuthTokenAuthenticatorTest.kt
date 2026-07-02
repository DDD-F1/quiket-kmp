package com.f1.quiket.core.network.auth

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class AuthTokenAuthenticatorTest {
    @Test
    fun authenticate_whenAccessTokenExpired_refreshesTokenAndRetriesRequest() {
        val tokenStore = FakeAuthenticatorTokenStore(oldTokenPair())
        val refreshClient = FakeAuthTokenRefreshClient(
            result = AuthTokenRefreshResult.Success(newTokenPair()),
        )
        val authenticator = AuthTokenAuthenticator(tokenStore, refreshClient)

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse(authorization = "Bearer old-access"),
        )

        assertThat(refreshClient.refreshTokenRequests).containsExactly("old-refresh")
        assertThat(tokenStore.tokenPair).isEqualTo(newTokenPair())
        assertThat(retryRequest?.header("Authorization")).isEqualTo("Bearer new-access")
    }

    @Test
    fun authenticate_whenTokenWasAlreadyRefreshed_retriesWithStoredTokenWithoutRefreshingAgain() {
        val tokenStore = FakeAuthenticatorTokenStore(newTokenPair())
        val refreshClient = FakeAuthTokenRefreshClient(
            result = AuthTokenRefreshResult.Success(
                newTokenPair(accessToken = "unused-access"),
            ),
        )
        val authenticator = AuthTokenAuthenticator(tokenStore, refreshClient)

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse(authorization = "Bearer old-access"),
        )

        assertThat(refreshClient.refreshTokenRequests).isEmpty()
        assertThat(retryRequest?.header("Authorization")).isEqualTo("Bearer new-access")
    }

    @Test
    fun authenticate_whenRefreshTokenIsInvalid_clearsTokenAndStopsRetry() {
        val tokenStore = FakeAuthenticatorTokenStore(oldTokenPair())
        val refreshClient = FakeAuthTokenRefreshClient(
            result = AuthTokenRefreshResult.InvalidRefreshToken,
        )
        val authenticator = AuthTokenAuthenticator(tokenStore, refreshClient)

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse(authorization = "Bearer old-access"),
        )

        assertThat(retryRequest).isNull()
        assertThat(tokenStore.tokenPair).isNull()
    }

    @Test
    fun authenticate_whenRequestHasNoAuthorization_doesNotRefresh() {
        val tokenStore = FakeAuthenticatorTokenStore(oldTokenPair())
        val refreshClient = FakeAuthTokenRefreshClient(
            result = AuthTokenRefreshResult.Success(newTokenPair()),
        )
        val authenticator = AuthTokenAuthenticator(tokenStore, refreshClient)

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse(authorization = null),
        )

        assertThat(retryRequest).isNull()
        assertThat(refreshClient.refreshTokenRequests).isEmpty()
        assertThat(tokenStore.tokenPair).isEqualTo(oldTokenPair())
    }

    @Test
    fun authenticate_whenRequestIsLogout_doesNotRefresh() {
        val tokenStore = FakeAuthenticatorTokenStore(oldTokenPair())
        val refreshClient = FakeAuthTokenRefreshClient(
            result = AuthTokenRefreshResult.Success(newTokenPair()),
        )
        val authenticator = AuthTokenAuthenticator(tokenStore, refreshClient)

        val retryRequest = authenticator.authenticate(
            route = null,
            response = unauthorizedResponse(
                authorization = "Bearer old-access",
                url = "https://quiket.test/api/v1/auth/logout",
            ),
        )

        assertThat(retryRequest).isNull()
        assertThat(refreshClient.refreshTokenRequests).isEmpty()
    }

    private fun unauthorizedResponse(
        authorization: String?,
        url: String = "https://quiket.test/api/v1/home",
    ): Response {
        val requestBuilder = Request.Builder().url(url)
        if (authorization != null) {
            requestBuilder.header("Authorization", authorization)
        }
        val request = requestBuilder.build()

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("{}".toResponseBody())
            .build()
    }

    private fun oldTokenPair() = TokenPair(
        accessToken = "old-access",
        refreshToken = "old-refresh",
        tokenType = "Bearer",
        accessTokenExpiresIn = 1_800,
        refreshTokenExpiresIn = 2_592_000,
    )

    private fun newTokenPair(
        accessToken: String = "new-access",
    ) = TokenPair(
        accessToken = accessToken,
        refreshToken = "new-refresh",
        tokenType = "Bearer",
        accessTokenExpiresIn = 1_800,
        refreshTokenExpiresIn = 2_592_000,
    )
}

private class FakeAuthenticatorTokenStore(
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

private class FakeAuthTokenRefreshClient(
    private val result: AuthTokenRefreshResult,
) : AuthTokenRefreshClient {
    val refreshTokenRequests = mutableListOf<String>()

    override suspend fun refresh(refreshToken: String): AuthTokenRefreshResult {
        refreshTokenRequests += refreshToken
        return result
    }
}
