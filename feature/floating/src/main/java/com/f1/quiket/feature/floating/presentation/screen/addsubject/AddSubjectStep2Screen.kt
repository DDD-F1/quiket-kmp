package com.f1.quiket.feature.floating.presentation.screen.addsubject

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.UsagePurpose
import com.f1.quiket.feature.floating.presentation.component.AddSubjectBottomCta
import com.f1.quiket.feature.floating.presentation.component.AddSubjectProgressBar
import com.f1.quiket.feature.floating.presentation.component.AddSubjectTopBar
import com.f1.quiket.feature.floating.presentation.component.ExamTypeCard
import com.f1.quiket.feature.floating.presentation.component.PurposeSelectionItem
import com.f1.quiket.feature.floating.presentation.component.SelectionChip
import com.f1.quiket.feature.floating.presentation.component.SkipBottomSheet

@Composable
fun AddSubjectStep2Screen(
    studyPurpose: StudyPurpose,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onNextClick: (selection: Any) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var showSkipSheet by remember { mutableStateOf(false) }

    Scaffold(containerColor = White, contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            AddSubjectTopBar(onBackClick = onBackClick)
            Spacer(modifier = Modifier.height(4.dp))
            AddSubjectProgressBar(currentStep = 2)
            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                when (studyPurpose) {
                    StudyPurpose.EXAM ->
                        ExamTypeSection(
                            onSkipClick = { showSkipSheet = true },
                            onNextClick = onNextClick,
                        )

                    StudyPurpose.SELF_STUDY ->
                        StudyFieldSection(
                            onSkipClick = { showSkipSheet = true },
                            onNextClick = onNextClick,
                        )

                    StudyPurpose.OTHER ->
                        UsagePurposeSection(
                            onSkipClick = { showSkipSheet = true },
                            onNextClick = onNextClick,
                        )
                }
            }
        }
    }

    if (showSkipSheet) {
        SkipBottomSheet(
            onDismiss = { showSkipSheet = false },
            onContinue = { showSkipSheet = false },
            onSkip = { showSkipSheet = false; onSkipClick() },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// EXAM : 시험 유형 6개 카드 그리드
// ──────────────────────────────────────────────────────────────────────────────
private data class ExamTypeItem(val type: ExamType, val icon: Int)

private val examTypeItems = listOf(
    ExamTypeItem(ExamType.UNIVERSITY, R.drawable.ic_addsubject_university),
    ExamTypeItem(ExamType.MIDDLE_HIGH, R.drawable.ic_addsubject_school),
    ExamTypeItem(ExamType.CERTIFICATE, R.drawable.ic_addsubject_certify),
    ExamTypeItem(ExamType.LANGUAGE, R.drawable.ic_addsubject_language),
    ExamTypeItem(ExamType.CIVIL_SERVICE, R.drawable.ic_addsubject_civil),
    ExamTypeItem(ExamType.OTHER_EXAM, R.drawable.ic_addsubject_other),
)

@Composable
private fun ExamTypeSection(
    onSkipClick: () -> Unit,
    onNextClick: (ExamType) -> Unit,
) {
    var selected by remember { mutableStateOf<ExamType?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "어떤 시험을 준비하고 있나요?",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Gray950,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(examTypeItems) { item ->
                ExamTypeCard(
                    label = item.type.label,
                    icon = item.icon,
                    isSelected = selected == item.type,
                    onClick = { selected = item.type },
                )
            }
        }

        AddSubjectBottomCta(
            buttonText = "다음",
            isEnabled = selected != null,
            onSkipClick = onSkipClick,
            onNextClick = { selected?.let { onNextClick(it) } },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// SELF_STUDY : 분야 12개 칩
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun StudyFieldSection(
    onSkipClick: () -> Unit,
    onNextClick: (StudyField) -> Unit,
) {
    var selected by remember { mutableStateOf<StudyField?>(null) }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            Text(
                text = "어떤 분야를 공부하고 있나요?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray950,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            StudyField.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { field ->
                        SelectionChip(
                            label = field.label,
                            isSelected = selected == field,
                            onClick = { selected = field },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    when (row.size) {
                        1 -> { Spacer(modifier = Modifier.weight(1f)); Spacer(modifier = Modifier.weight(1f)) }
                        2 -> Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        AddSubjectBottomCta(
            buttonText = "다음",
            isEnabled = selected != null,
            onSkipClick = onSkipClick,
            onNextClick = { selected?.let { onNextClick(it) } },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// OTHER : 이용 목적 5개 칩
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun UsagePurposeSection(
    onSkipClick: () -> Unit,
    onNextClick: (UsagePurpose) -> Unit,
) {
    var selected by remember { mutableStateOf<UsagePurpose?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "이용 목적에 가장 가까운 항목을 선택해주세요",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Gray950,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UsagePurpose.entries.forEach { purpose ->
                    PurposeSelectionItem(
                        usagePurpose = purpose,
                        isSelected = selected == purpose,
                        onClick = { selected = purpose },
                    )
                }
            }
        }

        AddSubjectBottomCta(
            buttonText = "다음",
            isEnabled = selected != null,
            onSkipClick = onSkipClick,
            onNextClick = { selected?.let { onNextClick(it) } },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
private fun Step2ExamPreview() {
    QuiketTheme { AddSubjectStep2Screen(studyPurpose = StudyPurpose.EXAM) }
}

@Preview(showBackground = true)
@Composable
private fun Step2SelfStudyPreview() {
    QuiketTheme { AddSubjectStep2Screen(studyPurpose = StudyPurpose.SELF_STUDY) }
}

@Preview(showBackground = true)
@Composable
private fun Step2OtherPreview() {
    QuiketTheme { AddSubjectStep2Screen(studyPurpose = StudyPurpose.OTHER) }
}