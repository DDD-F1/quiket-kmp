package com.f1.quiket.composeapp.quiz

import org.koin.compose.koinInject
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.composeapp.designsystem.QuiketBrown100
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray500
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray800
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStep
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizTypeOption
import com.f1.quiket.composeapp.quiz.presentation.icon
import com.f1.quiket.composeapp.subject.domain.model.PartSummary
import com.f1.quiket.composeapp.subject.domain.model.SubjectListItem
import com.f1.quiket.composeapp.util.hidePlatformKeyboard
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.feature.quiz.resources.Res
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_acorn
import com.f1.quiket.core.designsystem.resources.ic_detail_edit
import com.f1.quiket.core.designsystem.resources.ic_qring_profile

internal const val QuizLoadingFullLottieResource = "files/quiz_loading_full.json"
internal val QuizBrown700 = Color(0xFF684C40)
internal val QuizGreen100 = Color(0xFFEFF4D3)
internal val QuizGreen300 = Color(0xFFC8DA7C)
internal val QuizGreen800 = Color(0xFF465420)
internal val QuizBlue100 = Color(0xFFE2F2FC)
internal val QuizBlue300 = Color(0xFF84CFF5)
internal val QuizBlue800 = Color(0xFF0E567E)

@Composable
fun QuizCreateRoute(
    onBackClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onQuizGenerationStarted: () -> Unit = {},
    onQuizGenerationFinished: () -> Unit = {},
    onQuizReady: (QuizPlayLaunchConfig) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<QuizCreateStateHolder>()
    val selectedSubjectId = stateHolder.selectedSubjectId

    fun loadSubjects() {
        coroutineScope.launch {
            stateHolder.loadSubjects(onSessionExpired = onSessionExpired)
        }
    }

    fun createQuiz() {
        coroutineScope.launch {
            stateHolder.createQuiz(
                onSessionExpired = onSessionExpired,
                onQuizGenerationStarted = onQuizGenerationStarted,
                onQuizGenerationFinished = onQuizGenerationFinished,
            )?.let(onQuizReady)
        }
    }

    fun moveBack() {
        if (stateHolder.moveBack()) {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        loadSubjects()
    }

    LaunchedEffect(selectedSubjectId) {
        if (selectedSubjectId != null) {
            stateHolder.loadSelectedSubjectDetail(onSessionExpired = onSessionExpired)
        }
    }

    QuizCreateScreen(
        currentStep = stateHolder.currentStep,
        subjects = stateHolder.subjects,
        subjectDetail = stateHolder.selectedSubjectDetail,
        selectedSubjectId = selectedSubjectId,
        expandedChapterId = stateHolder.expandedChapterId,
        selectedPartIds = stateHolder.selectedPartIds,
        quizType = stateHolder.quizType,
        choiceCount = stateHolder.choiceCount,
        questionCountPreset = stateHolder.questionCountPreset,
        questionCountText = stateHolder.questionCountText,
        difficulty = stateHolder.difficulty,
        isLoadingSubjects = stateHolder.isLoadingSubjects,
        isLoadingDetail = stateHolder.isLoadingDetail,
        isGenerating = stateHolder.isGenerating,
        canBrowseDuringGeneration = stateHolder.canBrowseDuringGeneration,
        generationProgress = stateHolder.generationProgress,
        message = stateHolder.message,
        onBackClick = ::moveBack,
        onRetryClick = ::loadSubjects,
        onAddSubjectClick = onAddSubjectClick,
        onSubjectNextClick = stateHolder::moveToScope,
        onScopeNextClick = stateHolder::moveToOptions,
        onSubjectClick = stateHolder::selectSubject,
        onChapterExpandClick = stateHolder::toggleChapterExpanded,
        onChapterClick = stateHolder::toggleChapterParts,
        onPartClick = stateHolder::togglePart,
        onClearPartsClick = stateHolder::clearParts,
        onQuizTypeClick = stateHolder::selectQuizType,
        onChoiceCountClick = stateHolder::selectChoiceCount,
        onQuestionCountClick = stateHolder::selectQuestionCount,
        onQuestionCountTextChange = stateHolder::updateQuestionCountText,
        onDifficultyClick = stateHolder::selectDifficulty,
        onCreateClick = ::createQuiz,
        modifier = modifier,
    )
}
