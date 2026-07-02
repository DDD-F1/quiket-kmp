package com.f1.quiket.feature.home.presentation.route

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*
import com.f1.quiket.feature.home.presentation.screen.*

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.White
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CreateQuizRoute(
    onBackClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onQuizGenerationStarted: () -> Unit = {},
    onQuizGenerationFinished: () -> Unit = {},
    onQuizCreated: (String) -> Unit = {},
    createQuizBackStackEntry: NavBackStackEntry? = null,
    viewModel: CreateQuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val subjectCreatedFlow = remember(createQuizBackStackEntry) {
        createQuizBackStackEntry?.savedStateHandle?.getStateFlow("subject_created", false)
            ?: MutableStateFlow(false)
    }
    val subjectCreated by subjectCreatedFlow.collectAsStateWithLifecycle()

    fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateQuizEffect.ShowMessage -> showMessage(effect.message)
                CreateQuizEffect.QuizGenerationStarted -> onQuizGenerationStarted()
                CreateQuizEffect.QuizGenerationFinished -> onQuizGenerationFinished()
                is CreateQuizEffect.QuizCreated -> {
                    onQuizGenerationFinished()
                    onQuizCreated(effect.quizSessionId)
                }
            }
        }
    }

    LaunchedEffect(subjectCreated) {
        if (subjectCreated) {
            createQuizBackStackEntry?.savedStateHandle?.set("subject_created", false)
            viewModel.onIntent(CreateQuizIntent.LoadSubjects)
        }
    }

    BackHandler(
        enabled = state.currentStep != CreateQuizStep.Subject &&
            !state.customQuestionCountDialogVisible &&
            state.generationFailureMessage == null,
    ) {
        if (state.currentStep == CreateQuizStep.Loading) {
            if (state.canBrowseDuringGeneration) {
                onBackClick()
            }
        } else {
            viewModel.onIntent(CreateQuizIntent.BackClick)
        }
    }

    when (state.currentStep) {
        CreateQuizStep.Subject -> {
            CreateQuizSubjectScreen(
                subjects = state.subjects,
                selectedSubjectId = state.selectedSubjectId,
                onSubjectClick = { selectedSubject ->
                    viewModel.onIntent(CreateQuizIntent.SelectSubject(selectedSubject.id))
                },
                onAddSubjectClick = onAddSubjectClick,
                onBackClick = onBackClick,
                onNextClick = {
                    viewModel.onIntent(CreateQuizIntent.SubjectNextClick)
                },
            )
        }

        CreateQuizStep.Scope -> {
            state.selectedSubject?.let { subject ->
                CreateQuizScopeScreen(
                    subject = subject,
                    selectedPartIds = state.selectedPartIds.toSet(),
                    expandedChapterId = state.expandedChapterId,
                    onBackClick = {
                        viewModel.onIntent(CreateQuizIntent.ScopeBackClick)
                    },
                    onChapterExpandClick = { chapterId ->
                        viewModel.onIntent(CreateQuizIntent.ToggleChapterExpansion(chapterId))
                    },
                    onChapterSelectionClick = { chapter ->
                        viewModel.onIntent(CreateQuizIntent.ToggleChapterSelection(chapter.id))
                    },
                    onPartClick = { part ->
                        viewModel.onIntent(CreateQuizIntent.TogglePartSelection(part.id))
                    },
                    onClearAllClick = {
                        viewModel.onIntent(CreateQuizIntent.ClearAllParts)
                    },
                    onNextClick = {
                        viewModel.onIntent(CreateQuizIntent.ScopeNextClick)
                    },
                )
            }
        }

        CreateQuizStep.Options -> {
            state.selectedSubject?.let { subject ->
                CreateQuizOptionsScreen(
                    subject = subject,
                    selectedChapterCount = state.selectedChapterCount,
                    selectedPartCount = state.selectedPartCount,
                    selectedQuizType = state.selectedQuizType,
                    selectedChoiceCount = state.selectedChoiceCount,
                    selectedQuestionCountOption = state.selectedQuestionCountOption,
                    customQuestionCount = state.customQuestionCount,
                    selectedDifficulty = state.selectedDifficulty,
                    quizCreationRequested = state.isCreatingQuiz,
                    customQuestionCountDialogVisible = state.customQuestionCountDialogVisible,
                    customQuestionCountText = state.customQuestionCountText,
                    onBackClick = {
                        viewModel.onIntent(CreateQuizIntent.OptionsBackClick)
                    },
                    onQuizTypeClick = { quizType ->
                        viewModel.onIntent(CreateQuizIntent.SelectQuizType(quizType))
                    },
                    onChoiceCountClick = { count ->
                        viewModel.onIntent(CreateQuizIntent.SelectChoiceCount(count))
                    },
                    onQuestionCountClick = { option ->
                        viewModel.onIntent(CreateQuizIntent.SelectQuestionCount(option))
                    },
                    onCustomQuestionCountClick = {
                        viewModel.onIntent(CreateQuizIntent.OpenCustomQuestionCountDialog)
                    },
                    onCustomQuestionCountTextChange = { text ->
                        viewModel.onIntent(CreateQuizIntent.ChangeCustomQuestionCountText(text))
                    },
                    onCustomQuestionCountDismiss = {
                        viewModel.onIntent(CreateQuizIntent.DismissCustomQuestionCountDialog)
                    },
                    onCustomQuestionCountApply = {
                        viewModel.onIntent(CreateQuizIntent.ApplyCustomQuestionCount)
                    },
                    onDifficultyClick = { difficulty ->
                        viewModel.onIntent(CreateQuizIntent.SelectDifficulty(difficulty))
                    },
                    onCreateQuizClick = {
                        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
                    },
                )
            }
        }

        CreateQuizStep.Loading -> {
            CreateQuizLoadingScreen(
                progress = state.generationProgress.coerceIn(0f, 1f),
                rewardCount = state.rewardCount,
                browseEnabled = state.canBrowseDuringGeneration,
                onBrowseClick = onBackClick,
            )
        }
    }

    state.generationFailureMessage?.let { message ->
        CreateQuizGenerationFailureDialog(
            message = message,
            onDismiss = {
                viewModel.onIntent(CreateQuizIntent.DismissGenerationFailure)
            },
        )
    }
}

