package com.f1.quiket.composeapp.result.presentation

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.quiz.domain.model.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationAccepted
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationProgress
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayException
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlaySession
import com.f1.quiket.composeapp.quiz.domain.model.QuizQuestion
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.domain.model.QuizSession
import com.f1.quiket.composeapp.quiz.domain.model.QuizSubmitResult
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType.*
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.result.domain.model.QuizResult
import com.f1.quiket.composeapp.result.domain.model.QuizResultException
import com.f1.quiket.composeapp.result.domain.model.RewardSummary
import com.f1.quiket.composeapp.result.domain.model.RetryAvailable
import com.f1.quiket.composeapp.result.domain.repository.QuizResultRepository
import com.f1.quiket.composeapp.result.domain.usecase.QuizResultUseCases
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizResultStateHolderTest {
    @Test
    fun loadResultMovesToSuccessState() = runTest {
        val repository = FakeQuizResultRepository(
            getQuizResultAction = { _, _ -> quizResult() },
        )
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
            quizPlayUseCases = quizPlayUseCases(),
        )

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})

        val state = assertIs<QuizResultUiState.Success>(stateHolder.state)
        assertEquals("result-id", state.result.resultId)
        assertEquals(1, repository.getQuizResultCalls)
    }

    @Test
    fun loadResultUnauthorizedInvokesSessionExpired() = runTest {
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ -> throw QuizResultException("expired", isUnauthorized = true) },
                ),
            ),
            quizPlayUseCases = quizPlayUseCases(),
        )
        var sessionExpiredCount = 0

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = { sessionExpiredCount += 1 })

        assertEquals(1, sessionExpiredCount)
        assertIs<QuizResultUiState.Loading>(stateHolder.state)
    }

    @Test
    fun loadResultShowsErrorStateOnFailure() = runTest {
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ -> throw IllegalStateException("network fail") },
                ),
            ),
            quizPlayUseCases = quizPlayUseCases(),
        )

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})

        val state = assertIs<QuizResultUiState.Error>(stateHolder.state)
        assertTrue(state.message.isNotBlank())
    }

    @Test
    fun retryUsesPlaySessionIdWhenResultIdIsMissing() = runTest {
        val fakeQuizPlayRepository = FakeQuizPlayRepository(
            retryAllQuestionsAction = { _, _, _ ->
                QuizPlaySession(
                    playSessionId = "retry-play-session",
                    clientSessionId = "retry-client",
                    quizSessionId = "retry-session",
                    playType = RetryAll,
                    status = "ready",
                    quizSession = quizSession(
                        id = "retry-session",
                        playMode = QuizPlayMode.OneByOne,
                        timerEnabled = true,
                        timerScope = QuizTimerScope.PerQuestion,
                        timerSeconds = 30,
                    ),
                )
            },
        )
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ ->
                        quizResult(
                            resultId = null,
                            playSessionId = "legacy-play-session",
                            wrongCount = 0,
                        )
                    },
                ),
            ),
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = fakeQuizPlayRepository,
            ),
        )

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})
        val launchConfig = stateHolder.retry(wrongOnly = false, onSessionExpired = {})

        assertNotNull(launchConfig)
        assertEquals("retry-session", launchConfig.quizSessionId)
        assertEquals("retry-client", launchConfig.clientSessionId)
        assertEquals("retry-play-session", launchConfig.playSessionId)
        assertEquals(QuizPlayMode.OneByOne, launchConfig.playMode)
        assertEquals(QuizTimerScope.PerQuestion, launchConfig.timerScope)
        assertEquals(30, launchConfig.timerSeconds)
        assertEquals(1, fakeQuizPlayRepository.retryAllQuestionsCalls)
    }

    @Test
    fun retryWrongOnlyRefusesWhenNoWrongAnswers() = runTest {
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ ->
                        quizResult(
                            totalCount = 3,
                            wrongCount = 0,
                        )
                    },
                ),
            ),
            quizPlayUseCases = quizPlayUseCases(),
        )

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})
        val launchConfig = stateHolder.retry(wrongOnly = true, onSessionExpired = {})

        assertNull(launchConfig)
        assertEquals("다시 풀 오답이 없어요.", stateHolder.retryMessage)
    }

    @Test
    fun retryAllRefusesWhenTotalIsZero() = runTest {
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ -> quizResult(totalCount = 0, wrongCount = 1) },
                ),
            ),
            quizPlayUseCases = quizPlayUseCases(),
        )

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})
        val launchConfig = stateHolder.retry(wrongOnly = false, onSessionExpired = {})

        assertNull(launchConfig)
        assertEquals("전체 다시 풀기를 시작할 수 없어요.", stateHolder.retryMessage)
    }

    @Test
    fun retryUnauthorizedErrorTriggersSessionExpired() = runTest {
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ -> quizResult() },
                ),
            ),
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizPlayRepository(
                    retryWrongQuestionsAction = { _, _, _ -> throw QuizPlayException("expired", isUnauthorized = true) },
                ),
            ),
        )
        var sessionExpiredCount = 0

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})
        val launchConfig = stateHolder.retry(wrongOnly = true, onSessionExpired = { sessionExpiredCount += 1 })

        assertNull(launchConfig)
        assertEquals(1, sessionExpiredCount)
        assertFalse(stateHolder.isRetrying)
    }

    @Test
    fun retryFailureSetsErrorMessageAndStopsRetrying() = runTest {
        val stateHolder = QuizResultStateHolder(
            quizResultUseCases = QuizResultUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizResultRepository(
                    getQuizResultAction = { _, _ -> quizResult() },
                ),
            ),
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = FakeQuizPlayRepository(
                    retryAllQuestionsAction = { _, _, _ -> throw IllegalStateException("retry failed") },
                ),
            ),
        )

        stateHolder.loadResult(resultId = "result-1", onSessionExpired = {})
        val launchConfig = stateHolder.retry(wrongOnly = false, onSessionExpired = {})

        assertNull(launchConfig)
        assertTrue(stateHolder.retryMessage.orEmpty().isNotBlank())
        assertFalse(stateHolder.isRetrying)
    }
}

