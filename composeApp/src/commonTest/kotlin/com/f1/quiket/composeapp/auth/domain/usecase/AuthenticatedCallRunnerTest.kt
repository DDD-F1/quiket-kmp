package com.f1.quiket.composeapp.auth.domain.usecase

import com.f1.quiket.composeapp.FakeAuthRepository
import com.f1.quiket.composeapp.FakeSessionRepository
import com.f1.quiket.composeapp.auth.AuthException
import com.f1.quiket.composeapp.testAuthToken
import com.f1.quiket.composeapp.testSession
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthenticatedCallRunnerTest {
    @Test
    fun refreshesSessionAndRetriesRequestAfterUnauthorizedError() = runTest {
        val sessionRepository = FakeSessionRepository(
            initialSession = testSession(accessToken = "old-access", refreshToken = "old-refresh"),
        )
        val authRepository = FakeAuthRepository().apply {
            refreshedTokenData = testAuthToken(accessToken = "new-access", refreshToken = "new-refresh")
        }
        val runner = AuthenticatedCallRunner(
            sessionRepository = sessionRepository,
            authRepository = authRepository,
        )
        var attempts = 0

        val result = runner.run { session ->
            attempts += 1
            if (attempts == 1) {
                assertEquals("old-access", session.accessToken)
                throw AuthException("expired", isUnauthorized = true)
            }

            assertEquals("new-access", session.accessToken)
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(2, attempts)
        assertEquals(1, authRepository.refreshTokenCalls)
        assertEquals(1, sessionRepository.saveAuthCalls)
        assertEquals("new-access", sessionRepository.session.accessToken)
        assertEquals("new-refresh", sessionRepository.session.refreshToken)
    }

    @Test
    fun clearsAuthWhenRefreshTokenIsUnauthorized() = runTest {
        val sessionRepository = FakeSessionRepository(
            initialSession = testSession(accessToken = "expired-access", refreshToken = "expired-refresh"),
        )
        val authRepository = FakeAuthRepository().apply {
            refreshError = AuthException("refresh expired", isUnauthorized = true)
        }
        val runner = AuthenticatedCallRunner(
            sessionRepository = sessionRepository,
            authRepository = authRepository,
        )

        assertFailsWith<AuthException> {
            runner.run<String> {
                throw AuthException("expired", isUnauthorized = true)
            }
        }

        assertEquals(1, authRepository.refreshTokenCalls)
        assertEquals(1, sessionRepository.clearAuthCalls)
        assertEquals(null, sessionRepository.session.accessToken)
        assertEquals(null, sessionRepository.session.refreshToken)
    }
}
