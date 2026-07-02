package com.f1.quiket.feature.home.domain.usecase

import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BuildQuizCreateUseCaseTest {
    private val useCase = BuildQuizCreateUseCase()

    @Test
    fun invoke_withValidDraft_buildsDefaultGenerationOptions() {
        val result = useCase(validDraft())

        assertThat(result).isNotNull()
        assertThat(result!!.playMode).isEqualTo(QuizPlayMode.AllAtOnce)
        assertThat(result.timerEnabled).isFalse()
        assertThat(result.timerScope).isNull()
        assertThat(result.timerSeconds).isNull()
    }

    @Test
    fun invoke_withTimerEnabled_includesTimerPolicy() {
        val result = useCase(
            validDraft(
                playMode = QuizPlayMode.OneByOne,
                timerEnabled = true,
                timerScope = QuizTimerScope.PerQuestion,
                timerSeconds = 30,
            ),
        )

        assertThat(result).isNotNull()
        assertThat(result!!.playMode).isEqualTo(QuizPlayMode.OneByOne)
        assertThat(result.timerEnabled).isTrue()
        assertThat(result.timerScope).isEqualTo(QuizTimerScope.PerQuestion)
        assertThat(result.timerSeconds).isEqualTo(30)
    }

    @Test
    fun invoke_withoutRequiredFields_returnsNull() {
        assertThat(useCase(validDraft(subjectId = null))).isNull()
        assertThat(useCase(validDraft(partIds = emptyList()))).isNull()
        assertThat(useCase(validDraft(quizType = null))).isNull()
        assertThat(useCase(validDraft(questionCount = null))).isNull()
        assertThat(useCase(validDraft(difficulty = null))).isNull()
    }

    private fun validDraft(
        subjectId: String? = "subject-1",
        partIds: List<String> = listOf("part-1", "part-2"),
        quizType: ServerQuizType? = ServerQuizType.MultipleChoice,
        choiceCount: Int? = 4,
        questionCount: Int? = 5,
        difficulty: QuizDifficulty? = QuizDifficulty.Medium,
        playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
        timerEnabled: Boolean = false,
        timerScope: QuizTimerScope? = null,
        timerSeconds: Int? = null,
    ): QuizCreateDraft = QuizCreateDraft(
        subjectId = subjectId,
        partIds = partIds,
        quizType = quizType,
        choiceCount = choiceCount,
        questionCount = questionCount,
        difficulty = difficulty,
        playMode = playMode,
        timerEnabled = timerEnabled,
        timerScope = timerScope,
        timerSeconds = timerSeconds,
    )
}