private fun quizPlayUseCases(): QuizPlayUseCases = QuizPlayUseCases(
    authenticatedCallRunner = testAuthenticatedCallRunner(),
    repository = FakeQuizPlayRepository(),
)

private fun testAuthenticatedCallRunner(): AuthenticatedCallRunner = AuthenticatedCallRunner(
    sessionRepository = TestSessionRepository(testSession()),
    authRepository = TestAuthRepository(),
)

private fun quizResult(
    resultId: String? = "result-id",
    playSessionId: String = "play-session-id",
    totalCount: Int = 3,
    wrongCount: Int = 1,
): QuizResult = QuizResult(
    playSessionId = playSessionId,
    resultId = resultId,
    quizSessionId = "session-id",
    subjectId = "subject-id",
    subjectName = "SQLD",
    totalCount = totalCount,
    correctCount = totalCount - wrongCount,
    wrongCount = wrongCount,
    skipCount = 0,
    accuracyPct = 0,
    elapsedMs = 1_200_000,
    rewards = RewardSummary(
        dotoriEarned = 10,
        xpEarned = 20,
        leveledUp = false,
        newLevel = null,
        currentDotoriBalance = 0,
        currentXpTotal = 0,
    ),
    reviewItems = emptyList(),
    retryAvailable = RetryAvailable(
        retryAll = true,
        retryWrong = true,
        wrongCount = wrongCount,
    ),
    createdAt = null,
)

private class FakeQuizResultRepository(
    var getQuizResultAction: suspend (SessionSnapshot, String) -> QuizResult,
) : QuizResultRepository {
    var getQuizResultCalls = 0
    override suspend fun getQuizResult(session: SessionSnapshot, resultId: String): QuizResult {
        getQuizResultCalls += 1
        return getQuizResultAction(session, resultId)
    }
}

