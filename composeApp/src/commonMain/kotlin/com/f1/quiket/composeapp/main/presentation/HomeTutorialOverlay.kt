package com.f1.quiket.composeapp.main.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val TutorialOverlayColor = Color(0xE62A2A2A)
private val TutorialHighlightTextColor = Color(0xFFFFBB70)

@Composable
internal fun HomeTutorialOverlay(
    page: HomeTutorialPage,
    subjectTabRect: Rect?,
    uploadButtonRect: Rect?,
    quizButtonRect: Rect?,
    profileCardRect: Rect?,
    examCardRect: Rect?,
    activityTabRect: Rect?,
    fabRect: Rect?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val steps = page.steps(
        subjectTabRect = subjectTabRect,
        uploadButtonRect = uploadButtonRect,
        quizButtonRect = quizButtonRect,
        profileCardRect = profileCardRect,
        examCardRect = examCardRect,
        activityTabRect = activityTabRect,
        fabRect = fabRect,
    )
    val isLastPage = page == HomeTutorialPage.Third
    val density = LocalDensity.current
    var overlayRootOffset by remember { mutableStateOf(Offset.Zero) }
    val localSteps = steps.map { step ->
        step.copy(anchorRect = step.anchorRect?.toLocalRect(overlayRootOffset))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayRootOffset = coordinates.positionInRoot()
            }
            .zIndex(100f),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onNext() },
        ) {
            drawRect(color = TutorialOverlayColor)

            localSteps.forEach { step ->
                step.anchorRect?.let { rect ->
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(rect.right - rect.left, rect.bottom - rect.top),
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        blendMode = BlendMode.Clear,
                    )
                }
            }

            localSteps.forEach { step ->
                step.anchorRect?.let { rect ->
                    val left = rect.left
                    val top = rect.top
                    val right = rect.right
                    val bottom = rect.bottom
                    val anchorCenterX = (left + right) / 2f
                    val anchorCenterY = (top + bottom) / 2f
                    val (tooltipX, tooltipY) = step.tooltipOrigin(rect, density)

                    val tooltipW = with(density) { 120.dp.toPx() }
                    val tooltipH = with(density) { 60.dp.toPx() }
                    val tooltipCenterX = tooltipX + tooltipW / 2f
                    val tooltipCenterY = tooltipY + tooltipH / 2f
                    val anchorGapPx = with(density) { 10.dp.toPx() }

                    val (baseStartX, baseStartY) = when {
                        tooltipCenterY < top -> anchorCenterX to top - anchorGapPx
                        tooltipCenterY > bottom -> anchorCenterX to bottom + anchorGapPx
                        tooltipCenterX < left -> left - anchorGapPx to anchorCenterY
                        else -> right + anchorGapPx to anchorCenterY
                    }
                    val start = Offset(
                        x = baseStartX + with(density) { step.arrowStartOffset.x.dp.toPx() },
                        y = baseStartY + with(density) { step.arrowStartOffset.y.dp.toPx() },
                    )

                    val tooltipGapPx = with(density) { 12.dp.toPx() }
                    val (baseEndX, baseEndY) = when {
                        tooltipCenterY < top -> tooltipCenterX to tooltipY + tooltipH + tooltipGapPx
                        tooltipCenterY > bottom -> tooltipCenterX to tooltipY - tooltipGapPx
                        tooltipCenterX < left -> tooltipX + tooltipW + tooltipGapPx to tooltipCenterY
                        else -> tooltipX - tooltipGapPx to tooltipCenterY
                    }
                    val end = Offset(
                        x = baseEndX + with(density) { step.arrowEndOffset.x.dp.toPx() },
                        y = baseEndY + with(density) { step.arrowEndOffset.y.dp.toPx() },
                    )

                    drawDashedCurvedArrow(
                        start = start,
                        end = end,
                        color = QuiketWhite,
                        strokeWidth = with(density) { 2.dp.toPx() },
                        dashLength = with(density) { 3.dp.toPx() },
                        gapLength = with(density) { 3.dp.toPx() },
                        arrowSize = with(density) { 7.dp.toPx() },
                        curvature = step.arrowCurvature,
                    )
                }
            }
        }

        localSteps.forEach { step ->
            step.anchorRect?.let { rect ->
                val (x, y) = step.tooltipOffset(rect, density)
                HomeTutorialTooltip(
                    step = step,
                    modifier = Modifier.offset(x = x, y = y),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onNext() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isLastPage) {
                    "Quiket 사용해보기"
                } else {
                    "탭하여 다음으로 넘어가기"
                },
                color = QuiketWhite,
                style = MaterialTheme.typography.labelSmall,
            )
            if (!isLastPage) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ">",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

private fun Rect.toLocalRect(rootOffset: Offset): Rect = Rect(
    left = left - rootOffset.x,
    top = top - rootOffset.y,
    right = right - rootOffset.x,
    bottom = bottom - rootOffset.y,
)

@Composable
private fun HomeTutorialTooltip(
    step: HomeTutorialStep,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(220.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(QuiketOrange500),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${step.step}",
                color = QuiketWhite,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = QuiketWhite)) {
                    append(step.startText)
                }
                withStyle(SpanStyle(color = TutorialHighlightTextColor)) {
                    append(step.highlightedText)
                }
                withStyle(SpanStyle(color = QuiketWhite)) {
                    append(step.endText)
                }
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun HomeTutorialStep.tooltipOrigin(rect: Rect, density: androidx.compose.ui.unit.Density): Pair<Float, Float> = with(density) {
    val left = rect.left
    val top = rect.top
    val right = rect.right
    val bottom = rect.bottom
    return when (tooltipAlignment) {
        HomeTutorialAlignment.Step1 -> left + -30.dp.toPx() to bottom + 50.dp.toPx()
        HomeTutorialAlignment.Step2 -> left to top - 30.dp.toPx()
        HomeTutorialAlignment.Step3 -> right - 150.dp.toPx() to top - 70.dp.toPx()
        HomeTutorialAlignment.Step4 -> left to top - 40.dp.toPx()
        HomeTutorialAlignment.Step5 -> left + -30.dp.toPx() to bottom + 50.dp.toPx()
        HomeTutorialAlignment.Step6 -> right - 60.dp.toPx() to top - 70.dp.toPx()
        HomeTutorialAlignment.Step7 -> left to top - 40.dp.toPx()
        HomeTutorialAlignment.Step8 -> left + 10.dp.toPx() to bottom + 10.dp.toPx()
    }
}

private fun HomeTutorialStep.tooltipOffset(rect: Rect, density: androidx.compose.ui.unit.Density): Pair<Dp, Dp> = with(density) {
    val left = rect.left.toDp()
    val top = rect.top.toDp()
    val right = rect.right.toDp()
    val bottom = rect.bottom.toDp()
    return when (tooltipAlignment) {
        HomeTutorialAlignment.Step1 -> left + 40.dp to bottom + 20.dp
        HomeTutorialAlignment.Step2 -> left to top - 60.dp
        HomeTutorialAlignment.Step3 -> right - 170.dp to top - 100.dp
        HomeTutorialAlignment.Step4 -> left to top - 80.dp
        HomeTutorialAlignment.Step5 -> right - 60.dp to bottom + 25.dp
        HomeTutorialAlignment.Step6 -> right - 240.dp to top - 60.dp
        HomeTutorialAlignment.Step7 -> left to top - 80.dp
        HomeTutorialAlignment.Step8 -> right - 220.dp to bottom + 50.dp
    }
}

private fun DrawScope.drawDashedCurvedArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    dashLength: Float,
    gapLength: Float,
    arrowSize: Float,
    curvature: Float,
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val controlX = start.x + dx * 0.5f - dy * curvature
    val controlY = start.y + dy * 0.5f + dx * curvature

    val curvePath = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(controlX, controlY, end.x, end.y)
    }
    val measure = PathMeasure()
    measure.setPath(curvePath, false)
    val curveLength = measure.length
    val arrowReserve = arrowSize * 1.6f
    val dashedPath = Path()
    measure.getSegment(
        startDistance = 0f,
        stopDistance = (curveLength - arrowReserve).coerceAtLeast(0f),
        destination = dashedPath,
        startWithMoveTo = true,
    )

    drawPath(
        path = dashedPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength, gapLength),
                phase = 0f,
            ),
        ),
    )

    val tangentOffset = measure.getTangent(curveLength - 1f)
    val angle = atan2(tangentOffset.y, tangentOffset.x)
    val leftAngle = angle + PI.toFloat() * 5f / 6f
    val rightAngle = angle - PI.toFloat() * 5f / 6f
    val arrowPath = Path().apply {
        moveTo(end.x + arrowSize * cos(leftAngle), end.y + arrowSize * sin(leftAngle))
        lineTo(end.x, end.y)
        lineTo(end.x + arrowSize * cos(rightAngle), end.y + arrowSize * sin(rightAngle))
    }

    drawPath(
        path = arrowPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

internal enum class HomeTutorialPage(
    val index: Int,
) {
    First(index = 1),
    Second(index = 2),
    Third(index = 3),
}

private fun HomeTutorialPage.steps(
    subjectTabRect: Rect?,
    uploadButtonRect: Rect?,
    quizButtonRect: Rect?,
    profileCardRect: Rect?,
    examCardRect: Rect?,
    activityTabRect: Rect?,
    fabRect: Rect?,
): List<HomeTutorialStep> = when (this) {
    HomeTutorialPage.First -> listOf(
        HomeTutorialStep(
            step = 1,
            startText = "공부하고 싶은 과목을 추가해\n",
            highlightedText = "챕터, 파트",
            endText = " 별로 분류해 보관할\n수 있어요",
            anchorRect = subjectTabRect,
            tooltipAlignment = HomeTutorialAlignment.Step1,
            arrowStartOffset = Offset(-30f, -1f),
            arrowEndOffset = Offset(0f, -10f),
            arrowCurvature = 0.3f,
        ),
        HomeTutorialStep(
            step = 2,
            startText = "나의 강의 자료를 ",
            highlightedText = "pdf, 이미지,\n텍스트",
            endText = "로 업로드할 수 있어요",
            anchorRect = uploadButtonRect,
            tooltipAlignment = HomeTutorialAlignment.Step2,
            arrowStartOffset = Offset(-150f, -60f),
            arrowEndOffset = Offset(40f, -30f),
            arrowCurvature = -0.3f,
        ),
        HomeTutorialStep(
            step = 3,
            startText = "업로드한 강의를 기반으로\n",
            highlightedText = "AI가 퀴즈를 만들어줘요",
            endText = "",
            anchorRect = quizButtonRect,
            tooltipAlignment = HomeTutorialAlignment.Step3,
            arrowStartOffset = Offset(30f, 0f),
            arrowEndOffset = Offset(40f, -50f),
            arrowCurvature = 0.3f,
        ),
    )

    HomeTutorialPage.Second -> listOf(
        HomeTutorialStep(
            step = 4,
            startText = "누르면 마이페이지로 이동해요.퀴즈로\n모은 ",
            highlightedText = "도토리",
            endText = "를 쓸 수 있어요!",
            anchorRect = profileCardRect,
            tooltipAlignment = HomeTutorialAlignment.Step4,
            arrowStartOffset = Offset(-160f, 5f),
            arrowEndOffset = Offset(-30f, -55f),
            arrowCurvature = -0.3f,
        ),
        HomeTutorialStep(
            step = 5,
            startText = "최근에 생성하고 풀어본 퀴즈",
            highlightedText = " 항목",
            endText = "을 볼 수 있어요",
            anchorRect = activityTabRect,
            tooltipAlignment = HomeTutorialAlignment.Step5,
            arrowStartOffset = Offset(-30f, -1f),
            arrowEndOffset = Offset(0f, -15f),
            arrowCurvature = 0.3f,
        ),
        HomeTutorialStep(
            step = 6,
            startText = "플로팅 버튼으로도 ",
            highlightedText = "과목 추가,\n강의 업로드, 퀴즈 만들기",
            endText = " 등을 모두 할 수 있어요!",
            anchorRect = fabRect,
            tooltipAlignment = HomeTutorialAlignment.Step6,
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(-40f, -30f),
            arrowCurvature = 0.3f,
        ),
    )

    HomeTutorialPage.Third -> listOf(
        HomeTutorialStep(
            step = 7,
            startText = "퀴켓의 마스코트 다람쥐,\n",
            highlightedText = "",
            endText = "'큐링이'에요.",
            anchorRect = profileCardRect,
            tooltipAlignment = HomeTutorialAlignment.Step7,
            arrowStartOffset = Offset(-160f, 5f),
            arrowEndOffset = Offset(-30f, -55f),
            arrowCurvature = -0.3f,
        ),
        HomeTutorialStep(
            step = 8,
            startText = "획득한 도토리를 통해 '도토리\n",
            highlightedText = "",
            endText = "상점'에서 큐링이를 위한 아이\n템을 구매할 수 있게 돼요!",
            anchorRect = profileCardRect,
            tooltipAlignment = HomeTutorialAlignment.Step8,
            arrowStartOffset = Offset(100f, 0f),
            arrowEndOffset = Offset(170f, 50f),
            arrowCurvature = 0.3f,
        ),
    )
}

private data class HomeTutorialStep(
    val step: Int,
    val startText: String,
    val highlightedText: String,
    val endText: String,
    val anchorRect: Rect?,
    val tooltipAlignment: HomeTutorialAlignment,
    val arrowStartOffset: Offset,
    val arrowEndOffset: Offset,
    val arrowCurvature: Float,
)

private enum class HomeTutorialAlignment {
    Step1,
    Step2,
    Step3,
    Step4,
    Step5,
    Step6,
    Step7,
    Step8,
}
