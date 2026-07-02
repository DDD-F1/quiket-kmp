package com.f1.quiket.composeapp

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository

internal fun testSession(
    accessToken: String? = "access-token",
    refreshToken: String? = "refresh-token",
): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = true,
    homeGuideCompleted = false,
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = "Bearer",
    nickname = "테스터",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
)

internal fun testAuthToken(
    accessToken: String = "new-access-token",
    refreshToken: String = "new-refresh-token",
): AuthTokenData = AuthTokenData(
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = "Bearer",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
    user = AuthUser(
        id = "user-id",
        email = "user@example.com",
        nickname = "테스터",
    ),
)

internal class FakeSessionRepository(
    initialSession: SessionSnapshot = testSession(),
) : SessionRepository {
    var session: SessionSnapshot = initialSession
        private set

    var clearAuthCalls: Int = 0
        private set

    var saveAuthCalls: Int = 0
        private set

    override suspend fun read(): SessionSnapshot = session

    override suspend fun saveOnboardingCompleted() {
        session = session.copy(onboardingCompleted = true)
    }

    override suspend fun saveHomeGuideCompleted() {
        session = session.copy(homeGuideCompleted = true)
    }

    override suspend fun saveAuth(tokenData: AuthTokenData) {
        saveAuthCalls += 1
        session = session.copy(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
            tokenType = tokenData.tokenType,
            nickname = tokenData.user?.nickname ?: session.nickname,
            accessTokenExpiresIn = tokenData.accessTokenExpiresIn,
            refreshTokenExpiresIn = tokenData.refreshTokenExpiresIn,
        )
    }

    override suspend fun clearAuth() {
        clearAuthCalls += 1
        session = session.copy(
            accessToken = null,
            refreshToken = null,
            tokenType = null,
            nickname = null,
            accessTokenExpiresIn = 0,
            refreshTokenExpiresIn = 0,
        )
    }
}

internal class FakeAuthRepository : AuthRepository {
    var refreshedTokenData: AuthTokenData = testAuthToken()
    var refreshError: Throwable? = null
    var refreshTokenCalls: Int = 0
        private set

    override suspend fun refreshToken(refreshToken: String): AuthTokenData {
        refreshTokenCalls += 1
        refreshError?.let { throw it }
        return refreshedTokenData
    }

    override suspend fun checkEmailAvailability(email: String): EmailAvailability = unsupported()
    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData = unsupported()

    override suspend fun resendEmailVerification(email: String): EmailVerificationSent = unsupported()
    override suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData = unsupported()
    override suspend fun login(email: String, password: String): AuthTokenData = unsupported()
    override suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult = unsupported()
    override suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData = unsupported()
    override suspend fun linkKakaoAccount(linkToken: String, email: String, password: String): AuthTokenData = unsupported()
    override suspend fun logout(session: SessionSnapshot) = Unit
    override suspend fun getMe(session: SessionSnapshot): AuthUser = unsupported()
    override suspend fun requestPasswordReset(email: String): PasswordResetRequested = unsupported()
    override suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) = Unit

    private fun unsupported(): Nothing = error("Not needed for this test")
}