private class FakeQuizPlayRepository(
    var getQuizSessionAction: suspend (SessionSnapshot, String) -> QuizSession = { _, _ ->
        quizSession(
            id = "session-id",
            playMode = QuizPlayMode.AllAtOnce,
            timerEnabled = false,
            timerScope = null,
            timerSeconds = null,
            questions = emptyList(),
        )
    },
    var retryAllQuestionsAction: suspend (
        SessionSnapshot,
        String,
        String,
    ) -> QuizPlaySession = { _, _, _ ->
        error("Not used")
    },
    var retryWrongQuestionsAction: suspend (
        SessionSnapshot,
        String,
        String,
    ) -> QuizPlaySession = { _, _, _ ->
        error("Not used")
    },
    var startQuizPlaySessionAction: suspend (
        SessionSnapshot,
        String,
        String,
        QuizPlayType,
    ) -> QuizPlaySession = { _, _, _, _ ->
        error("Not used")
    },
) : QuizPlayRepository {
    var retryAllQuestionsCalls = 0
    var retryWrongQuestionsCalls = 0

    override suspend fun getQuizSession(session: SessionSnapshot, quizSessionId: String): QuizSession =
        getQuizSessionAction(session, quizSessionId)

    override suspend fun createQuizSession(
        session: SessionSnapshot,
        request: QuizCreateRequest,
    ): QuizGenerationAccepted = error("Not used")

    override suspend fun getQuizScope(session: SessionSnapshot, subjectId: String): QuizScope = error("Not used")

    override suspend fun getQuizGenerationStatus(
        session: SessionSnapshot,
        quizSessionId: String,
    ): QuizGenerationProgress = error("Not used")

    override suspend fun startQuizPlaySession(
        session: SessionSnapshot,
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType,
    ): QuizPlaySession = startQuizPlaySessionAction(session, quizSessionId, clientSessionId, playType)

    override suspend fun retryAllQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession {
        retryAllQuestionsCalls += 1
        return retryAllQuestionsAction(session, resultId, clientSessionId)
    }

    override suspend fun retryWrongQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession {
        retryWrongQuestionsCalls += 1
        return retryWrongQuestionsAction(session, resultId, clientSessionId)
    }

    override suspend fun submitQuizResult(
        session: SessionSnapshot,
        request: QuizResultSubmit,
    ): QuizSubmitResult = error("Not used")
}

private class TestSessionRepository(initialSession: SessionSnapshot) : SessionRepository {
    private var session = initialSession

    override suspend fun read(): SessionSnapshot = session
    override suspend fun saveOnboardingCompleted() = Unit
    override suspend fun saveHomeGuideCompleted() = Unit
    override suspend fun saveAuth(tokenData: AuthTokenData) {
        session = session.copy(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
            tokenType = tokenType(tokenData.tokenType),
        )
    }

    override suspend fun clearAuth() {
        session = session.copy(accessToken = null, refreshToken = null, tokenType = null)
    }

    private fun tokenType(tokenType: String?) = tokenType ?: session.tokenType
}

private class TestAuthRepository : AuthRepository {
    override suspend fun refreshToken(refreshToken: String): AuthTokenData = AuthTokenData(
        accessToken = "refreshed-access-token",
        refreshToken = "refreshed-refresh-token",
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 7200,
        user = AuthUser(
            id = "user-id",
            email = null,
            nickname = "테스터",
        ),
    )

    override suspend fun checkEmailAvailability(email: String): EmailAvailability = throw error("Not needed for this test")
    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData = throw error("Not needed for this test")
    override suspend fun resendEmailVerification(email: String): EmailVerificationSent =
        throw error("Not needed for this test")
    override suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData =
        throw error("Not needed for this test")
    override suspend fun login(email: String, password: String): AuthTokenData = throw error("Not needed for this test")
    override suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult = throw error("Not needed for this test")
    override suspend fun appleLogin(
        identityToken: String,
        authorizationCode: String?,
        fullName: String?,
    ): AppleLoginResult = throw error("Not needed for this test")
    override suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData =
        throw error("Not needed for this test")
    override suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = throw error("Not needed for this test")
    override suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData =
        throw error("Not needed for this test")
    override suspend fun linkAppleAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = throw error("Not needed for this test")
    override suspend fun logout(session: SessionSnapshot) = Unit
    override suspend fun getMe(session: SessionSnapshot): AuthUser = throw error("Not needed for this test")
    override suspend fun requestPasswordReset(email: String): PasswordResetRequested =
        throw error("Not needed for this test")
    override suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) = Unit
}

private fun testSession() = SessionSnapshot(
    onboardingCompleted = true,
    homeGuideCompleted = false,
    accessToken = "access-token",
    refreshToken = "refresh-token",
    tokenType = "Bearer",
    nickname = "테스터",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
)

private fun quizSession(
    id: String,
    playMode: QuizPlayMode,
    timerEnabled: Boolean,
    timerScope: QuizTimerScope?,
    timerSeconds: Int?,
    questions: List<QuizQuestion> = emptyList(),
): QuizSession = QuizSession(
    id = id,
    subjectId = "subject-id",
    subjectName = "SQLD",
    quizType = ServerQuizType.MultipleChoice,
    choiceCount = 4,
    questionCount = questions.size,
    playMode = playMode,
    timerEnabled = timerEnabled,
    timerScope = timerScope,
    timerSeconds = timerSeconds,
    difficulty = QuizDifficulty.Medium,
    status = "ready",
    questions = questions,
)
