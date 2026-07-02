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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.feature.floating.presentation.component.RequiredSubjectNameDialog
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.presentation.component.AddSubjectBottomCta
import com.f1.quiket.feature.floating.presentation.component.AddSubjectProgressBar
import com.f1.quiket.feature.floating.presentation.component.AddSubjectTopBar
import com.f1.quiket.feature.floating.presentation.component.PurposeOptionItem
import com.f1.quiket.feature.floating.presentation.component.SkipBottomSheet

@Composable
fun AddSubjectStep1Screen(
    initialSubjectName: String = "",
    existingSubjectNames: List<String> = emptyList(),
    onBackClick: () -> Unit = {},
    onSkipClick: (subjectName: String) -> Unit = {},
    onNextClick: (subjectName: String, purpose: StudyPurpose) -> Unit = { _, _ -> },
) {
    val focusManager = LocalFocusManager.current
    var subjectName by remember { mutableStateOf(initialSubjectName) }
    var selectedPurpose by remember { mutableStateOf<StudyPurpose?>(null) }
    var showSkipSheet by remember { mutableStateOf(false) }
    var showRequiredNameDialog by remember { mutableStateOf(false) }

    val isDuplicate = subjectName.isNotBlank() &&
        existingSubjectNames.any { it.equals(subjectName.trim(), ignoreCase = true) }
    val isNextEnabled = subjectName.isNotBlank() && selectedPurpose != null && !isDuplicate

    Scaffold(containerColor = White, contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            // ── TopBar ──────────────────────────────────────
            AddSubjectTopBar(
                onBackClick = onBackClick,
            )

            // ── ProgressBar (step 1) ────────────────────────
            Spacer(modifier = Modifier.height(4.dp))
            AddSubjectProgressBar(currentStep = 1)
            Spacer(modifier = Modifier.height(28.dp))

            // ── Content ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                // 과목명 라벨
                Row {
                    Text(
                        text = "과목명",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = " *",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Negative,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 과목명 입력
                BaseTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    hint = "과목명을 입력해주세요",
                    modifier = Modifier.fillMaxWidth(),
                    isError = isDuplicate,
                    errorMessage = if (isDuplicate) "이미 있는 과목명입니다." else null,
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (!isDuplicate) {
                    Text(
                        text = "과목명은 나중에 수정할 수 있어요",
                        style = MaterialTheme.typography.labelSmall.copy(color = Gray500),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 학습 목적 라벨
                Text(
                    text = "학습 목적",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudyPurpose.entries.forEach { purpose ->
                        PurposeOptionItem(
                            purpose = purpose,
                            isSelected = selectedPurpose == purpose,
                            onClick = { selectedPurpose = purpose },
                        )
                    }
                }
            }

            // ── Bottom CTA ───────────────────────────────────
            AddSubjectBottomCta(
                buttonText = "다음",
                isEnabled = isNextEnabled,
                onSkipClick = { showSkipSheet = true },
                onNextClick = {
                    if (isNextEnabled) onNextClick(subjectName, selectedPurpose!!)
                },
            )
        }
    }

    // ── Skip BottomSheet ─────────────────────────────────────
    if (showSkipSheet) {
        SkipBottomSheet(
            onDismiss = { showSkipSheet = false },
            onContinue = { showSkipSheet = false },
            onSkip = {
                showSkipSheet = false
                if (subjectName.isBlank()) {
                    showRequiredNameDialog = true
                } else {
                    onSkipClick(subjectName)
                }
            },
        )
    }

    if (showRequiredNameDialog) {
        RequiredSubjectNameDialog(onDismiss = { showRequiredNameDialog = false })
    }
}

@Preview(showBackground = true)
@Composable
private fun AddSubjectStep1Preview() {
    QuiketTheme {
        AddSubjectStep1Screen()
    }
}