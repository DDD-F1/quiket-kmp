package com.f1.quiket.composeapp.quiz.domain.model

import com.f1.quiket.composeapp.subject.ChapterWithParts

internal class QuizPlayException(
    message: String,
    val isUnauthorized: Boolean = false,
) : Exception(message)

internal data class QuizPlayLaunchConfig(
    val quizSessionId: String,
    val clientSessionId: String? = null,
    val playSessionId: String? = null,
    val playType: QuizPlayType = QuizPlayType.First,
    val playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    val timerEnabled: Boolean = false,
    val timerScope: QuizTimerScope? = null,
    val timerSeconds: Int? = null,
)

internal data class QuizCreateRequest(
    val subjectId: String,
    val partIds: List<String>,
    val quizType: ServerQuizType,
    val choiceCount: Int?,
    val questionCount: Int,
    val playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    val timerEnabled: Boolean = false,
    val timerScope: QuizTimerScope? = null,
    val timerSeconds: Int? = null,
    val difficulty: QuizDifficulty,
)

internal data class QuizGenerationAccepted(
    val quizSessionId: String,
    val jobId: String,
    val status: QuizGenerationStatus,
    val estimatedSeconds: Int?,
)

internal data class QuizGenerationProgress(
    val quizSessionId: String,
    val jobId: String,
    val status: QuizGenerationStatus,
    val estimatedSeconds: Int?,
    val progressPct: Int?,
    val generatedCount: Int?,
    val failReason: String?,
)

internal data class QuizScope(
    val subjectId: String,
    val subjectName: String,
    val chapters: List<ChapterWithParts>,
) {
    val id: String get() = subjectId
    val name: String get() = subjectName
}

internal data class QuizSession(
    val id: String,
    val subjectId: String,
    val subjectName: String?,
    val quizType: ServerQuizType,
    val choiceCount: Int?,
    val questionCount: Int,
    val playMode: QuizPlayMode,
    val timerEnabled: Boolean,
    val timerScope: QuizTimerScope?,
    val timerSeconds: Int?,
    val difficulty: QuizDifficulty,
    val status: String,
    val questions: List<QuizQuestion>,
)

internal data class QuizQuestion(
    val id: String,
    val subjectId: String?,
    val chapterId: String?,
    val partId: String?,
    val partName: String?,
    val questionType: ServerQuizType,
    val difficulty: QuizDifficulty?,
    val summary: String?,
    val body: String,
    val displayOrder: Int,
    val options: List<QuizOption>,
    val answerValue: String?,
    val correctExplanation: String?,
    val incorrectExplanation: String?,
)

internal data class QuizOption(
    val id: String,
    val optionNumber: Int,
    val content: String,
    val value: String? = null,
)

internal data class QuizPlaySession(
    val playSessionId: String,
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: QuizPlayType,
    val status: String,
    val quizSession: QuizSession?,
)

internal data class QuizResultSubmit(
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: QuizPlayType,
    val elapsedMs: Int,
    val answers: List<QuizAnswerSubmitItem>,
)

internal data class QuizAnswerSubmitItem(
    val questionId: String,
    val selectedOptionId: String?,
    val selectedValue: String?,
    val correctClient: Boolean?,
    val skipped: Boolean,
    val marked: Boolean = false,
)

internal data class QuizSubmitResult(
    val playSessionId: String,
    val resultId: String?,
)

internal enum class ServerQuizType(val wireValue: String) {
    MultipleChoice("multiple_choice"),
    Ox("ox"),
}

internal enum class QuizPlayMode(val wireValue: String) {
    AllAtOnce("all_at_once"),
    OneByOne("one_by_one"),
}

internal enum class QuizTimerScope(val wireValue: String) {
    PerQuestion("per_question"),
    Total("total"),
}

internal enum class QuizPlayType(val wireValue: String) {
    First("first"),
    RetryAll("retry_all"),
    RetryWrong("retry_wrong"),
    Unknown("unknown"),
}

internal enum class QuizDifficulty(val wireValue: String) {
    Easy("easy"),
    Medium("medium"),
    Hard("hard"),
}

internal enum class QuizGenerationStatus(val wireValue: String) {
    Pending("pending"),
    InProgress("in_progress"),
    Completed("completed"),
    Failed("failed"),
    Unknown("unknown"),
}

internal fun QuizPlaySession.toLaunchConfig(): QuizPlayLaunchConfig {
    val session = quizSession
    return QuizPlayLaunchConfig(
        quizSessionId = session?.id ?: quizSessionId,
        clientSessionId = clientSessionId,
        playSessionId = playSessionId,
        playType = playType,
        playMode = session?.playMode ?: QuizPlayMode.AllAtOnce,
        timerEnabled = session?.timerEnabled ?: false,
        timerScope = session?.timerScope,
        timerSeconds = session?.timerSeconds,
    )
}

internal fun QuizOption.matchesAnswerValue(answerValue: String?): Boolean? {
    if (answerValue.isNullOrBlank()) return null
    val normalizedOption = (value ?: content).toOxAnswerSymbol() ?: optionNumber.toOxAnswerSymbol()
    val normalizedAnswer = answerValue.toOxAnswerSymbol()

    return id == answerValue ||
        optionNumber.toString() == answerValue ||
        value == answerValue ||
        content == answerValue ||
        (normalizedOption != null && normalizedOption == normalizedAnswer)
}

internal fun ServerQuizType.defaultOptions(questionId: String): List<QuizOption> =
    when (this) {
        ServerQuizType.Ox -> listOf(
            QuizOption(
                id = "$questionId-ox-o",
                optionNumber = 1,
                content = "O",
                value = "O",
            ),
            QuizOption(
                id = "$questionId-ox-x",
                optionNumber = 2,
                content = "X",
                value = "X",
            ),
        )

        ServerQuizType.MultipleChoice -> emptyList()
    }

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
