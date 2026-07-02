package com.f1.quiket.feature.floating.presentation.screen.addsubject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.domain.model.AddSubjectState
import com.f1.quiket.feature.floating.domain.model.ChineseTestType
import com.f1.quiket.feature.floating.domain.model.CivilServantGrade
import com.f1.quiket.feature.floating.domain.model.CivilServantSeries
import com.f1.quiket.feature.floating.domain.model.CourseType
import com.f1.quiket.feature.floating.domain.model.EnglishTestType
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.FamiliarityLevel
import com.f1.quiket.feature.floating.domain.model.JapaneseTestType
import com.f1.quiket.feature.floating.domain.model.LanguageType
import com.f1.quiket.feature.floating.domain.model.MiddleHighCurriculum
import com.f1.quiket.feature.floating.domain.model.MiddleHighSubjectType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.UniversityMajorCategory
import com.f1.quiket.feature.floating.domain.model.UsagePurpose
import com.f1.quiket.feature.floating.presentation.component.AddSubjectBottomCta
import com.f1.quiket.feature.floating.presentation.component.AddSubjectProgressBar
import com.f1.quiket.feature.floating.presentation.component.AddSubjectTopBar
import com.f1.quiket.feature.floating.presentation.component.SelectionChip
import com.f1.quiket.feature.floating.presentation.component.SkipBottomSheet

// 샘플 자격증 목록
private val popularCertificates = listOf(
    "정보처리기사", "컴퓨터활용능력 1급", "컴퓨터활용능력 2급",
    "한국사능력검정시험", "영어회화전문가", "사회조사분석사",
    "빅데이터분석기사", "네트워크관리사",
)

private val popularShortCerts = listOf("정보처리기사", "컴퓨터활용능력", "한국사능력검정")

/**
 * 3depth : 시험 유형(ExamType) 또는 SELF_STUDY/OTHER 목적에 따라 분기
 */
