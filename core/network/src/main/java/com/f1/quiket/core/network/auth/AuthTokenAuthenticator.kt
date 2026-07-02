package com.f1.quiket.core.network.auth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class AuthTokenAuthenticator @Inject constructor(
    private val tokenStore: AuthTokenStore,
    private val tokenRefreshClient: AuthTokenRefreshClient,
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        val failedAuthorization = response.request.header(AuthorizationHeader) ?: return null

        if (response.responseCount >= MaxAuthAttempts || response.request.isAuthEndpoint()) {
            return null
        }

        return synchronized(refreshLock) {
            val latestTokenPair = runBlocking { tokenStore.getTokenPair() } ?: return@synchronized null
            val latestAuthorization = latestTokenPair.toAuthorizationHeader()

            if (latestAuthorization != failedAuthorization) {
                return@synchronized response.request.withAuthorization(latestAuthorization)
            }

            when (val result = runBlocking { tokenRefreshClient.refresh(latestTokenPair.refreshToken) }) {
                is AuthTokenRefreshResult.Success -> {
                    runBlocking { tokenStore.saveTokenPair(result.tokenPair) }
                    response.request.withAuthorization(result.tokenPair.toAuthorizationHeader())
                }

                AuthTokenRefreshResult.InvalidRefreshToken -> {
                    runBlocking { tokenStore.clear() }
                    null
                }

                AuthTokenRefreshResult.Failure -> null
            }
        }
    }

    private val Response.responseCount: Int
        get() {
            var result = 1
            var priorResponse = priorResponse
            while (priorResponse != null) {
                result++
                priorResponse = priorResponse.priorResponse
            }
            return result
        }

    private fun Request.withAuthorization(authorization: String): Request =
        newBuilder()
            .header(AuthorizationHeader, authorization)
            .build()

    private fun Request.isAuthEndpoint(): Boolean {
        val path = url.encodedPath
        return path.endsWith("/auth/token/refresh") ||
            path.endsWith("/auth/logout")
    }

    private companion object {
        const val AuthorizationHeader = "Authorization"
        const val MaxAuthAttempts = 2
    }
}
