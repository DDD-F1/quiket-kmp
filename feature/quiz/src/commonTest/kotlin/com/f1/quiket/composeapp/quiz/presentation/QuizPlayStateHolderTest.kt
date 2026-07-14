package com.f1.quiket.composeapp.quiz.presentation

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.quiz.domain.model.*
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizPlayStateHolderTest {
    @Test
    fun loadQuizStartsPlaySessionWhenMissing() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ ->
                quizSession(
                    id = "unsorted-session",
                    questions = listOf(
                        quizQuestion(id = "q-2", displayOrder = 2),
                        quizQuestion(id = "q-1", displayOrder = 1),
                    ),
                )
            },
            startQuizPlaySessionAction = { _, _, _, _ ->
                QuizPlaySession(
                    playSessionId = "play-1",
                    clientSessionId = "client-abc",
                    quizSessionId = "server-session",
                    playType = QuizPlayType.First,
                    status = "ready",
                    quizSession = quizSession(
                        id = "server-session",
                        questions = listOf(
                            quizQuestion(id = "q-1", displayOrder = 1),
                        ),
                    ),
                )
            },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )
        var sessionExpiredCount = 0

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(
                quizSessionId = "session-id",
                timerEnabled = true,
                timerScope = QuizTimerScope.Total,
                timerSeconds = 120,
                playType = QuizPlayType.RetryWrong,
            ),
            onSessionExpired = { sessionExpiredCount += 1 },
        )

        val state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertEquals("play-1", state.playSessionId)
        assertEquals("client-abc", state.clientSessionId)
        assertEquals("server-session", state.quizSession.id)
        assertEquals("q-1", state.questions.first().id)
        assertEquals(120, state.remainingTotalSeconds)
        assertEquals(1, repository.startQuizPlaySessionCalls)
        assertEquals(1, repository.getQuizSessionCalls)
        assertEquals(0, sessionExpiredCount)
    }

    @Test
    fun loadQuizReusesExistingPlaySessionWithoutRestart() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ -> quizSession(id = "quiz-session-2") },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(
                quizSessionId = "session-id",
                playSessionId = "existing-play-id",
                clientSessionId = "existing-client-id",
            ),
            onSessionExpired = {},
        )

        val state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertEquals("existing-play-id", state.playSessionId)
        assertEquals("existing-client-id", state.clientSessionId)
        assertEquals(0, repository.startQuizPlaySessionCalls)
        assertEquals(1, repository.getQuizSessionCalls)
    }

    @Test
    fun loadQuizUnauthorizedInvokesSessionExpiredCallback() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ -> throw QuizPlayException("expired", isUnauthorized = true) },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )
        var sessionExpiredCount = 0

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(quizSessionId = "session-id"),
            onSessionExpired = { sessionExpiredCount += 1 },
        )

        assertEquals(1, sessionExpiredCount)
        assertIs<QuizPlayUiState.Loading>(stateHolder.state)
    }

    @Test
    fun loadQuizFailureMovesToErrorState() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ -> throw IllegalStateException("network is unavailable") },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(quizSessionId = "session-id"),
            onSessionExpired = {},
        )

        val state = assertIs<QuizPlayUiState.Error>(stateHolder.state)
        assertTrue(state.message.isNotBlank())
    }

    @Test
    fun perQuestionOneByOneTimerMarksQuestionCheckedAtZero() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ ->
                quizSession(
                    id = "timer-session",
                    timerEnabled = true,
                    timerScope = QuizTimerScope.PerQuestion,
                    timerSeconds = 1,
                    playMode = QuizPlayMode.OneByOne,
                    questions = listOf(quizQuestion(id = "q-1", displayOrder = 1)),
                )
            },
            startQuizPlaySessionAction = { session, quizSessionId, clientSessionId, playType ->
                QuizPlaySession(
                    playSessionId = "play-1",
                    clientSessionId = clientSessionId,
                    quizSessionId = quizSessionId,
                    playType = playType,
                    status = "ready",
                    quizSession = null,
                )
            },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )
        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(
                quizSessionId = "timer-session",
                timerEnabled = true,
                timerScope = QuizTimerScope.PerQuestion,
                timerSeconds = 1,
                playMode = QuizPlayMode.OneByOne,
            ),
            onSessionExpired = {},
        )

        stateHolder.tickTimer()

        val state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertEquals(0, state.remainingSecondsByQuestionId["q-1"])
        assertEquals(setOf("q-1"), state.checkedQuestionIds)
    }

    @Test
    fun perQuestionTimerStopsAfterQuestionChecked() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ ->
                quizSession(
                    id = "timer-session",
                    timerEnabled = true,
                    timerScope = QuizTimerScope.PerQuestion,
                    timerSeconds = 2,
                    playMode = QuizPlayMode.OneByOne,
                    questions = listOf(quizQuestion(id = "q-1", displayOrder = 1)),
                )
            },
            startQuizPlaySessionAction = { session, quizSessionId, clientSessionId, playType ->
                QuizPlaySession(
                    playSessionId = "play-1",
                    clientSessionId = clientSessionId,
                    quizSessionId = quizSessionId,
                    playType = playType,
                    status = "ready",
                    quizSession = null,
                )
            },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )
        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(
                quizSessionId = "timer-session",
                timerEnabled = true,
                timerScope = QuizTimerScope.PerQuestion,
                timerSeconds = 2,
                playMode = QuizPlayMode.OneByOne,
            ),
            onSessionExpired = {},
        )

        stateHolder.tickTimer()
        var state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertEquals(1, state.remainingSecondsByQuestionId["q-1"])
        assertFalse(state.isCurrentQuestionChecked)

        stateHolder.tickTimer()
        state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertEquals(0, state.remainingSecondsByQuestionId["q-1"])
        assertTrue(state.isCurrentQuestionChecked)

        stateHolder.tickTimer()
        state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertEquals(0, state.remainingSecondsByQuestionId["q-1"])
        assertTrue(state.isCurrentQuestionChecked)
    }

    @Test
    fun submitSuccessBuildsAnswersAndReturnsResult() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ ->
                quizSession(
                    id = "submit-session",
                    timerEnabled = false,
                    questions = listOf(quizQuestion(id = "q-1"), quizQuestion(id = "q-2")),
                )
            },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(quizSessionId = "submit-session", timerEnabled = false),
            onSessionExpired = {},
        )
        stateHolder.updateReady { ready ->
            ready.copy(
                selectedOptionIds = mapOf(
                    "q-1" to ready.questions.first { it.id == "q-1" }.options[0].id,
                    "q-2" to ready.questions.first { it.id == "q-2" }.options[1].id,
                ),
                bookmarkedQuestionIds = setOf("q-2"),
            )
        }

        val resultId = stateHolder.submit(onSessionExpired = {})

        val request = assertNotNull(repository.submittedRequests.single())
        assertEquals("result-id-1", resultId)
        assertEquals(2, request.answers.size)
        assertEquals("q-2-a2", request.answers.first { it.questionId == "q-2" }.selectedOptionId)
        assertEquals(true, request.answers.first { it.questionId == "q-2" }.marked)
        assertEquals(false, request.answers.first { it.questionId == "q-1" }.marked)
    }

    @Test
    fun submitFailureShowsMessage() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ ->
                quizSession(
                    id = "submit-session",
                    timerEnabled = false,
                    questions = listOf(quizQuestion(id = "q-1")),
                )
            },
            submitQuizResultAction = { _, _ -> throw RuntimeException("submit failed") },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(quizSessionId = "submit-session", timerEnabled = false),
            onSessionExpired = {},
        )
        stateHolder.updateReady { ready ->
            ready.copy(selectedOptionIds = mapOf("q-1" to ready.questions.first().options.first().id))
        }
        val returned = stateHolder.submit(onSessionExpired = {})

        assertNull(returned)
        val state = assertIs<QuizPlayUiState.Ready>(stateHolder.state)
        assertTrue(state.errorMessage.orEmpty().isNotBlank())
        assertEquals(false, state.isSubmitting)
        assertEquals(1, repository.submittedCalls)
    }

    @Test
    fun submitUnauthorizedInvokesSessionExpiredCallback() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ -> quizSession(id = "submit-session", timerEnabled = false) },
            submitQuizResultAction = { _, _ -> throw QuizPlayException("expired", isUnauthorized = true) },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )
        var sessionExpiredCount = 0

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(quizSessionId = "submit-session", timerEnabled = false),
            onSessionExpired = {},
        )
        stateHolder.updateReady { ready ->
            ready.copy(selectedOptionIds = mapOf("q-1" to ready.questions.first().options.first().id))
        }
        val returned = stateHolder.submit(onSessionExpired = { sessionExpiredCount += 1 })

        assertNull(returned)
        assertEquals(1, sessionExpiredCount)
        assertEquals(1, repository.submittedCalls)
    }

    @Test
    fun submitIgnoresWhenAlreadySubmitting() = runTest {
        val repository = FakeQuizPlayRepository(
            getQuizSessionAction = { _, _ -> quizSession(id = "submit-session", timerEnabled = false) },
            submitQuizResultAction = { _, _ -> throw IllegalStateException("ignored") },
        )
        val stateHolder = QuizPlayStateHolder(
            quizPlayUseCases = QuizPlayUseCases(
                authenticatedCallRunner = testAuthenticatedCallRunner(),
                repository = repository,
            ),
        )

        stateHolder.loadQuiz(
            launchConfig = QuizPlayLaunchConfig(quizSessionId = "submit-session", timerEnabled = false),
            onSessionExpired = {},
        )
        stateHolder.updateReady { it.copy(isSubmitting = true) }

        val returned = stateHolder.submit(onSessionExpired = {})

        assertNull(returned)
        assertEquals(0, repository.submittedCalls)
    }
}