@Composable
fun AddSubjectStep3Screen(
    studyPurpose: StudyPurpose,
    examType: ExamType? = null,
    studyField: StudyField? = null,
    usagePurpose: UsagePurpose? = null,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit = { _, _ -> },
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
            AddSubjectProgressBar(currentStep = 3)
            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                when (studyPurpose) {
                    StudyPurpose.EXAM -> when (examType) {
                        ExamType.UNIVERSITY -> UniversitySection(
                            onSkipClick = { showSkipSheet = true },
                            onCreateClick = onCreateClick,
                        )
                        ExamType.MIDDLE_HIGH -> MiddleHighSection(
                            onSkipClick = { showSkipSheet = true },
                            onCreateClick = onCreateClick,
                        )
                        ExamType.CERTIFICATE -> CertificateSection(
                            onSkipClick = { showSkipSheet = true },
                            onCreateClick = onCreateClick,
                        )
                        ExamType.LANGUAGE -> LanguageSection(
                            onSkipClick = { showSkipSheet = true },
                            onCreateClick = onCreateClick,
                        )
                        ExamType.CIVIL_SERVICE -> CivilServantSection(
                            onSkipClick = { showSkipSheet = true },
                            onCreateClick = onCreateClick,
                        )
                        ExamType.OTHER_EXAM, null -> ExamOtherSection(
                            onSkipClick = { showSkipSheet = true },
                            onCreateClick = onCreateClick,
                        )
                    }

                    StudyPurpose.SELF_STUDY -> FamiliaritySection(
                        onSkipClick = { showSkipSheet = true },
                        onCreateClick = onCreateClick,
                    )
                    StudyPurpose.OTHER -> AdditionalDescriptionSection(
                        onSkipClick = { showSkipSheet = true },
                        onCreateClick = onCreateClick,
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
// (1) 대학시험 : 전공계열 8개 + 전공명 입력 + 과목유형(전공/교양)
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun UniversitySection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var selectedMajor by remember { mutableStateOf<UniversityMajorCategory?>(null) }
    var majorName by remember { mutableStateOf("") }
    var selectedCourseType by remember { mutableStateOf<CourseType?>(null) }

    val isEnabled = true

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionLabel("전공 계열")
            Spacer(modifier = Modifier.height(10.dp))
            ChipGrid2Col(
                items = UniversityMajorCategory.entries.map { it.label },
                selectedIndex = selectedMajor?.ordinal,
                onSelect = { selectedMajor = UniversityMajorCategory.entries[it] },
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("전공명")
            Spacer(modifier = Modifier.height(8.dp))
            BaseTextField(
                value = majorName,
                onValueChange = { majorName = it },
                hint = "전공명을 입력해주세요",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("이 과목은 어떤 유형의 수강 과목인가요?")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CourseType.entries.forEach { type ->
                    SelectionChip(
                        label = type.label,
                        isSelected = selectedCourseType == type,
                        onClick = { selectedCourseType = type },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val major = selectedMajor
                val course = selectedCourseType
                val name = majorName
                val label = listOfNotNull(
                    major?.label,
                    name.ifBlank { null },
                    course?.label,
                ).joinToString(" · ")
                onCreateClick(label) { state ->
                    state.copy(majorCategory = major, majorName = name, courseType = course)
                }
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// (2) 중고등시험 : 교육과정 6개 + 과목 유형 9개(마지막은 직접입력)
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun MiddleHighSection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var selectedCurriculum by remember { mutableStateOf<MiddleHighCurriculum?>(null) }
    var selectedSubjectType by remember { mutableStateOf<MiddleHighSubjectType?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customSubjectType by remember { mutableStateOf("") }

    val isEnabled =
        selectedCurriculum != null &&
                (selectedSubjectType != null && selectedSubjectType != MiddleHighSubjectType.CUSTOM ||
                        (selectedSubjectType == MiddleHighSubjectType.CUSTOM && customSubjectType.isNotBlank()))

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionLabel("교육 과정")
            Spacer(modifier = Modifier.height(10.dp))
            ChipGrid2Col(
                items = MiddleHighCurriculum.entries.map { it.label },
                selectedIndex = selectedCurriculum?.ordinal,
                onSelect = { selectedCurriculum = MiddleHighCurriculum.entries[it] },
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("과목 유형")
            Spacer(modifier = Modifier.height(10.dp))
            ChipGrid2Col(
                items = MiddleHighSubjectType.entries.map {
                    if (it == MiddleHighSubjectType.CUSTOM && selectedSubjectType == it && customSubjectType.isNotBlank())
                        customSubjectType
                    else it.label
                },
                selectedIndex = selectedSubjectType?.ordinal,
                onSelect = { idx ->
                    val type = MiddleHighSubjectType.entries[idx]
                    if (type == MiddleHighSubjectType.CUSTOM) showCustomDialog = true
                    else selectedSubjectType = type
                },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val subjectTypeLabel = customSubjectType.ifBlank { selectedSubjectType?.label ?: "" }
                val label = "${selectedCurriculum?.label} · $subjectTypeLabel"
                val curriculum = selectedCurriculum
                val subjectType = selectedSubjectType
                val customType = customSubjectType
                onCreateClick(label) { state ->
                    state.copy(curriculum = curriculum, subjectType = subjectType, customSubjectType = customType)
                }
            },
        )
    }

    if (showCustomDialog) {
        CustomInputDialog(
            title = "과목 유형 직접 입력",
            hint = "과목 유형을 입력해주세요",
            value = customSubjectType,
            onValueChange = { customSubjectType = it },
            onCancel = { showCustomDialog = false },
            onApply = { selectedSubjectType = MiddleHighSubjectType.CUSTOM; showCustomDialog = false },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// (3) 자격증 : 자격증명 검색 + 직접 추가 + 자주 찾는 자격증
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun CertificateSection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedCert by remember { mutableStateOf("") }
    var isDirectInput by remember { mutableStateOf(false) }

    val filtered = if (query.isNotBlank()) popularCertificates.filter { it.contains(query) } else emptyList()
    val isEnabled = selectedCert.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionLabel("자격증 명")
            Spacer(modifier = Modifier.height(8.dp))

            BaseTextField(
                value = query,
                onValueChange = {
                    query = it
                    selectedCert = ""
                    isDirectInput = false
                },
                hint = "자격증명을 검색해주세요",
                modifier = Modifier.fillMaxWidth(),
            )

            // 검색 결과 목록
            if (filtered.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Gray300, RoundedCornerShape(8.dp))
                        .background(White),
                ) {
                    filtered.forEach { cert ->
                        Text(
                            text = cert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCert = cert; query = cert }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(color = Gray700),
                        )
                        HorizontalDivider(color = Gray100)
                    }
                }
            }

            // 직접 추가하기 (검색어가 있고 일치 결과 없을 때)
            if (query.isNotBlank() && filtered.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray100)
                        .clickable {
                            selectedCert = query
                            isDirectInput = true
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_addsubject_add),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Brown950,
                    )
                    Text(
                        text = "\"$query\" 직접 추가하기",
                        style = MaterialTheme.typography.bodySmall.copy(color = Brown950),
                    )
                }
            }

            // 자주 찾는 자격증 (직접 입력 중이 아닐 때만 표시)
            if (!isDirectInput) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "자주 찾는 자격증",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray700,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    popularShortCerts.forEach { cert ->
                        SelectionChip(
                            label = cert,
                            isSelected = selectedCert == cert,
                            onClick = { selectedCert = cert; query = cert },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val cert = selectedCert
                onCreateClick(cert) { state -> state.copy(certificateName = cert) }
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// (4) 어학 : 언어 선택 -> 시험 유형 선택
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun LanguageSection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var selectedLanguage by remember { mutableStateOf<LanguageType?>(null) }
    var selectedEnglishTest by remember { mutableStateOf<EnglishTestType?>(null) }
    var selectedJapaneseTest by remember { mutableStateOf<JapaneseTestType?>(null) }
    var selectedChineseTest by remember { mutableStateOf<ChineseTestType?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customTestName by remember { mutableStateOf("") }

    val isEnabled = selectedLanguage != null && when (selectedLanguage) {
        LanguageType.ENGLISH -> selectedEnglishTest != null &&
                (selectedEnglishTest != EnglishTestType.CUSTOM || customTestName.isNotBlank())
        LanguageType.JAPANESE -> selectedJapaneseTest != null &&
                (selectedJapaneseTest != JapaneseTestType.CUSTOM || customTestName.isNotBlank())
        LanguageType.CHINESE -> selectedChineseTest != null &&
                (selectedChineseTest != ChineseTestType.CUSTOM || customTestName.isNotBlank())
        null -> false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionLabel("언어 종류")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageType.entries.forEach { lang ->
                    SelectionChip(
                        label = lang.label,
                        isSelected = selectedLanguage == lang,
                        onClick = {
                            selectedLanguage = lang
                            selectedEnglishTest = null
                            selectedJapaneseTest = null
                            selectedChineseTest = null
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (selectedLanguage != null) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("시험 유형")
                Spacer(modifier = Modifier.height(10.dp))

                when (selectedLanguage) {
                    LanguageType.ENGLISH -> ChipGrid2Col(
                        items = EnglishTestType.entries.map {
                            if (it == EnglishTestType.CUSTOM && selectedEnglishTest == it && customTestName.isNotBlank()) customTestName else it.label
                        },
                        selectedIndex = selectedEnglishTest?.ordinal,
                        onSelect = {
                            val t = EnglishTestType.entries[it]
                            if (t == EnglishTestType.CUSTOM) showCustomDialog = true else selectedEnglishTest = t
                        },
                    )
                    LanguageType.JAPANESE -> ChipGrid2Col(
                        items = JapaneseTestType.entries.map {
                            if (it == JapaneseTestType.CUSTOM && selectedJapaneseTest == it && customTestName.isNotBlank()) customTestName else it.label
                        },
                        selectedIndex = selectedJapaneseTest?.ordinal,
                        onSelect = {
                            val t = JapaneseTestType.entries[it]
                            if (t == JapaneseTestType.CUSTOM) showCustomDialog = true else selectedJapaneseTest = t
                        },
                    )
                    LanguageType.CHINESE -> ChipGrid2Col(
                        items = ChineseTestType.entries.map {
                            if (it == ChineseTestType.CUSTOM && selectedChineseTest == it && customTestName.isNotBlank()) customTestName else it.label
                        },
                        selectedIndex = selectedChineseTest?.ordinal,
                        onSelect = {
                            val t = ChineseTestType.entries[it]
                            if (t == ChineseTestType.CUSTOM) showCustomDialog = true else selectedChineseTest = t
                        },
                    )
                    null -> Unit
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val testLabel = when (selectedLanguage) {
                    LanguageType.ENGLISH -> customTestName.ifBlank { selectedEnglishTest?.label ?: "" }
                    LanguageType.JAPANESE -> customTestName.ifBlank { selectedJapaneseTest?.label ?: "" }
                    LanguageType.CHINESE -> customTestName.ifBlank { selectedChineseTest?.label ?: "" }
                    null -> ""
                }
                val lang = selectedLanguage
                val engTest = selectedEnglishTest
                val jpTest = selectedJapaneseTest
                val cnTest = selectedChineseTest
                val customTest = customTestName
                onCreateClick("${selectedLanguage?.label} · $testLabel") { state ->
                    state.copy(
                        languageType = lang,
                        englishTest = engTest,
                        japaneseTest = jpTest,
                        chineseTest = cnTest,
                        customLanguageTest = customTest,
                    )
                }
            },
        )
    }

    if (showCustomDialog) {
        CustomInputDialog(
            title = "시험 유형 직접 입력",
            hint = "시험 유형을 입력해주세요",
            value = customTestName,
            onValueChange = { customTestName = it },
            onCancel = { showCustomDialog = false },
            onApply = {
                when (selectedLanguage) {
                    LanguageType.ENGLISH -> selectedEnglishTest = EnglishTestType.CUSTOM
                    LanguageType.JAPANESE -> selectedJapaneseTest = JapaneseTestType.CUSTOM
                    LanguageType.CHINESE -> selectedChineseTest = ChineseTestType.CUSTOM
                    null -> Unit
                }
                showCustomDialog = false
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// (5) 공무원 시험 : 급수 6개 + 직렬 12개
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun CivilServantSection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var selectedGrade by remember { mutableStateOf<CivilServantGrade?>(null) }
    var selectedSeries by remember { mutableStateOf<CivilServantSeries?>(null) }
    val isEnabled = selectedGrade != null && selectedSeries != null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionLabel("급수")
            Spacer(modifier = Modifier.height(10.dp))
            ChipGrid2Col(
                items = CivilServantGrade.entries.map { it.label },
                selectedIndex = selectedGrade?.ordinal,
                onSelect = { selectedGrade = CivilServantGrade.entries[it] },
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("직렬")
            Spacer(modifier = Modifier.height(10.dp))
            ChipGrid3Col(
                items = CivilServantSeries.entries.map { it.label },
                selectedIndex = selectedSeries?.ordinal,
                onSelect = { selectedSeries = CivilServantSeries.entries[it] },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val grade = selectedGrade
                val series = selectedSeries
                onCreateClick("${grade?.label} · ${series?.label}") { state ->
                    state.copy(civilServantGrade = grade, civilServantSeries = series)
                }
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// (6) 기타 시험 : 자유 입력 30자
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExamOtherSection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val isEnabled = text.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "준비 중인 시험이나 자격증을 입력해주세요",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray950,
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            BaseTextField(
                value = text,
                onValueChange = { if (it.length <= 30) text = it },
                hint = "최대 30자 이내로 입력해주세요",
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${text.length}/30",
                style = MaterialTheme.typography.labelSmall.copy(color = Gray500),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val t = text
                onCreateClick(t) { state -> state.copy(otherExamText = t) }
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// SELF_STUDY : 숙련도 4개
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun FamiliaritySection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var selected by remember { mutableStateOf<FamiliarityLevel?>(null) }
    val isEnabled = selected != null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "이 과목에 얼마나 익숙하신가요?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray950,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FamiliarityLevel.entries.forEach { level ->
                    FamiliarityItem(
                        level = level,
                        isSelected = selected == level,
                        onClick = { selected = level },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val level = selected
                onCreateClick(level?.label ?: "") { state -> state.copy(familiarityLevel = level) }
            },
        )
    }
}

@Composable
private fun FamiliarityItem(
    level: FamiliarityLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Brown950 else androidx.compose.ui.graphics.Color.Transparent
    val bgColor = if (isSelected) androidx.compose.ui.graphics.Color(0xFFF5F0EB) else Gray100

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = level.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Brown950 else Gray700,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = level.description,
                style = MaterialTheme.typography.labelSmall.copy(color = Gray500),
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// OTHER : 추가 설명 입력
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun AdditionalDescriptionSection(
    onSkipClick: () -> Unit,
    onCreateClick: (String, (AddSubjectState) -> AddSubjectState) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val isEnabled = true

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "추가로 설명해주시면 더 잘 도와드릴 수 있어요",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray950,
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            BaseTextField(
                value = text,
                onValueChange = { text = it },
                hint = "자유롭게 입력해주세요",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        AddSubjectBottomCta(
            buttonText = "과목 만들기",
            isEnabled = isEnabled,
            onSkipClick = onSkipClick,
            onNextClick = {
                val desc = text
                onCreateClick(desc) { state -> state.copy(additionalDescription = desc) }
            },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Helper Composables
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Gray950,
        ),
    )
}

@Composable
private fun ChipGrid2Col(
    items: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEachIndexed { colIdx, label ->
                    SelectionChip(
                        label = label,
                        isSelected = selectedIndex == rowIdx * 2 + colIdx,
                        onClick = { onSelect(rowIdx * 2 + colIdx) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChipGrid3Col(
    items: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEachIndexed { colIdx, label ->
                    SelectionChip(
                        label = label,
                        isSelected = selectedIndex == rowIdx * 3 + colIdx,
                        onClick = { onSelect(rowIdx * 3 + colIdx) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CustomInputDialog(
    title: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Gray950,
                ),
            )
        },
        text = {
            BaseTextField(
                value = value,
                onValueChange = onValueChange,
                hint = hint,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onApply, enabled = value.isNotBlank()) {
                Text("적용", color = Brown950)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("취소", color = Gray500) }
        },
        containerColor = White,
        shape = RoundedCornerShape(12.dp),
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
private fun Step3UniversityPreview() {
    QuiketTheme { AddSubjectStep3Screen(studyPurpose = StudyPurpose.EXAM, examType = ExamType.UNIVERSITY) }
}

@Preview(showBackground = true)
@Composable
private fun Step3CertificatePreview() {
    QuiketTheme { AddSubjectStep3Screen(studyPurpose = StudyPurpose.EXAM, examType = ExamType.CERTIFICATE) }
}

@Preview(showBackground = true)
@Composable
private fun Step3LanguagePreview() {
    QuiketTheme { AddSubjectStep3Screen(studyPurpose = StudyPurpose.EXAM, examType = ExamType.LANGUAGE) }
}

@Preview(showBackground = true)
@Composable
private fun Step3SelfStudyPreview() {
    QuiketTheme { AddSubjectStep3Screen(studyPurpose = StudyPurpose.SELF_STUDY) }
}

@Preview(showBackground = true)
@Composable
private fun Step3OtherPreview() {
    QuiketTheme { AddSubjectStep3Screen(studyPurpose = StudyPurpose.OTHER) }
}
