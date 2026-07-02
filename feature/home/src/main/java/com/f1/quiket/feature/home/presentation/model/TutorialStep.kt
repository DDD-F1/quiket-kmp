package com.f1.quiket.feature.home.presentation.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class TutorialStep(
    val step: Int,
    val startText: String,
    val highlightedText: String,
    val endText: String,
    val tooltipAlignment: TooltipAlignment,
    val anchorRect: Rect? = null,
    val arrowStartOffset: Offset = Offset.Zero,
    val arrowEndOffset: Offset = Offset.Zero,
    val arrowCurvature: Float  // 양수=오른쪽, 음수=왼쪽
)

enum class TooltipAlignment {
    Step1, Step2, Step3, Step4, Step5, Step6, Step7, Step8
}

enum class TutorialPage { FIRST, SECOND, THIRD }