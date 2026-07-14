package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.subject.domain.model.*

import org.koin.compose.koinInject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.f1.quiket.composeapp.designsystem.QuiketBrown100
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray500
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray800
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.subject.presentation.CertificateOption
import com.f1.quiket.composeapp.subject.presentation.ChineseTestType
import com.f1.quiket.composeapp.subject.presentation.CivilServantGrade
import com.f1.quiket.composeapp.subject.presentation.CivilServantSeries
import com.f1.quiket.composeapp.subject.presentation.CourseType
import com.f1.quiket.composeapp.subject.presentation.EnglishTestType
import com.f1.quiket.composeapp.subject.presentation.FamiliarityLevel
import com.f1.quiket.composeapp.subject.presentation.JapaneseTestType
import com.f1.quiket.composeapp.subject.presentation.LanguageType
import com.f1.quiket.composeapp.subject.presentation.MiddleHighCurriculum
import com.f1.quiket.composeapp.subject.presentation.MiddleHighSubjectType
import com.f1.quiket.composeapp.subject.presentation.PopularCertificates
import com.f1.quiket.composeapp.subject.presentation.PopularShortCertificates
import com.f1.quiket.composeapp.subject.presentation.StudyField
import com.f1.quiket.composeapp.subject.presentation.SubjectCreateDraft
import com.f1.quiket.composeapp.subject.presentation.SubjectCreateStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectCreateTotalSteps
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailsEditStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectExamType
import com.f1.quiket.composeapp.subject.presentation.UniversityMajorCategory
import com.f1.quiket.composeapp.subject.presentation.UsagePurpose
import com.f1.quiket.composeapp.subject.presentation.icon
import com.f1.quiket.composeapp.subject.presentation.normalizedCertificateName
import com.f1.quiket.composeapp.subject.presentation.toCreateDraft
import com.f1.quiket.composeapp.util.hidePlatformKeyboard
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SubjectCreateRoute(
    onBackClick: () -> Unit,
    onCreated: (CreatedSubject) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf(SubjectCreateDraft()) }
    var currentStep by remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<SubjectCreateStateHolder>()
    val certificates = stateHolder.certificates
    val isLoadingNames = stateHolder.isLoadingNames
    val isLoadingCertificates = stateHolder.isLoadingCertificates
    val isSubmitting = stateHolder.isSubmitting
    val feedbackMessage = stateHolder.feedbackMessage

    val trimmedSubjectName = draft.subjectName.trim()
    val isDuplicate = stateHolder.isDuplicateSubjectName(trimmedSubjectName)
    val subjectNameErrorMessage = when {
        isDuplicate -> "이미 있는 과목명입니다."
        else -> null
    }
    val isNextEnabled = when (currentStep) {
        1 -> trimmedSubjectName.isNotBlank() && draft.studyPurpose != null && !isDuplicate && !isLoadingNames
        2 -> draft.isStep2Complete()
        else -> draft.isReadyToCreate()
    } && !isSubmitting

    LaunchedEffect(stateHolder) {
        stateHolder.loadInitialData(onSessionExpired = onSessionExpired)
    }

    fun createSubject(input: SubjectCreateInput) {
        coroutineScope.launch {
            stateHolder.createSubject(
                input = input,
                onSessionExpired = onSessionExpired,
            )?.let(onCreated)
        }
    }

    fun submit() {
        if (!isNextEnabled) return
        if (currentStep < SubjectCreateTotalSteps) {
            currentStep += 1
            stateHolder.clearFeedback()
            return
        }

        val input = draft.toInput(trimmedSubjectName) ?: return
        createSubject(input)
    }

    fun skip() {
        if (isSubmitting || trimmedSubjectName.isBlank()) return
        createSubject(draft.toSkippedInput(trimmedSubjectName))
    }

    SubjectCreateScreen(
        draft = draft,
        currentStep = currentStep,
        screenTitle = "과목 추가",
        subjectNameErrorMessage = subjectNameErrorMessage,
        feedbackMessage = feedbackMessage,
        certificates = certificates,
        isLoadingNames = isLoadingNames,
        isLoadingCertificates = isLoadingCertificates,
        isNextEnabled = isNextEnabled,
        buttonText = when {
            isSubmitting -> "만드는 중"
            currentStep == SubjectCreateTotalSteps -> "과목 만들기"
            else -> "다음"
        },
        onDraftChange = {
            draft = it
            stateHolder.clearFeedback()
        },
        onBackClick = {
            if (currentStep > 1) {
                currentStep -= 1
                stateHolder.clearFeedback()
            } else {
                onBackClick()
            }
        },
        onNextClick = ::submit,
        onSkipClick = ::skip,
        modifier = modifier,
    )
}