private fun testAuthenticatedCallRunner(
    refreshToken: String? = null,
): AuthenticatedCallRunner = AuthenticatedCallRunner(
    sessionRepository = TestSessionRepository(
        initialSession = testSession(refreshToken),
    ),
    authRepository = TestAuthRepository(),
)

private fun quizSession(
    id: String,
    timerEnabled: Boolean = true,
    timerScope: QuizTimerScope? = QuizTimerScope.Total,
    timerSeconds: Int? = 300,
    playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    questions: List<QuizQuestion> = listOf(quizQuestion("q-1")),
): QuizSession = QuizSession(
    id = id,
    subjectId = "subject-1",
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

private fun quizQuestion(id: String, displayOrder: Int = 1): QuizQuestion = QuizQuestion(
    id = id,
    subjectId = "subject-1",
    chapterId = "chapter-1",
    partId = "part-1",
    partName = "Part 1",
    questionType = ServerQuizType.MultipleChoice,
    difficulty = QuizDifficulty.Medium,
    summary = null,
    body = "질문 $id",
    displayOrder = displayOrder,
    options = listOf(
        QuizOption(id = "${id}-a1", optionNumber = 1, content = "A1"),
        QuizOption(id = "${id}-a2", optionNumber = 2, content = "A2"),
    ),
    answerValue = "${id}-a1",
    correctExplanation = null,
    incorrectExplanation = null,
)

private fun testSession(refreshToken: String?): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = true,
    homeGuideCompleted = false,
    accessToken = "access-token",
    refreshToken = refreshToken,
    tokenType = "Bearer",
    nickname = "테스터",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
)

