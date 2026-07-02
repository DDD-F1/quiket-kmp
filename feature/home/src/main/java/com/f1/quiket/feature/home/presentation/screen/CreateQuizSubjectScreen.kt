package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.presentation.route.createQuizSubjectSamples
import kotlin.collections.listOf

@Composable
fun CreateQuizSubjectScreen(
    subjects: List<QuizSubjectUiModel>,
    selectedSubjectId: String?,
    onSubjectClick: (QuizSubjectUiModel) -> Unit,
    onAddSubjectClick: () -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNextEnabled = selectedSubjectId != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 92.dp),
        ) {
            CreateQuizTopBar(
                title = "과목 선택",
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                QuizCreationStepper(
                    currentStep = 1,
                    totalSteps = 3,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuizSubjectTitleSection()

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        subjects.forEach { subject ->
                            QuizSubjectCard(
                                subject = subject,
                                selected = selectedSubjectId == subject.id,
                                onClick = { onSubjectClick(subject) },
                            )
                        }
                        AddSubjectCard(
                            onClick = onAddSubjectClick,
                        )
                    }
                }
            }
        }

        QuiketPrimaryButton(
            text = "다음",
            enabled = isNextEnabled,
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )
    }
}

@Composable
private fun QuizSubjectTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "어떤 과목의 퀴즈를 만들까요?",
            color = Gray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "퀴즈를 만들 과목을 하나 선택해 주세요",
            color = Gray500,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun QuizSubjectCard(
    subject: QuizSubjectUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(shape)
            .background(if (selected) Brown50 else Gray50)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Brown950,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = subject.name,
                color = Gray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChapterChip(
                    count = subject.chapterCount,
                    selected = selected,
                )
                Text(
                    text = "파트 ${subject.partCount}",
                    color = Gray600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AddSubjectCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(69.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Gray50)
            .dashedBorder(
                color = Gray300,
                cornerRadius = 4.dp,
            )
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = Gray500,
                    start = Offset(size.width * 0.5f, size.height * 0.15f),
                    end = Offset(size.width * 0.5f, size.height * 0.85f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Gray500,
                    start = Offset(size.width * 0.15f, size.height * 0.5f),
                    end = Offset(size.width * 0.85f, size.height * 0.5f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "과목 추가",
                color = Gray500,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateQuizSubjectScreenPreview() {
    QuiketTheme {
        CreateQuizSubjectScreen(
            subjects = listOf(
                createQuizSubjectSamples()[0],
                createQuizSubjectSamples()[1],
                createQuizSubjectSamples()[2],
            ),
            selectedSubjectId = null,
            onSubjectClick = {},
            onAddSubjectClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateQuizSubjectScreenSelectedPreview() {
    QuiketTheme {
        CreateQuizSubjectScreen(
            subjects = listOf(
                createQuizSubjectSamples()[0],
                createQuizSubjectSamples()[1],
                createQuizSubjectSamples()[2],
            ),
            selectedSubjectId = "sqld",
            onSubjectClick = {},
            onAddSubjectClick = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}