@Composable
internal fun SubjectDetailsEditRoute(
    subject: SubjectDetail,
    onBackClick: () -> Unit,
    onSaved: (CreatedSubject) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(subject.id) { mutableStateOf(subject.toCreateDraft()) }
    var currentStep by remember(subject.id) {
        mutableStateOf(
            if (draft.studyPurpose == SubjectCreatePurpose.Exam && draft.examType != null) {
                SubjectCreateTotalSteps
            } else {
                2
            },
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<SubjectDetailsEditStateHolder>()
    val isSubmitting = stateHolder.isSubmitting
    val feedbackMessage = stateHolder.feedbackMessage
    val isNextEnabled = when (currentStep) {
        2 -> draft.isStep2Complete()
        else -> draft.isReadyToCreate()
    } && !isSubmitting

    LaunchedEffect(subject.id, stateHolder) {
        stateHolder.loadCertificates(onSessionExpired = onSessionExpired)
    }

    fun submit() {
        if (!isNextEnabled) return
        if (currentStep < SubjectCreateTotalSteps) {
            currentStep += 1
            stateHolder.clearFeedback()
            return
        }

        val input = draft.toInput(subject.name.trim()) ?: return
        coroutineScope.launch {
            stateHolder.updateSubjectDetails(
                subjectId = subject.id,
                input = input,
                onSessionExpired = onSessionExpired,
            )?.let(onSaved)
        }
    }

    SubjectCreateScreen(
        draft = draft,
        currentStep = currentStep,
        screenTitle = "과목 유형 수정",
        subjectNameErrorMessage = null,
        feedbackMessage = feedbackMessage,
        certificates = stateHolder.certificates,
        isLoadingNames = false,
        isLoadingCertificates = stateHolder.isLoadingCertificates,
        isNextEnabled = isNextEnabled,
        buttonText = when {
            isSubmitting -> "저장 중"
            currentStep == SubjectCreateTotalSteps -> "저장"
            else -> "다음"
        },
        onDraftChange = {
            draft = it
            stateHolder.clearFeedback()
        },
        onBackClick = {
            if (currentStep > 2) {
                currentStep -= 1
                stateHolder.clearFeedback()
            } else {
                onBackClick()
            }
        },
        onNextClick = ::submit,
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun SubjectCreateScreen(
    draft: SubjectCreateDraft,
    currentStep: Int,
    screenTitle: String,
    subjectNameErrorMessage: String?,
    feedbackMessage: String?,
    certificates: List<Certificate>,
    isLoadingNames: Boolean,
    isLoadingCertificates: Boolean,
    isNextEnabled: Boolean,
    buttonText: String,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showSkipSheet by remember { mutableStateOf(false) }
    var showRequiredNameDialog by remember { mutableStateOf(false) }

    fun hideKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        hidePlatformKeyboard()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 124.dp, bottom = 132.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            SubjectCreateProgress(
                currentStep = currentStep,
                totalSteps = SubjectCreateTotalSteps,
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (currentStep) {
                1 -> SubjectCreateStepOne(
                    draft = draft,
                    subjectNameErrorMessage = subjectNameErrorMessage,
                    isLoadingNames = isLoadingNames,
                    onDraftChange = onDraftChange,
                    onKeyboardDismiss = ::hideKeyboard,
                )
                2 -> SubjectCreateStepTwo(
                    draft = draft,
                    onDraftChange = onDraftChange,
                    onKeyboardDismiss = ::hideKeyboard,
                )
                else -> SubjectCreateStepThree(
                    draft = draft,
                    certificates = certificates,
                    isLoadingCertificates = isLoadingCertificates,
                    onDraftChange = onDraftChange,
                    onKeyboardDismiss = ::hideKeyboard,
                )
            }

            feedbackMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    color = QuiketNegative,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }

        SubjectCreateTopBar(
            title = screenTitle,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        SubjectCreateBottomCta(
            buttonText = buttonText,
            isEnabled = isNextEnabled,
            showSkip = onSkipClick != null,
            onSkipClick = {
                hideKeyboard()
                showSkipSheet = true
            },
            onNextClick = {
                hideKeyboard()
                onNextClick()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding(),
        )
    }

    if (showSkipSheet) {
        SubjectCreateSkipSheet(
            onDismiss = { showSkipSheet = false },
            onContinue = { showSkipSheet = false },
            onSkip = {
                showSkipSheet = false
                if (draft.subjectName.isBlank()) {
                    showRequiredNameDialog = true
                } else {
                    onSkipClick?.invoke()
                }
            },
        )
    }

    if (showRequiredNameDialog) {
        RequiredSubjectNameDialog(onDismiss = { showRequiredNameDialog = false })
    }
}

@Composable
private fun SubjectCreateBottomCta(
    buttonText: String,
    isEnabled: Boolean,
    showSkip: Boolean,
    onSkipClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(QuiketWhite)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showSkip) {
            Row(
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onSkipClick)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "지금은 건너뛰기",
                    color = QuiketGray500,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.width(4.dp))
                SmallChevronRight(color = QuiketGray500)
            }
        }
        QuiketPrimaryButton(
            text = buttonText,
            enabled = isEnabled,
            onClick = onNextClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectCreateSkipSheet(
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = QuiketWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .semantics { contentDescription = "닫기" }
                        .clickable(role = Role.Button, onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    CloseIcon(color = QuiketGray700)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "잠깐! 더 정확한 문제를 받아볼 수 있어요",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "목적과 유형 등을 설정하면 내 과목에 맞는 문제를\n더 정확하게 만들어드려요!",
                color = QuiketGray800,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(QuiketGray50),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Quiket",
                    color = QuiketGray300,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuiketPrimaryButton(
                text = "계속 작성할게요",
                enabled = true,
                onClick = onContinue,
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuiketPrimaryButton(
                text = "지금은 넘어갈게요",
                enabled = true,
                onClick = onSkip,
                containerColor = QuiketWhite,
                contentColor = QuiketGray950,
                modifier = Modifier.border(2.dp, QuiketBrown950, RoundedCornerShape(12.dp)),
            )
        }
    }
}

@Composable
private fun RequiredSubjectNameDialog(
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = QuiketWhite,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(QuiketGray100)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "!",
                        color = QuiketGray700,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "과목명을 입력해주세요",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "과목명은 필수로 입력해야만 해요. \n과목명을 적어주세요!",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                QuiketPrimaryButton(
                    text = "확인",
                    enabled = true,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun SubjectCreateStepOne(
    draft: SubjectCreateDraft,
    subjectNameErrorMessage: String?,
    isLoadingNames: Boolean,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RequiredLabel(text = "과목명")
            QuiketTextField(
                value = draft.subjectName,
                onValueChange = { onDraftChange(draft.copy(subjectName = it)) },
                hint = "과목명을 입력해주세요",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onKeyboardDismiss() }),
                isError = !subjectNameErrorMessage.isNullOrBlank(),
                errorMessage = subjectNameErrorMessage,
                modifier = Modifier.fillMaxWidth(),
            )
            if (subjectNameErrorMessage == null) {
                Text(
                    text = if (isLoadingNames) "기존 과목명을 확인하고 있어요" else "과목명은 나중에 수정할 수 있어요",
                    color = QuiketGray400,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PlainLabel(text = "학습 목적")
            SubjectCreatePurpose.entries.forEach { purpose ->
                PurposeOptionCard(
                    purpose = purpose,
                    selected = draft.studyPurpose == purpose,
                    onClick = {
                        onKeyboardDismiss()
                        onDraftChange(draft.withPurpose(purpose))
                    },
                )
            }
        }
    }
}

@Composable
private fun SubjectCreateStepTwo(
    draft: SubjectCreateDraft,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    when (draft.studyPurpose) {
        SubjectCreatePurpose.Exam -> {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlainLabel(text = "어떤 시험을 준비하고 있나요?")
                ExamTypeGrid(
                    selected = draft.examType,
                    onSelect = {
                        onKeyboardDismiss()
                        onDraftChange(draft.withExamType(it))
                    },
                )
            }
        }
        SubjectCreatePurpose.Review -> {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlainLabel(text = "어떤 분야를 공부하고 있나요?")
                Step2ChoiceGrid(
                    items = StudyField.entries,
                    selected = draft.studyField,
                    label = { it.label },
                    onSelect = { onDraftChange(draft.copy(studyField = it, familiarityLevel = null)) },
                    onBeforeSelect = onKeyboardDismiss,
                    columns = 3,
                )
            }
        }
        SubjectCreatePurpose.Other -> {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlainLabel(text = "이용 목적에 가장 가까운 항목을 선택해주세요")
                UsagePurpose.entries.forEach { purpose ->
                    Step2PurposeCard(
                        title = purpose.label,
                        body = purpose.description,
                        selected = draft.usagePurpose == purpose,
                        onClick = {
                            onKeyboardDismiss()
                            onDraftChange(draft.copy(usagePurpose = purpose, additionalDescription = ""))
                        },
                    )
                }
            }
        }
        null -> Text(
            text = "학습 목적을 먼저 선택해주세요.",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ExamTypeGrid(
    selected: SubjectExamType?,
    onSelect: (SubjectExamType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SubjectExamType.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { examType ->
                    ExamTypeCard(
                        label = examType.label,
                        icon = examType.icon,
                        selected = selected == examType,
                        onClick = { onSelect(examType) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExamTypeCard(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) QuiketBrown950 else QuiketGray50,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) QuiketBrown100 else QuiketGray100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun <T> Step2ChoiceGrid(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    onBeforeSelect: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    Step2ChoiceChip(
                        text = label(item),
                        selected = selected == item,
                        onClick = {
                            onBeforeSelect()
                            onSelect(item)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Step2ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun Step2PurposeCard(
    title: String,
    body: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) QuiketBrown950 else QuiketGray50,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                color = QuiketGray600,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun SubjectCreateStepThree(
    draft: SubjectCreateDraft,
    certificates: List<Certificate>,
    isLoadingCertificates: Boolean,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    when (draft.studyPurpose) {
        SubjectCreatePurpose.Exam -> ExamDetailSection(
            draft = draft,
            certificates = certificates,
            isLoadingCertificates = isLoadingCertificates,
            onDraftChange = onDraftChange,
            onKeyboardDismiss = onKeyboardDismiss,
        )
        SubjectCreatePurpose.Review -> ReviewDetailSection(
            draft = draft,
            onDraftChange = onDraftChange,
            onKeyboardDismiss = onKeyboardDismiss,
        )
        SubjectCreatePurpose.Other -> OtherDetailSection(
            draft = draft,
            onDraftChange = onDraftChange,
            onKeyboardDismiss = onKeyboardDismiss,
        )
        null -> Text(
            text = "학습 목적을 먼저 선택해주세요.",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ExamDetailSection(
    draft: SubjectCreateDraft,
    certificates: List<Certificate>,
    isLoadingCertificates: Boolean,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    when (draft.examType) {
        SubjectExamType.University -> Column {
            SectionTitle(text = "전공 계열")
            Spacer(modifier = Modifier.height(10.dp))
            Step2ChoiceGrid(
                items = UniversityMajorCategory.entries,
                selected = draft.majorCategory,
                label = { it.label },
                onSelect = { onDraftChange(draft.copy(majorCategory = it)) },
                onBeforeSelect = onKeyboardDismiss,
                columns = 2,
            )
            Spacer(modifier = Modifier.height(20.dp))
            TextInputSection(
                title = "전공명",
                value = draft.majorName,
                hint = "전공명을 입력해주세요",
                onValueChange = { onDraftChange(draft.copy(majorName = it.take(30))) },
                onKeyboardDismiss = onKeyboardDismiss,
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(text = "이 과목은 어떤 유형의 수강 과목인가요?")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CourseType.entries.forEach { type ->
                    Step2ChoiceChip(
                        text = type.label,
                        selected = draft.courseType == type,
                        onClick = {
                            onKeyboardDismiss()
                            onDraftChange(draft.copy(courseType = type))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        SubjectExamType.MiddleHigh -> Column {
            SectionTitle(text = "교육 과정")
            Spacer(modifier = Modifier.height(10.dp))
            Step2ChoiceGrid(
                items = MiddleHighCurriculum.entries,
                selected = draft.curriculum,
                label = { it.label },
                onSelect = { onDraftChange(draft.copy(curriculum = it)) },
                onBeforeSelect = onKeyboardDismiss,
                columns = 2,
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(text = "과목 유형")
            Spacer(modifier = Modifier.height(10.dp))
            Step2ChoiceGrid(
                items = MiddleHighSubjectType.entries,
                selected = draft.subjectType,
                label = {
                    if (it == MiddleHighSubjectType.Custom &&
                        draft.subjectType == it &&
                        draft.customSubjectType.isNotBlank()
                    ) {
                        draft.customSubjectType
                    } else {
                        it.label
                    }
                },
                onSelect = { onDraftChange(draft.copy(subjectType = it)) },
                onBeforeSelect = onKeyboardDismiss,
                columns = 2,
            )
            if (draft.subjectType == MiddleHighSubjectType.Custom) {
                Spacer(modifier = Modifier.height(20.dp))
                TextInputSection(
                    title = "과목 유형 직접 입력",
                    value = draft.customSubjectType,
                    hint = "과목 유형을 입력해주세요",
                    onValueChange = { onDraftChange(draft.copy(customSubjectType = it.take(20))) },
                    onKeyboardDismiss = onKeyboardDismiss,
                )
            }
        }

        SubjectExamType.Certificate -> CertificateSearchSection(
            draft = draft,
            certificates = certificates,
            isLoadingCertificates = isLoadingCertificates,
            onDraftChange = onDraftChange,
            onKeyboardDismiss = onKeyboardDismiss,
        )

        SubjectExamType.Language -> Column {
            Step3SectionLabel(text = "언어 종류")
            Spacer(modifier = Modifier.height(10.dp))
            ChoiceGrid(
                items = LanguageType.entries,
                selected = draft.languageType,
                label = { it.label },
                onSelect = {
                    onDraftChange(
                        draft.copy(
                            languageType = it,
                            englishTest = null,
                            japaneseTest = null,
                            chineseTest = null,
                            customLanguageTest = "",
                        ),
                    )
                },
                onBeforeSelect = onKeyboardDismiss,
                columns = 3,
            )
            draft.languageType?.let { language ->
                Spacer(modifier = Modifier.height(20.dp))
                Step3SectionLabel(text = "시험 유형")
                Spacer(modifier = Modifier.height(10.dp))
                when (language) {
                    LanguageType.English -> ChoiceGrid(
                        items = EnglishTestType.entries,
                        selected = draft.englishTest,
                        label = { it.label },
                        onSelect = { onDraftChange(draft.copy(englishTest = it)) },
                        onBeforeSelect = onKeyboardDismiss,
                        columns = 2,
                    )
                    LanguageType.Japanese -> ChoiceGrid(
                        items = JapaneseTestType.entries,
                        selected = draft.japaneseTest,
                        label = { it.label },
                        onSelect = { onDraftChange(draft.copy(japaneseTest = it)) },
                        onBeforeSelect = onKeyboardDismiss,
                    )
                    LanguageType.Chinese -> ChoiceGrid(
                        items = ChineseTestType.entries,
                        selected = draft.chineseTest,
                        label = { it.label },
                        onSelect = { onDraftChange(draft.copy(chineseTest = it)) },
                        onBeforeSelect = onKeyboardDismiss,
                    )
                }
            }
            if (draft.needsCustomLanguageTest()) {
                Spacer(modifier = Modifier.height(20.dp))
                TextInputSection(
                    title = "시험 유형 직접 입력",
                    value = draft.customLanguageTest,
                    hint = "시험 유형을 입력해주세요",
                    onValueChange = { onDraftChange(draft.copy(customLanguageTest = it.take(30))) },
                    onKeyboardDismiss = onKeyboardDismiss,
                )
            }
        }

        SubjectExamType.CivilService -> Column {
            Step3SectionLabel(text = "급수")
            Spacer(modifier = Modifier.height(10.dp))
            ChoiceGrid(
                items = CivilServantGrade.entries,
                selected = draft.civilServantGrade,
                label = { it.label },
                onSelect = { onDraftChange(draft.copy(civilServantGrade = it)) },
                onBeforeSelect = onKeyboardDismiss,
                columns = 2,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Step3SectionLabel(text = "직렬")
            Spacer(modifier = Modifier.height(10.dp))
            ChoiceGrid(
                items = CivilServantSeries.entries,
                selected = draft.civilServantSeries,
                label = { it.label },
                onSelect = { onDraftChange(draft.copy(civilServantSeries = it)) },
                onBeforeSelect = onKeyboardDismiss,
                columns = 3,
            )
        }

        SubjectExamType.OtherExam -> Column {
            SectionTitle(text = "준비 중인 시험이나 자격증을 입력해주세요")
            Spacer(modifier = Modifier.height(12.dp))
            QuiketTextField(
                value = draft.otherExamText,
                onValueChange = { onDraftChange(draft.copy(otherExamText = it.take(30))) },
                hint = "최대 30자 이내로 입력해주세요",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onKeyboardDismiss() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${draft.otherExamText.length}/30",
                color = QuiketGray500,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }

        null -> Text(
            text = "시험 유형을 먼저 선택해주세요.",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CertificateSearchSection(
    draft: SubjectCreateDraft,
    certificates: List<Certificate>,
    isLoadingCertificates: Boolean,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    var query by remember(draft.examType) { mutableStateOf(draft.certificateName) }
    val certificateOptions = remember(certificates) {
        certificates
            .sortedWith(compareByDescending<Certificate> { it.featured }
                .thenBy { it.displayOrder }
                .thenBy { it.name })
            .map { certificate ->
                CertificateOption(
                    id = certificate.id.toString(),
                    name = certificate.name,
                    featured = certificate.featured,
                    displayOrder = certificate.displayOrder,
                )
            }
            .plus(PopularCertificates.mapIndexed { index, name ->
                CertificateOption(
                    id = null,
                    name = name,
                    featured = false,
                    displayOrder = Int.MAX_VALUE - PopularCertificates.size + index,
                )
            })
            .distinctBy { it.name.normalizedCertificateName() }
    }
    val featuredCertificateOptions = remember(certificateOptions) {
        certificateOptions
            .filter { it.featured }
            .take(3)
            .ifEmpty {
                PopularShortCertificates.mapIndexed { index, name ->
                    CertificateOption(
                        id = null,
                        name = name,
                        featured = false,
                        displayOrder = index,
                    )
                }
            }
    }
    var isDirectInput by remember(draft.examType, certificateOptions) {
        mutableStateOf(
            draft.certificateName.isNotBlank() &&
                certificateOptions.none {
                    it.name.equals(draft.certificateName, ignoreCase = true)
                },
        )
    }
    val trimmedQuery = query.trim()
    val filteredCertificates = remember(trimmedQuery, certificateOptions) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            certificateOptions.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
        }
    }
    val canDirectAdd = trimmedQuery.isNotBlank() && filteredCertificates.isEmpty()

    Column {
        Step3SectionLabel(text = "자격증 명")
        Spacer(modifier = Modifier.height(8.dp))
        if (isLoadingCertificates) {
            Text(
                text = "자격증 목록을 불러오고 있어요",
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        QuiketTextField(
            value = query,
            onValueChange = { value ->
                query = value.take(40)
                isDirectInput = false
                onDraftChange(draft.copy(certificateId = null, certificateName = ""))
            },
            hint = "자격증명을 검색해주세요",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onKeyboardDismiss() }),
            modifier = Modifier.fillMaxWidth(),
        )

        if (filteredCertificates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CertificateResultList(
                certificates = filteredCertificates,
                onSelect = { certificate ->
                    onKeyboardDismiss()
                    query = certificate.name
                    isDirectInput = false
                    onDraftChange(
                        draft.copy(
                            certificateId = certificate.id,
                            certificateName = certificate.name,
                        ),
                    )
                },
            )
        }

        if (canDirectAdd) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(QuiketGray100)
                    .clickable(role = Role.Button) {
                        onKeyboardDismiss()
                        query = trimmedQuery
                        isDirectInput = true
                        onDraftChange(draft.copy(certificateId = null, certificateName = trimmedQuery))
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "+",
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "\"$trimmedQuery\" 직접 추가하기",
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        if (!isDirectInput) {
            Spacer(modifier = Modifier.height(20.dp))
            Step3SupportLabel(text = "자주 찾는 자격증")
            Spacer(modifier = Modifier.height(8.dp))
            ChoiceGrid(
                items = featuredCertificateOptions,
                selected = featuredCertificateOptions.firstOrNull {
                    it.name.equals(draft.certificateName, ignoreCase = true)
                },
                label = { it.name },
                onSelect = { certificate ->
                    query = certificate.name
                    onDraftChange(
                        draft.copy(
                            certificateId = certificate.id,
                            certificateName = certificate.name,
                        ),
                    )
                },
                onBeforeSelect = onKeyboardDismiss,
                columns = 3,
            )
        }
    }
}

@Composable
private fun CertificateResultList(
    certificates: List<CertificateOption>,
    onSelect: (CertificateOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, QuiketGray300, RoundedCornerShape(8.dp))
            .background(QuiketWhite),
    ) {
        certificates.forEachIndexed { index, certificate ->
            Text(
                text = certificate.name,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onSelect(certificate) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            )
            if (index != certificates.lastIndex) {
                HorizontalDivider(color = QuiketGray100)
            }
        }
    }
}

@Composable
private fun ReviewDetailSection(
    draft: SubjectCreateDraft,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    Column {
        SectionTitle(text = "이 과목에 얼마나 익숙하신가요?")
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FamiliarityLevel.entries.forEach { level ->
                ChoiceCard(
                    title = level.label,
                    body = level.description,
                    selected = draft.familiarityLevel == level,
                    onClick = {
                        onKeyboardDismiss()
                        onDraftChange(draft.copy(familiarityLevel = level))
                    },
                )
            }
        }
    }
}

@Composable
private fun OtherDetailSection(
    draft: SubjectCreateDraft,
    onDraftChange: (SubjectCreateDraft) -> Unit,
    onKeyboardDismiss: () -> Unit,
) {
    Column {
        SectionTitle(text = "추가로 설명해주시면 더 잘 도와드릴 수 있어요")
        Spacer(modifier = Modifier.height(12.dp))
        TextInputSection(
            title = "",
            value = draft.additionalDescription,
            hint = "자유롭게 입력해주세요",
            onValueChange = { onDraftChange(draft.copy(additionalDescription = it)) },
            onKeyboardDismiss = onKeyboardDismiss,
            required = false,
        )
    }
}

@Composable
private fun TextInputSection(
    title: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    onKeyboardDismiss: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title.isNotBlank()) {
            if (required) {
                RequiredLabel(text = title)
            } else {
                SectionTitle(text = title)
            }
        }
        QuiketTextField(
            value = value,
            onValueChange = onValueChange,
            hint = hint,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onKeyboardDismiss() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        modifier = modifier,
    )
}

@Composable
private fun Step3SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier,
    )
}

@Composable
private fun Step3SupportLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray700,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier,
    )
}

@Composable
private fun <T> ChoiceGrid(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    onBeforeSelect: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    ChoiceChip(
                        text = label(item),
                        selected = selected == item,
                        onClick = {
                            onBeforeSelect()
                            onSelect(item)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    body: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = title,
                color = if (selected) QuiketBrown950 else QuiketGray700,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                color = QuiketGray500,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun SubjectCreateTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(QuiketWhite),
    ) {
        SubjectCreateBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 50.dp),
        )
        Text(
            text = title,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp),
        )
    }
}

@Composable
private fun SubjectCreateProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (index < currentStep) QuiketBrown950 else QuiketGray100),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$currentStep/$totalSteps",
            color = QuiketGray900,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SmallChevronRight(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.2f),
            end = Offset(size.width * 0.65f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.5f),
            end = Offset(size.width * 0.35f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun CloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.28f),
            end = Offset(size.width * 0.72f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height * 0.28f),
            end = Offset(size.width * 0.28f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun RequiredLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(
            text = text,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = " *",
            color = QuiketNegative,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun PlainLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        modifier = modifier,
    )
}

@Composable
private fun PurposeOptionCard(
    purpose: SubjectCreatePurpose,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) QuiketBrown950 else QuiketGray50,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = purpose.title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = purpose.description,
                color = QuiketGray600,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun SubjectCreateBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .semantics { contentDescription = "뒤로" }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = QuiketGray700,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray700,
                start = Offset(size.width * 0.32f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