@Composable
private fun CreateQuizGenerationFailureDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            color = White,
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "퀴즈 생성에 실패했어요",
                    color = Gray950,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = message,
                    color = Gray700,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Start,
                    ),
                )
                QuiketPrimaryButton(
                    text = "확인",
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Suppress("unused")
internal fun createQuizSubjectSamples(): List<QuizSubjectUiModel> = listOf(
    QuizSubjectUiModel(
        id = "sqld",
        name = "SQLD",
        chapters = listOf(
            QuizScopeChapterUiModel(
                id = "sqld-basic",
                title = "SQLD 기본",
                chapterNumber = 1,
                parts = listOf(
                    QuizScopePartUiModel(id = "sqld-basic-overview", title = "SQLD 개요"),
                    QuizScopePartUiModel(id = "sqld-basic-modeling", title = "데이터모델링"),
                    QuizScopePartUiModel(id = "sqld-basic-sql", title = "핵심 SQL 문법"),
                ),
            ),
            QuizScopeChapterUiModel(
                id = "sqld-data-model",
                title = "데이터 모델",
                chapterNumber = 2,
                parts = listOf(
                    QuizScopePartUiModel(id = "sqld-data-model-concept", title = "데이터 모델의 이해"),
                    QuizScopePartUiModel(id = "sqld-data-model-entity", title = "엔터티와 속성"),
                    QuizScopePartUiModel(id = "sqld-data-model-relation", title = "관계와 식별자"),
                    QuizScopePartUiModel(id = "sqld-data-model-performance", title = "모델링과 성능"),
                ),
            ),
            QuizScopeChapterUiModel(
                id = "sqld-usage",
                title = "SQL 활용",
                chapterNumber = 3,
                parts = listOf(
                    QuizScopePartUiModel(id = "sqld-usage-join", title = "JOIN 활용"),
                    QuizScopePartUiModel(id = "sqld-usage-subquery", title = "서브쿼리"),
                ),
            ),
        ),
    ),
    QuizSubjectUiModel(
        id = "figma-workshop",
        name = "기획자의 피그마 실무 워크숍",
        chapters = listOf(
            QuizScopeChapterUiModel(
                id = "figma-basic",
                title = "피그마 기본",
                chapterNumber = 1,
                parts = listOf(
                    QuizScopePartUiModel(id = "figma-basic-interface", title = "인터페이스"),
                    QuizScopePartUiModel(id = "figma-basic-frame", title = "프레임과 오토레이아웃"),
                    QuizScopePartUiModel(id = "figma-basic-component", title = "컴포넌트"),
                ),
            ),
            QuizScopeChapterUiModel(
                id = "figma-prototype",
                title = "프로토타입",
                chapterNumber = 2,
                parts = listOf(
                    QuizScopePartUiModel(id = "figma-prototype-flow", title = "화면 흐름"),
                    QuizScopePartUiModel(id = "figma-prototype-interaction", title = "인터랙션"),
                    QuizScopePartUiModel(id = "figma-prototype-test", title = "테스트"),
                ),
            ),
            QuizScopeChapterUiModel(
                id = "figma-handoff",
                title = "실무 핸드오프",
                chapterNumber = 3,
                parts = listOf(
                    QuizScopePartUiModel(id = "figma-handoff-token", title = "디자인 토큰"),
                    QuizScopePartUiModel(id = "figma-handoff-spec", title = "스펙 정리"),
                    QuizScopePartUiModel(id = "figma-handoff-review", title = "리뷰"),
                ),
            ),
        ),
    ),
    QuizSubjectUiModel(
        id = "western-philosophy",
        name = "서양철학사",
        chapters = listOf(
            QuizScopeChapterUiModel(
                id = "western-philosophy-intro",
                title = "서양철학 입문",
                chapterNumber = 1,
                parts = listOf(
                    QuizScopePartUiModel(id = "western-philosophy-intro-history", title = "철학사의 흐름"),
                ),
            ),
        ),
    ),
)
