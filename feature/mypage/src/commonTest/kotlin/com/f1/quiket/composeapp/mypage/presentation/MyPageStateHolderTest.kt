package com.f1.quiket.composeapp.mypage.presentation

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent as AuthEmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.login.EmailFormatErrorMessage
import com.f1.quiket.composeapp.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.mypage.domain.model.Feedback
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCategory
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCreate
import com.f1.quiket.composeapp.mypage.domain.model.MyPageData
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings
import com.f1.quiket.composeapp.mypage.domain.repository.MyPageRepository
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MyPageStateHolderTest {
    @Test
    fun notificationSettingsLoadPopulatesState() = runTest {
        val repository = FakeMyPageRepository()
        val stateHolder = NotificationSettingsStateHolder(repository.toUseCases())

        stateHolder.loadSettings(onSessionExpired = {})

        assertEquals(repository.notificationSettings, stateHolder.state.settings)
        assertFalse(stateHolder.state.isLoading)
        assertFalse(stateHolder.state.isSaving)
        assertNull(stateHolder.state.message)
    }

    @Test
    fun notificationSettingsFailureRollsBackOptimisticUpdate() = runTest {
        val repository = FakeMyPageRepository()
        val stateHolder = NotificationSettingsStateHolder(repository.toUseCases())
        stateHolder.loadSettings(onSessionExpired = {})
        val previous = repository.notificationSettings
        repository.updateFailure = IllegalStateException("서버 저장 실패")

        stateHolder.updateSettings(
            nextSettings = previous.copy(activityEnabled = !previous.activityEnabled),
            onSessionExpired = {},
        )

        assertEquals(previous, stateHolder.state.settings)
        assertFalse(stateHolder.state.isSaving)
        assertEquals("서버 저장 실패", stateHolder.state.message)
    }

    @Test
    fun inquiryRejectsBlankBodyAndInvalidReplyEmailWithoutRequest() = runTest {
        val repository = FakeMyPageRepository()
        val stateHolder = InquiryStateHolder(repository.toUseCases())

        stateHolder.submit(onSessionExpired = {})
        assertEquals("문의 내용을 입력해주세요.", stateHolder.state.message)

        stateHolder.updateBody("문의 내용")
        stateHolder.updateReplyEmail("not-an-email")
        stateHolder.submit(onSessionExpired = {})

        assertEquals(EmailFormatErrorMessage, stateHolder.state.message)
        assertNull(repository.createdFeedback)
    }

    @Test
    fun inquiryTrimsPayloadAndResetsFormAfterSuccess() = runTest {
        val repository = FakeMyPageRepository()
        val stateHolder = InquiryStateHolder(repository.toUseCases())
        stateHolder.selectCategory(FeedbackCategory.Bug)
        stateHolder.updateBody("  재현 가능한 오류입니다.  ")
        stateHolder.updateReplyEmail("  qa@quiket.co.kr  ")

        stateHolder.submit(onSessionExpired = {})

        assertEquals(
            FeedbackCreate(
                category = FeedbackCategory.Bug,
                body = "재현 가능한 오류입니다.",
                replyEmail = "qa@quiket.co.kr",
            ),
            repository.createdFeedback,
        )
        assertEquals("문의가 접수되었습니다.", stateHolder.state.message)
        assertEquals("", stateHolder.state.body)
        assertEquals("", stateHolder.state.replyEmail)
        assertEquals(FeedbackCategory.Inquiry, stateHolder.state.category)
        assertFalse(stateHolder.state.isSubmitting)
    }

    @Test
    fun inquiryBodyIsLimitedToServerMaximum() {
        val stateHolder = InquiryStateHolder(FakeMyPageRepository().toUseCases())

        stateHolder.updateBody("a".repeat(1001))

        assertEquals(1000, stateHolder.state.body.length)
    }
}

private class FakeMyPageRepository : MyPageRepository {
    var notificationSettings = NotificationSettings(
        fcmTokenRegistered = true,
        activityEnabled = true,
        updateEnabled = false,
        reviewEnabled = true,
    )
    var updateFailure: Throwable? = null
    var createdFeedback: FeedbackCreate? = null

    override suspend fun getNotificationSettings(session: SessionSnapshot): NotificationSettings =
        notificationSettings

    override suspend fun updateNotificationSettings(
        session: SessionSnapshot,
        settings: NotificationSettings,
    ): NotificationSettings {
        updateFailure?.let { throw it }
        notificationSettings = settings
        return settings
    }

    override suspend fun createFeedback(
        session: SessionSnapshot,
        feedback: FeedbackCreate,
    ): Feedback {
        createdFeedback = feedback
        return Feedback(
            id = "feedback-1",
            category = feedback.category,
            body = feedback.body,
            replyEmail = feedback.replyEmail,
            createdAt = "2026-07-14T00:00:00",
        )
    }

    override suspend fun getMyPage(session: SessionSnapshot): MyPageData = unsupported()
    override suspend fun getMyProfile(session: SessionSnapshot): MyProfile = unsupported()
    override suspend fun updateMyNickname(session: SessionSnapshot, nickname: String): MyProfile = unsupported()
    override suspend fun requestMyEmailChange(session: SessionSnapshot, newEmail: String): EmailVerificationSent = unsupported()
    override suspend fun confirmMyEmailChange(session: SessionSnapshot, newEmail: String, verificationCode: String): MyProfile = unsupported()
    override suspend fun updateMyPassword(session: SessionSnapshot, currentPassword: String, newPassword: String, newPasswordConfirm: String) = unsupported()
    override suspend fun deleteMyAccount(session: SessionSnapshot, password: String?) = unsupported()
    override suspend fun updateFcmToken(session: SessionSnapshot, fcmToken: String) = unsupported()

    fun toUseCases(): MyPageUseCases = MyPageUseCases(
        authenticatedCallRunner = AuthenticatedCallRunner(
            sessionRepository = FakeSessionRepository(),
            authRepository = FakeAuthRepository(),
        ),
        repository = this,
    )

    private fun unsupported(): Nothing = error("Not needed for this test")
}

private class FakeSessionRepository : SessionRepository {
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

private class FakeAuthRepository : AuthRepository {
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
    override suspend fun resendEmailVerification(email: String): AuthEmailVerificationSent = unsupported()
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

private fun testSession(): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = true,
    homeGuideCompleted = true,
    accessToken = "access-token",
    refreshToken = "refresh-token",
    tokenType = "Bearer",
    nickname = "테스터",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
)