private class TestSessionRepository(
    initialSession: SessionSnapshot,
) : SessionRepository {
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

    override suspend fun checkEmailAvailability(email: String) = unsupported()
    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): com.f1.quiket.composeapp.auth.domain.model.SignupData = unsupported()
    override suspend fun resendEmailVerification(email: String): com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent =
        unsupported()
    override suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData =
        unsupported()
    override suspend fun login(email: String, password: String): AuthTokenData = unsupported()
    override suspend fun kakaoLogin(kakaoAccessToken: String): com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult = unsupported()
    override suspend fun appleLogin(
        identityToken: String,
        authorizationCode: String?,
        fullName: String?,
    ): com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult = unsupported()
    override suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData = unsupported()
    override suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = unsupported()
    override suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData = unsupported()
    override suspend fun linkAppleAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = unsupported()
    override suspend fun logout(session: SessionSnapshot) = Unit
    override suspend fun getMe(session: SessionSnapshot): com.f1.quiket.composeapp.auth.domain.model.AuthUser = unsupported()
    override suspend fun requestPasswordReset(email: String): com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested =
        unsupported()
    override suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) = Unit

    private fun unsupported(): Nothing = error("Not needed for this test")
}

private class FakeQuizPlayRepository(
    var getQuizSessionAction: suspend (SessionSnapshot, String) -> QuizSession = { _, _ -> quizSession("session-id") },
    var startQuizPlaySessionAction: suspend (
        SessionSnapshot,
        String,
        String,
        QuizPlayType,
    ) -> QuizPlaySession = { _, _, _, _ ->
        QuizPlaySession(
            playSessionId = "play-1",
            clientSessionId = "client-1",
            quizSessionId = "session-id",
            playType = QuizPlayType.First,
            status = "ready",
            quizSession = null,
        )
    },
    var submitQuizResultAction: suspend (SessionSnapshot, QuizResultSubmit) -> QuizSubmitResult = { _, _ ->
        QuizSubmitResult(playSessionId = "play-1", resultId = "result-id-1")
    },
    var createQuizSessionAction: suspend (SessionSnapshot, QuizCreateRequest) -> QuizGenerationAccepted = { _, _ ->
        unsupported("Not used")
    },
    var getQuizScopeAction: suspend (SessionSnapshot, String) -> QuizScope = { _, _ ->
        unsupported("Not used")
    },
    var getQuizGenerationStatusAction: suspend (SessionSnapshot, String) -> QuizGenerationProgress = { _, _ ->
        unsupported("Not used")
    },
    var retryAllQuestionsAction: suspend (SessionSnapshot, String, String) -> QuizPlaySession = { _, _, _ ->
        unsupported("Not used")
    },
    var retryWrongQuestionsAction: suspend (SessionSnapshot, String, String) -> QuizPlaySession = { _, _, _ ->
        unsupported("Not used")
    },
) : QuizPlayRepository {
    var getQuizSessionCalls = 0
    var startQuizPlaySessionCalls = 0
    var submittedCalls = 0
    val submittedRequests = mutableListOf<QuizResultSubmit>()

    override suspend fun getQuizSession(session: SessionSnapshot, quizSessionId: String): QuizSession {
        getQuizSessionCalls += 1
        return getQuizSessionAction(session, quizSessionId)
    }

    override suspend fun createQuizSession(
        session: SessionSnapshot,
        request: QuizCreateRequest,
    ): QuizGenerationAccepted = createQuizSessionAction(session, request)

    override suspend fun getQuizScope(session: SessionSnapshot, subjectId: String): QuizScope =
        getQuizScopeAction(session, subjectId)

    override suspend fun getQuizGenerationStatus(session: SessionSnapshot, quizSessionId: String): QuizGenerationProgress =
        getQuizGenerationStatusAction(session, quizSessionId)

    override suspend fun startQuizPlaySession(
        session: SessionSnapshot,
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType,
    ): QuizPlaySession {
        startQuizPlaySessionCalls += 1
        return startQuizPlaySessionAction(session, quizSessionId, clientSessionId, playType)
    }

    override suspend fun retryAllQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession = retryAllQuestionsAction(session, resultId, clientSessionId)

    override suspend fun retryWrongQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession = retryWrongQuestionsAction(session, resultId, clientSessionId)

    override suspend fun submitQuizResult(
        session: SessionSnapshot,
        request: QuizResultSubmit,
    ): QuizSubmitResult {
        submittedCalls += 1
        submittedRequests += request
        return submitQuizResultAction(session, request)
    }
}

private fun unsupported(message: String): Nothing = error(message)
