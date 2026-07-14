package com.f1.quiket.composeapp.quiz.presentation

import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import org.jetbrains.compose.resources.DrawableResource
import com.f1.quiket.feature.quiz.resources.Res
import com.f1.quiket.feature.quiz.resources.ic_quiz_flashcard
import com.f1.quiket.feature.quiz.resources.ic_quiz_multiple
import com.f1.quiket.feature.quiz.resources.ic_quiz_ox
import com.f1.quiket.feature.quiz.resources.ic_quiz_short

internal enum class QuizCreateStep {
    Subject,
    Scope,
    Options,
    Loading,
}

internal enum class QuizTypeOption(
    val title: String,
    val serverType: ServerQuizType?,
    val requiresChoiceCount: Boolean,
) {
    MultipleChoice("객관식", ServerQuizType.MultipleChoice, true),
    Ox("O/X 퀴즈", ServerQuizType.Ox, false),
    Flashcard("플래시카드", null, false),
    ShortAnswer("쪽지시험", null, false),
}

internal val QuizTypeOption.icon: DrawableResource
    get() = when (this) {
        QuizTypeOption.MultipleChoice -> Res.drawable.ic_quiz_multiple
        QuizTypeOption.Ox -> Res.drawable.ic_quiz_ox
        QuizTypeOption.Flashcard -> Res.drawable.ic_quiz_flashcard
        QuizTypeOption.ShortAnswer -> Res.drawable.ic_quiz_short
    }
