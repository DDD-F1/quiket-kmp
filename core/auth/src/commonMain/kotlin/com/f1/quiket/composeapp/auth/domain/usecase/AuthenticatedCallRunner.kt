package com.f1.quiket.composeapp.auth.domain.usecase

import com.f1.quiket.composeapp.auth.domain.model.AuthException
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthenticatedCallRunner(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
) {
    private val refreshMutex = Mutex()

    suspend fun <T> run(
        isUnauthorized: (Throwable) -> Boolean = { error ->
            error is AuthException && error.isUnauthorized
        },
        request: suspend (SessionSnapshot) -> T,
    ): T {
        val initialSession = sessionRepository.read()

        return try {
            request(initialSession)
        } catch (firstError: Throwable) {
            if (firstError is CancellationException || !isUnauthorized(firstError)) {
                throw firstError
            }

            val storedRefreshToken = initialSession.refreshToken?.takeIf { it.isNotBlank() }
                ?: throw firstError

            val refreshedSession = refreshMutex.withLock {
                val latestSession = sessionRepository.read()
                if (
                    !latestSession.accessToken.isNullOrBlank() &&
                    latestSession.accessToken != initialSession.accessToken
                ) {
                    return@withLock latestSession
                }

                val refreshToken = latestSession.refreshToken?.takeIf { it.isNotBlank() } ?: storedRefreshToken
                val refreshedTokens = try {
                    authRepository.refreshToken(refreshToken)
                } catch (refreshError: Throwable) {
                    if (refreshError is CancellationException) {
                        throw refreshError
                    }
                    if (refreshError is AuthException && refreshError.isUnauthorized) {
                        sessionRepository.clearAuth()
                        throw firstError
                    }
                    throw refreshError
                }

                sessionRepository.saveAuth(refreshedTokens)
                sessionRepository.read()
            }

            retryWithSession(
                session = refreshedSession,
                isUnauthorized = isUnauthorized,
                request = request,
            )
        }
    }

    private suspend fun <T> retryWithSession(
        session: SessionSnapshot,
        isUnauthorized: (Throwable) -> Boolean,
        request: suspend (SessionSnapshot) -> T,
    ): T {
        return try {
            request(session)
        } catch (retryError: Throwable) {
            if (retryError is CancellationException) {
                throw retryError
            }
            if (isUnauthorized(retryError)) {
                sessionRepository.clearAuth()
            }
            throw retryError
        }
    }
}
