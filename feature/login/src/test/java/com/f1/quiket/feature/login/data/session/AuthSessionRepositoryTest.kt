package com.f1.quiket.feature.login.data.session

import com.f1.quiket.core.network.auth.AuthTokenStore
import com.f1.quiket.core.network.auth.TokenPair
import com.f1.quiket.core.session.UserSessionStatus
import com.f1.quiket.feature.login.domain.model.AuthProvider
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.model.AuthTokenData
import com.f1.quiket.feature.login.domain.model.AuthUser
import com.f1.quiket.feature.login.domain.model.EmailAvailability
import com.f1.quiket.feature.login.domain.model.EmailVerificationSent
import com.f1.quiket.feature.login.domain.model.KakaoLoginResult
import com.f1.quiket.feature.login.domain.model.PasswordResetRequested
import com.f1.quiket.feature.login.domain.model.SignupData
import com.f1.quiket.feature.login.domain.model.UserStatus
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AuthSessionRepositoryTest {
    @Test
    fun observeSessionStatus_mapsTokenPresence() = runTest {
        val tokenStore = FakeAuthTokenStore()
        val repository = AuthSessionRepository(
            authTokenStore = tokenStore,
            authRepository = FakeAuthRepository(),
        )
        val statuses = mutableListOf<UserSessionStatus>()

        tokenStore.saveTokenPair(tokenPair())
        repository.observeSessionStatus().take(1).toList(statuses)

        assertThat(statuses).containsExactly(UserSessionStatus.SignedIn)
    }

    @Test
    fun hasValidSession_whenTokenIsMissing_returnsFalseWithoutCallingMe() = runTest {
        val authRepository = FakeAuthRepository()
        val repository = AuthSessionRepository(
            authTokenStore = FakeAuthTokenStore(),
            authRepository = authRepository,
        )

        val result = repository.hasValidSession()

        assertThat(result).isFalse()
        assertThat(authRepository.getMeCallCount).isEqualTo(0)
    }

    @Test
    fun hasValidSession_whenMeSucceeds_returnsTrue() = runTest {
        val tokenStore = FakeAuthTokenStore(tokenPair())
        val authRepository = FakeAuthRepository(
            getMeResult = AuthResult.Success(authUser()),
        )
        val repository = AuthSessionRepository(
            authTokenStore = tokenStore,
            authRepository = authRepository,
        )

        val result = repository.hasValidSession()

        assertThat(result).isTrue()
        assertThat(authRepository.getMeCallCount).isEqualTo(1)
        assertThat(tokenStore.getTokenPair()).isNotNull()
    }

    @Test
    fun hasValidSession_whenMeFailsUnauthorized_clearsTokenAndReturnsFalse() = runTest {
        val tokenStore = FakeAuthTokenStore(tokenPair())
        val authRepository = FakeAuthRepository(
            getMeResult = AuthResult.Failure(
                code = "COMMON_UNAUTHORIZED",
                message = "인증이 필요합니다.",
                httpCode = 401,
            ),
        )
        val repository = AuthSessionRepository(
            authTokenStore = tokenStore,
            authRepository = authRepository,
        )

        val result = repository.hasValidSession()

        assertThat(result).isFalse()
        assertThat(authRepository.getMeCallCount).isEqualTo(1)
        assertThat(tokenStore.getTokenPair()).isNull()
    }

    @Test
    fun hasValidSession_whenMeFailsWithTransientError_keepsTokenAndReturnsFalse() = runTest {
        val tokenStore = FakeAuthTokenStore(tokenPair())
        val authRepository = FakeAuthRepository(
            getMeResult = AuthResult.Failure(
                code = "NETWORK_ERROR",
                message = "네트워크 오류",
            ),
        )
        val repository = AuthSessionRepository(
            authTokenStore = tokenStore,
            authRepository = authRepository,
        )

        val result = repository.hasValidSession()

        assertThat(result).isFalse()
        assertThat(authRepository.getMeCallCount).isEqualTo(1)
        assertThat(tokenStore.getTokenPair()).isNotNull()
    }

    @Test
    fun logout_whenRepositoryFails_clearsToken() = runTest {
        val tokenStore = FakeAuthTokenStore(tokenPair())
        val authRepository = FakeAuthRepository(
            logoutResult = AuthResult.Failure(
                code = "NETWORK_ERROR",
                message = "네트워크 오류",
            ),
        )
        val repository = AuthSessionRepository(
            authTokenStore = tokenStore,
            authRepository = authRepository,
        )

        repository.logout()

        assertThat(authRepository.logoutCallCount).isEqualTo(1)
        assertThat(tokenStore.getTokenPair()).isNull()
    }

    private class FakeAuthTokenStore(
        initialTokenPair: TokenPair? = null,
    ) : AuthTokenStore {
        private val tokenPair = MutableStateFlow(initialTokenPair)

        override fun observeTokenPair(): Flow<TokenPair?> = tokenPair

        override suspend fun getTokenPair(): TokenPair? = tokenPair.value

        override suspend fun saveTokenPair(tokenPair: TokenPair) {
            this.tokenPair.value = tokenPair
        }

        override suspend fun clear() {
            tokenPair.value = null
        }
    }

    private class FakeAuthRepository(
        private val getMeResult: AuthResult<AuthUser> = AuthResult.Failure(
            code = "UNHANDLED",
            message = "Unhandled",
        ),
        private val logoutResult: AuthResult<Unit> = AuthResult.Success(Unit),
    ) : AuthRepository {
        var getMeCallCount = 0
            private set
        var logoutCallCount = 0
            private set

        override suspend fun getMe(): AuthResult<AuthUser> {
            getMeCallCount += 1
            return getMeResult
        }

        override suspend fun logout(refreshToken: String?): AuthResult<Unit> {
            logoutCallCount += 1
            return logoutResult
        }

        override suspend fun signup(
            email: String,
            password: String,
            passwordConfirm: String,
            nickname: String,
        ): AuthResult<SignupData> = unhandled()

        override suspend fun checkEmailAvailability(email: String): AuthResult<EmailAvailability> = unhandled()

        override suspend fun resendEmailVerification(email: String): AuthResult<EmailVerificationSent> = unhandled()

        override suspend fun confirmEmailVerification(
            email: String,
            verificationCode: String?,
            verificationToken: String?,
            deviceId: String?,
            deviceName: String?,
        ): AuthResult<AuthTokenData> = unhandled()

        override suspend fun login(
            email: String,
            password: String,
            deviceId: String?,
            deviceName: String?,
        ): AuthResult<AuthTokenData> = unhandled()

        override suspend fun refreshToken(
            refreshToken: String,
            deviceId: String?,
            deviceName: String?,
        ): AuthResult<AuthTokenData> = unhandled()

        override suspend fun requestPasswordReset(email: String): AuthResult<PasswordResetRequested> = unhandled()

        override suspend fun confirmPasswordReset(
            email: String,
            newPassword: String,
            newPasswordConfirm: String,
            resetToken: String?,
            verificationCode: String?,
        ): AuthResult<Unit> = unhandled()

        override suspend fun kakaoLogin(
            kakaoAccessToken: String,
            agreedToTerms: Boolean,
            deviceId: String?,
            deviceName: String?,
        ): KakaoLoginResult = KakaoLoginResult.Failure(unhandledFailure())

        override suspend fun linkKakaoAccount(
            linkToken: String,
            email: String,
            password: String,
            agreedToLink: Boolean,
            deviceId: String?,
            deviceName: String?,
        ): AuthResult<AuthTokenData> = unhandled()

        override suspend fun completeKakaoNickname(
            signupToken: String,
            nickname: String,
            deviceId: String?,
            deviceName: String?,
        ): AuthResult<AuthTokenData> = unhandled()

        private fun <T> unhandled(): AuthResult<T> = unhandledFailure()

        private fun unhandledFailure() = AuthResult.Failure(
            code = "UNHANDLED",
            message = "Unhandled",
        )
    }

    private fun tokenPair() = TokenPair(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        tokenType = "Bearer",
        accessTokenExpiresIn = 1_800,
        refreshTokenExpiresIn = 2_592_000,
    )

    private fun authUser() = AuthUser(
        id = "user-1",
        email = "user@example.com",
        nickname = "재훈",
        dotoriBalance = 0,
        emailVerified = true,
        status = UserStatus.Active,
        providers = listOf(AuthProvider.Local),
    )
}
