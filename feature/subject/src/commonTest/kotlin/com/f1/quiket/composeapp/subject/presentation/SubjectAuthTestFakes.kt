package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository

internal fun testSession(): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = true,
    homeGuideCompleted = false,
    accessToken = "access-token",
    refreshToken = "refresh-token",
    tokenType = "Bearer",
    nickname = "테스터",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
)

internal class FakeSessionRepository : SessionRepository {
    private var session = testSession()

    override suspend fun read(): SessionSnapshot = session
    override suspend fun saveOnboardingCompleted() = Unit
    override suspend fun saveHomeGuideCompleted() = Unit
    override suspend fun saveAuth(tokenData: AuthTokenData) {
        session = session.copy(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
            tokenType = tokenData.tokenType,
        )
    }

    override suspend fun clearAuth() {
        session = session.copy(accessToken = null, refreshToken = null, tokenType = null)
    }
}

internal class FakeAuthRepository : AuthRepository {
    override suspend fun refreshToken(refreshToken: String): AuthTokenData = AuthTokenData(
        accessToken = "new-access-token",
        refreshToken = "new-refresh-token",
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 7200,
        user = AuthUser(id = "user-id", email = null, nickname = "테스터"),
    )

    override suspend fun checkEmailAvailability(email: String): EmailAvailability = unsupported()
    override suspend fun signup(email: String, password: String, passwordConfirm: String, nickname: String): SignupData = unsupported()
    override suspend fun resendEmailVerification(email: String): EmailVerificationSent = unsupported()
    override suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData = unsupported()
    override suspend fun login(email: String, password: String): AuthTokenData = unsupported()
    override suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult = unsupported()
    override suspend fun appleLogin(identityToken: String, authorizationCode: String?, fullName: String?): AppleLoginResult = unsupported()
    override suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData = unsupported()
    override suspend fun linkKakaoAccount(linkToken: String, email: String, password: String): AuthTokenData = unsupported()
    override suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData = unsupported()
    override suspend fun linkAppleAccount(linkToken: String, email: String, password: String): AuthTokenData = unsupported()
    override suspend fun logout(session: SessionSnapshot) = Unit
    override suspend fun getMe(session: SessionSnapshot): AuthUser = unsupported()
    override suspend fun requestPasswordReset(email: String): PasswordResetRequested = unsupported()
    override suspend fun confirmPasswordReset(email: String, verificationCode: String, newPassword: String, newPasswordConfirm: String) = Unit

    private fun unsupported(): Nothing = error("Not needed for this test")
}
