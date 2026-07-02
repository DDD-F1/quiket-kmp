package com.f1.quiket.composeapp.quiz.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.quiz.QuizDifficulty
import com.f1.quiket.composeapp.quiz.QuizPlayException
import com.f1.quiket.composeapp.quiz.QuizQuestion
import com.f1.quiket.composeapp.quiz.QuizSession
import com.f1.quiket.composeapp.quiz.ServerQuizType
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.toQuizNetworkAwareMessage

internal class QuizStartStateHolder(
    private val quizPlayUseCases: QuizPlayUseCases,
) {
    var state by mutableStateOf<QuizStartUiState>(QuizStartUiState.Loading)
        private set

    suspend fun loadQuizSession(
        quizSessionId: String,
        onSessionExpired: () -> Unit,
    ) {
        state = QuizStartUiState.Loading
        runCatching {
            quizPlayUseCases.getQuizSession(quizSessionId = quizSessionId)
        }.onSuccess { quizSession ->
            state = QuizStartUiState.Ready(
                summary = quizSession.toStartSummary(),
            )
        }.onFailure { error ->
            if (error is QuizPlayException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = QuizStartUiState.Error(
                    message = error.toQuizNetworkAwareMessage(
                        fallback = QuizStartLoadFailureMessage,
                    ),
                )
            }
        }
    }
}

internal sealed interface QuizStartUiState {
    data object Loading : QuizStartUiState

    data class Error(
        val message: String,
    ) : QuizStartUiState

    data class Ready(
        val summary: QuizStartSummary,
    ) : QuizStartUiState
}

internal data class QuizStartSummary(
    val title: String,
    val quizTypeLabel: String,
    val choiceLabel: String?,
    val questionCountLabel: String,
    val difficultyLabel: String,
    val scopeLabels: List<String>,
)

private fun QuizSession.toStartSummary(): QuizStartSummary {
    val choiceLabel = when (quizType) {
        ServerQuizType.MultipleChoice -> "${choiceCount ?: 4}지선다"
        ServerQuizType.Ox -> null
    }

    return QuizStartSummary(
        title = subjectName.orEmpty().ifBlank { "퀴즈" },
        quizTypeLabel = when (quizType) {
            ServerQuizType.MultipleChoice -> "객관식"
            ServerQuizType.Ox -> "O/X 퀴즈"
        },
        choiceLabel = choiceLabel,
        questionCountLabel = "${questionCount}문제",
        difficultyLabel = "난이도: ${difficulty.toLabel()}",
        scopeLabels = questions.toScopeLabels(),
    )
}

private fun QuizDifficulty.toLabel(): String = when (this) {
    QuizDifficulty.Easy -> "쉬움"
    QuizDifficulty.Medium -> "보통"
    QuizDifficulty.Hard -> "어려움"
}

private fun List<QuizQuestion>.toScopeLabels(): List<String> {
    val partLabels = mapNotNull { question -> question.partName?.takeIf(String::isNotBlank) }
        .distinct()
        .take(3)

    return partLabels.ifEmpty {
        val chapterCount = mapNotNull { question -> question.chapterId }.distinct().size
        val partCount = mapNotNull { question -> question.partId }.distinct().size
        when {
            chapterCount > 0 || partCount > 0 -> listOf("챕터 $chapterCount / 파트 $partCount")
            else -> listOf("출제 범위")
        }
    }
}

private const val QuizStartLoadFailureMessage = "퀴즈 정보를 불러올 수 없어요. 잠시 후 다시 시도해 주세요."
