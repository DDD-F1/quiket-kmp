package com.f1.quiket.composeapp.quiz.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import com.f1.quiket.composeapp.quiz.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.composeapp.quiz.domain.model.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationAccepted
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationProgress
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationStatus
import com.f1.quiket.composeapp.quiz.domain.model.QuizOption
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayException
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlaySession
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizQuestion
import com.f1.quiket.composeapp.quiz.domain.model.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.domain.model.QuizSession
import com.f1.quiket.composeapp.quiz.domain.model.QuizSubmitResult
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import com.f1.quiket.composeapp.subject.ChapterWithParts
import com.f1.quiket.composeapp.subject.PartSummary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class QuizPlayClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getQuizSession(
        session: SessionSnapshot,
        quizSessionId: String,
    ): QuizSession {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}quiz-sessions/$quizSessionId") {
            header("Authorization", authorization)
        }

        val envelope = parseResponse(response.bodyAsText())
        if (response.status.value == 401) {
            throw QuizPlayException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw QuizPlayException(envelope.message.ifBlank { "퀴즈 정보를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw QuizPlayException("퀴즈 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizSessionDataResponse>(data).toDomain()
        }.getOrElse {
            throw QuizPlayException("퀴즈 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun createQuizSession(
        session: SessionSnapshot,
        request: QuizCreateRequest,
    ): QuizGenerationAccepted {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}quiz-sessions") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(request.toRequest())
        }

        val envelope = parseResponse(response.bodyAsText())
        if (response.status.value == 401) {
            throw QuizPlayException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw QuizPlayException(envelope.message.ifBlank { "퀴즈 생성을 시작하지 못했습니다." })
        }

        val data = envelope.data ?: throw QuizPlayException("퀴즈 생성 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizGenerationAcceptedDataResponse>(data).toDomain()
        }.getOrElse {
            throw QuizPlayException("퀴즈 생성 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun getQuizScope(
        session: SessionSnapshot,
        subjectId: String,
    ): QuizScope {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/quiz-scope") {
            header("Authorization", authorization)
        }

        val envelope = parseResponse(response.bodyAsText())
        if (response.status.value == 401) {
            throw QuizPlayException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw QuizPlayException(envelope.message.ifBlank { "퀴즈 범위를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw QuizPlayException("퀴즈 범위 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizScopeDataResponse>(data).toDomain()
        }.getOrElse {
            throw QuizPlayException("퀴즈 범위 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun getQuizGenerationStatus(
        session: SessionSnapshot,
        quizSessionId: String,
    ): QuizGenerationProgress {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}quiz-sessions/$quizSessionId/generation-status") {
            header("Authorization", authorization)
        }

        val envelope = parseResponse(response.bodyAsText())
        if (response.status.value == 401) {
            throw QuizPlayException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw QuizPlayException(envelope.message.ifBlank { "퀴즈 생성 상태를 확인하지 못했습니다." })
        }

        val data = envelope.data ?: throw QuizPlayException("퀴즈 생성 상태 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizGenerationStatusDataResponse>(data).toDomain()
        }.getOrElse {
            throw QuizPlayException("퀴즈 생성 상태 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun startQuizPlaySession(
        session: SessionSnapshot,
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType = QuizPlayType.First,
    ): QuizPlaySession {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}quiz-sessions/$quizSessionId/play-sessions") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                QuizPlayStartRequest(
                    clientSessionId = clientSessionId,
                    playType = playType.wireValue,
                    questionShuffled = false,
                    optionShuffled = false,
                ),
            )
        }

        return response.toPlaySession(defaultErrorMessage = "풀이 세션을 시작하지 못했습니다.")
    }

    suspend fun retryAllQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession = retryQuestions(
        session = session,
        resultId = resultId,
        clientSessionId = clientSessionId,
        path = "retry-all",
        defaultErrorMessage = "전체 다시 풀기를 시작하지 못했습니다.",
    )

    suspend fun retryWrongQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession = retryQuestions(
        session = session,
        resultId = resultId,
        clientSessionId = clientSessionId,
        path = "retry-wrong",
        defaultErrorMessage = "틀린 문제 다시 풀기를 시작하지 못했습니다.",
    )

    suspend fun submitQuizResult(
        session: SessionSnapshot,
        request: QuizResultSubmit,
    ): QuizSubmitResult {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}quiz-results") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(request.toRequest())
        }

        val envelope = parseResponse(response.bodyAsText())
        if (response.status.value == 401) {
            throw QuizPlayException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw QuizPlayException(envelope.message.ifBlank { "퀴즈 결과를 제출하지 못했습니다." })
        }

        val data = envelope.data ?: throw QuizPlayException("결과 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizResultDataResponse>(data).toSubmitResult()
        }.getOrElse {
            throw QuizPlayException("결과 응답을 해석하지 못했습니다.")
        }
    }

    private suspend fun retryQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
        path: String,
        defaultErrorMessage: String,
    ): QuizPlaySession {
        val authorization = session.authorizationHeader()
            ?: throw QuizPlayException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}quiz-results/$resultId/$path") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                QuizRetryRequest(
                    clientSessionId = clientSessionId,
                    questionShuffled = false,
                    optionShuffled = false,
                ),
            )
        }

        return response.toPlaySession(defaultErrorMessage = defaultErrorMessage)
    }

    private suspend fun io.ktor.client.statement.HttpResponse.toPlaySession(
        defaultErrorMessage: String,
    ): QuizPlaySession {
        val envelope = parseResponse(bodyAsText())
        if (status.value == 401) {
            throw QuizPlayException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (status.value !in 200..299 || !envelope.success) {
            throw QuizPlayException(envelope.message.ifBlank { defaultErrorMessage })
        }

        val data = envelope.data ?: throw QuizPlayException("풀이 세션 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizPlaySessionDataResponse>(data).toDomain()
        }.getOrElse {
            throw QuizPlayException("풀이 세션 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseResponse(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw QuizPlayException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }
}

@Serializable
private data class QuizSessionDataResponse(
    val id: String,
    val subjectId: String,
    val subjectName: String? = null,
    val quizType: String,
    val choiceCount: Int? = null,
    val questionCount: Int,
    val playMode: String,
    val timerEnabled: Boolean = false,
    val timerScope: String? = null,
    val timerSeconds: Int? = null,
    val difficulty: String,
    val status: String,
    val questions: List<QuestionResponse> = emptyList(),
) {
    fun toDomain(): QuizSession = QuizSession(
        id = id,
        subjectId = subjectId,
        subjectName = subjectName,
        quizType = quizType.toServerQuizType(),
        choiceCount = choiceCount,
        questionCount = questionCount,
        playMode = playMode.toQuizPlayMode(),
        timerEnabled = timerEnabled,
        timerScope = timerScope.toQuizTimerScope(),
        timerSeconds = timerSeconds,
        difficulty = difficulty.toQuizDifficulty(),
        status = status,
        questions = questions.map { it.toDomain() },
    )
}

@Serializable
private data class QuizScopeDataResponse(
    val subjectId: String,
    val subjectName: String,
    val chapters: List<QuizScopeChapterResponse> = emptyList(),
) {
    fun toDomain(): QuizScope = QuizScope(
        subjectId = subjectId,
        subjectName = subjectName,
        chapters = chapters.map { it.toDomain() },
    )
}

@Serializable
private data class QuizScopeChapterResponse(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<QuizScopePartResponse> = emptyList(),
) {
    fun toDomain(): ChapterWithParts = ChapterWithParts(
        id = id,
        subjectId = subjectId,
        name = name,
        displayOrder = displayOrder,
        parts = parts.map { it.toDomain() },
    )
}

@Serializable
private data class QuizScopePartResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
) {
    fun toDomain(): PartSummary = PartSummary(
        id = id,
        chapterId = chapterId,
        name = name,
        partNumber = partNumber,
        contentPreview = contentPreview,
    )
}

@Serializable
private data class QuizCreateRequestDto(
    val subjectId: String,
    val partIds: List<String>,
    val quizType: String,
    val choiceCount: Int? = null,
    val questionCount: Int,
    val playMode: String,
    val timerEnabled: Boolean = false,
    val timerScope: String? = null,
    val timerSeconds: Int? = null,
    val difficulty: String,
)

@Serializable
private data class QuizGenerationAcceptedDataResponse(
    val quizSessionId: String,
    val jobId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
) {
    fun toDomain(): QuizGenerationAccepted = QuizGenerationAccepted(
        quizSessionId = quizSessionId,
        jobId = jobId,
        status = status.toQuizGenerationStatus(),
        estimatedSeconds = estimatedSeconds,
    )
}

@Serializable
private data class QuizGenerationStatusDataResponse(
    val quizSessionId: String,
    val jobId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
    val progressPct: Int? = null,
    val generatedCount: Int? = null,
    val failReason: String? = null,
) {
    fun toDomain(): QuizGenerationProgress = QuizGenerationProgress(
        quizSessionId = quizSessionId,
        jobId = jobId,
        status = status.toQuizGenerationStatus(),
        estimatedSeconds = estimatedSeconds,
        progressPct = progressPct,
        generatedCount = generatedCount,
        failReason = failReason,
    )
}

@Serializable
private data class QuestionResponse(
    val id: String,
    val subjectId: String? = null,
    val chapterId: String? = null,
    val partId: String? = null,
    val partName: String? = null,
    val questionType: String,
    val difficulty: String? = null,
    val summary: String? = null,
    val body: String,
    val displayOrder: Int,
    val options: List<QuestionOptionResponse> = emptyList(),
    val answer: QuestionAnswerResponse,
    val correctExplanation: String? = null,
    val incorrectExplanation: String? = null,
) {
    fun toDomain(): QuizQuestion = QuizQuestion(
        id = id,
        subjectId = subjectId,
        chapterId = chapterId,
        partId = partId,
        partName = partName,
        questionType = questionType.toServerQuizType(),
        difficulty = difficulty?.toQuizDifficulty(),
        summary = summary,
        body = body,
        displayOrder = displayOrder,
        options = options.map { it.toDomain() },
        answerValue = answer.answerValue,
        correctExplanation = correctExplanation,
        incorrectExplanation = incorrectExplanation,
    )
}

@Serializable
private data class QuestionOptionResponse(
    val id: String,
    val optionNumber: Int,
    val content: String,
) {
    fun toDomain(): QuizOption = QuizOption(
        id = id,
        optionNumber = optionNumber,
        content = content,
    )
}

@Serializable
private data class QuestionAnswerResponse(
    val answerValue: String,
)

@Serializable
private data class QuizPlayStartRequest(
    val clientSessionId: String,
    val playType: String,
    val parentPlaySessionId: String? = null,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = false,
    val shuffleSeed: String? = null,
)

@Serializable
private data class QuizRetryRequest(
    val clientSessionId: String,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = false,
    val shuffleSeed: String? = null,
)

@Serializable
private data class QuizPlaySessionDataResponse(
    val playSessionId: String,
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: String,
    val status: String,
    val quizSession: QuizSessionDataResponse? = null,
) {
    fun toDomain(): QuizPlaySession = QuizPlaySession(
        playSessionId = playSessionId,
        clientSessionId = clientSessionId,
        quizSessionId = quizSessionId,
        playType = playType.toQuizPlayType(),
        status = status,
        quizSession = quizSession?.toDomain(),
    )
}

@Serializable
private data class QuizResultSubmitRequest(
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: String,
    val parentPlaySessionId: String? = null,
    val elapsedMs: Int,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = false,
    val shuffleSeed: String? = null,
    val answers: List<QuizAnswerSubmitItemRequest>,
)

@Serializable
private data class QuizAnswerSubmitItemRequest(
    val questionId: String,
    val selectedOptionId: String? = null,
    val selectedValue: String? = null,
    val correctClient: Boolean? = null,
    val skipped: Boolean,
    val answerElapsedMs: Int? = null,
    val marked: Boolean = false,
)

@Serializable
private data class QuizResultDataResponse(
    val playSessionId: String,
    val resultId: String? = null,
) {
    fun toSubmitResult(): QuizSubmitResult = QuizSubmitResult(
        playSessionId = playSessionId,
        resultId = resultId,
    )
}

private fun QuizCreateRequest.toRequest(): QuizCreateRequestDto = QuizCreateRequestDto(
    subjectId = subjectId,
    partIds = partIds,
    quizType = quizType.wireValue,
    choiceCount = if (quizType == ServerQuizType.MultipleChoice) {
        choiceCount ?: 4
    } else {
        null
    },
    questionCount = questionCount.coerceIn(1, 100),
    playMode = playMode.wireValue,
    timerEnabled = timerEnabled && timerSeconds != null,
    timerScope = timerScope?.takeIf { timerEnabled && timerSeconds != null }?.wireValue,
    timerSeconds = timerSeconds?.takeIf { timerEnabled },
    difficulty = difficulty.wireValue,
)

private fun QuizResultSubmit.toRequest(): QuizResultSubmitRequest = QuizResultSubmitRequest(
    clientSessionId = clientSessionId,
    quizSessionId = quizSessionId,
    playType = playType.wireValue,
    elapsedMs = elapsedMs,
    answers = answers.map { it.toRequest() },
)

private fun QuizAnswerSubmitItem.toRequest(): QuizAnswerSubmitItemRequest = QuizAnswerSubmitItemRequest(
    questionId = questionId,
    selectedOptionId = selectedOptionId,
    selectedValue = selectedValue,
    correctClient = correctClient,
    skipped = skipped,
    marked = marked,
)

private fun String.toServerQuizType(): ServerQuizType =
    ServerQuizType.entries.firstOrNull { it.wireValue == this }
        ?: ServerQuizType.MultipleChoice

private fun String.toQuizPlayMode(): QuizPlayMode =
    QuizPlayMode.entries.firstOrNull { it.wireValue == this }
        ?: QuizPlayMode.AllAtOnce

private fun String?.toQuizTimerScope(): QuizTimerScope? =
    QuizTimerScope.entries.firstOrNull { it.wireValue == this }

private fun String.toQuizPlayType(): QuizPlayType =
    QuizPlayType.entries.firstOrNull { it.wireValue == this }
        ?: QuizPlayType.Unknown

private fun String.toQuizDifficulty(): QuizDifficulty =
    QuizDifficulty.entries.firstOrNull { it.wireValue == this }
        ?: QuizDifficulty.Medium

private fun String.toQuizGenerationStatus(): QuizGenerationStatus =
    QuizGenerationStatus.entries.firstOrNull { it.wireValue == this }
        ?: QuizGenerationStatus.Unknown

private fun Int.toOxAnswerSymbol(): String? =
    when (this) {
        1 -> "O"
        2 -> "X"
        else -> null
    }

private fun String.toOxAnswerSymbol(): String? =
    when (trim().uppercase()) {
        "O", "TRUE", "T", "YES", "Y", "그렇다", "맞다" -> "O"
        "X", "FALSE", "F", "NO", "N", "아니다", "틀리다" -> "X"
        else -> null
    }

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}
