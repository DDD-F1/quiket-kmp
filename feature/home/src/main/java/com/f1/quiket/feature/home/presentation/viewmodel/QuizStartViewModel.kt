package com.f1.quiket.feature.home.presentation.viewmodel

import com.f1.quiket.feature.home.presentation.contract.*

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.Question
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizSession
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.f1.quiket.feature.home.domain.repository.QuizPlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuizStartViewModel @Inject constructor(
    private val quizPlayRepository: QuizPlayRepository,
) : MviViewModel<QuizStartState, QuizStartIntent, QuizStartEffect>(
    initialState = QuizStartState(),
) {
    override fun handleIntent(intent: QuizStartIntent) {
        when (intent) {
            is QuizStartIntent.Load -> load(intent.quizSessionId)
        }
    }

    private fun load(quizSessionId: String?) {
        if (quizSessionId.isNullOrBlank()) {
            updateState {
                copy(
                    isLoading = false,
                    errorMessage = "퀴즈 정보를 불러올 수 없어요.",
                )
            }
            return
        }

        launch {
            updateState {
                copy(
                    isLoading = true,
                    summary = null,
                    errorMessage = null,
                )
            }

            when (val result = quizPlayRepository.getQuizSession(quizSessionId)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            summary = result.data.toSummaryUiModel(),
                            errorMessage = null,
                        )
                    }
                }

                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isLoading = false,
                            summary = null,
                            errorMessage = result.toQuizStartErrorMessage(),
                        )
                    }
                }
            }
        }
    }
}

private const val QuizStartLoadFailureMessage = "퀴즈 정보를 불러올 수 없어요. 잠시 후 다시 시도해 주세요."

private fun NetworkResult.Failure.toQuizStartErrorMessage(): String {
    val normalizedMessage = message.trim()
    return when {
        normalizedMessage.isBlank() -> QuizStartLoadFailureMessage
        normalizedMessage.equals("timeout", ignoreCase = true) -> QuizStartLoadFailureMessage
        else -> normalizedMessage
    }
}

private fun QuizSession.toSummaryUiModel(): QuizStartSummaryUiModel {
    val choiceLabel = when (quizType) {
        ServerQuizType.MultipleChoice -> "${choiceCount ?: 4}지선다"
        ServerQuizType.Ox -> null
    }

    return QuizStartSummaryUiModel(
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

private fun List<Question>.toScopeLabels(): List<String> {
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
