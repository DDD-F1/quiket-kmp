package com.f1.quiket.feature.home.presentation.component

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.Tutorial
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.R
import com.f1.quiket.feature.home.presentation.model.TooltipAlignment
import com.f1.quiket.feature.home.presentation.model.TutorialPage
import com.f1.quiket.feature.home.presentation.model.TutorialStep
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeTutorialOverlay(
    currentPage: TutorialPage,
    firstPageSteps: List<TutorialStep>,
    secondPageSteps: List<TutorialStep>,
    thirdPageSteps: List<TutorialStep>,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    statusBarHeight: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val currentSteps = when (currentPage) {
        TutorialPage.FIRST -> firstPageSteps
        TutorialPage.SECOND -> secondPageSteps
        TutorialPage.THIRD -> thirdPageSteps
    }
    val isLastPage = currentPage == TutorialPage.THIRD

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onNext() }
        ) {
            // 오버레이 배경
            drawRect(color = Tutorial)

            // 앵커 컴포넌트 투명 구멍 뚫기
            currentSteps.forEach { step ->
                step.anchorRect?.let { rect ->
                    val top = rect.top - with(density) { statusBarHeight.toPx() }
                    val bottom = rect.bottom - with(density) { statusBarHeight.toPx() }

                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(rect.left, top),
                        size = Size(rect.right - rect.left, bottom - top),
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
            }

            // 앵커(컴포넌트) → 툴팁 방향 대시 곡선 화살표
            val arrowColor = White

            currentSteps.forEachIndexed { index, step ->
                step.anchorRect?.let { rect ->
                    val top = rect.top - with(density) { statusBarHeight.toPx() }
                    val bottom = rect.bottom - with(density) { statusBarHeight.toPx() }
                    val left = rect.left
                    val right = rect.right
                    val anchorCenterX = (left + right) / 2f
                    val anchorCenterY = (top + bottom) / 2f

                    // 툴팁 좌상단 offset
                    val (tooltipX, tooltipY) = when (step.tooltipAlignment) {
                        TooltipAlignment.Step1 -> Pair(
                            left + with(density) { -30.dp.toPx() },
                            bottom + with(density) { 50.dp.toPx() }
                        )

                        TooltipAlignment.Step2 -> Pair(
                            left,
                            top - with(density) { 30.dp.toPx() }
                        )

                        TooltipAlignment.Step3 -> Pair(
                            right - with(density) { 150.dp.toPx() },
                            top - with(density) { 70.dp.toPx() }
                        )

                        TooltipAlignment.Step4 -> Pair(
                            left,
                            top - with(density) { 40.dp.toPx() }
                        )

                        TooltipAlignment.Step5 -> Pair(
                            left + with(density) { -30.dp.toPx() },
                            bottom + with(density) { 50.dp.toPx() }
                        )

                        TooltipAlignment.Step6 -> Pair(
                            right - with(density) { 60.dp.toPx() },
                            top - with(density) { 70.dp.toPx() }
                        )

                        TooltipAlignment.Step7 -> Pair(
                            left,
                            top - with(density) { 40.dp.toPx() }
                        )

                        TooltipAlignment.Step8 -> Pair(
                            left + with(density) { 10.dp.toPx() },
                            bottom + with(density) { 10.dp.toPx() }
                        )
                    }

                    // 툴팁 크기 추정 (약 160dp x 60dp)
                    val tooltipW = with(density) { 120.dp.toPx() }
                    val tooltipH = with(density) { 60.dp.toPx() }
                    val tooltipCenterX = tooltipX + tooltipW / 2f
                    val tooltipCenterY = tooltipY + tooltipH / 2f

                    // ── 시작점 계산 ──────────────────────────────────────────
                    // 1) 툴팁 방향에 따라 앵커의 가장 가까운 엣지를 기준점으로 잡고
                    // 2) 앵커에서 살짝 띄우는 gap 적용
                    // 3) arrowStartOffset(dp) 으로 좌우/상하 추가 조절
                    val anchorGapPx = with(density) { 10.dp.toPx() }

                    val baseStartX: Float
                    val baseStartY: Float
                    if (tooltipCenterY < top) {
                        // 툴팁이 위 → 앵커 상단에서 위로 gap
                        baseStartX = anchorCenterX
                        baseStartY = top - anchorGapPx
                    } else if (tooltipCenterY > bottom) {
                        // 툴팁이 아래 → 앵커 하단에서 아래로 gap
                        baseStartX = anchorCenterX
                        baseStartY = bottom + anchorGapPx
                    } else if (tooltipCenterX < left) {
                        // 툴팁이 왼쪽 → 앵커 좌측에서 왼쪽으로 gap
                        baseStartX = left - anchorGapPx
                        baseStartY = anchorCenterY
                    } else {
                        // 툴팁이 오른쪽 → 앵커 우측에서 오른쪽으로 gap
                        baseStartX = right + anchorGapPx
                        baseStartY = anchorCenterY
                    }

                    // arrowStartOffset 을 px 로 변환해 더함
                    val startOffsetPx = with(density) {
                        Offset(
                            step.arrowStartOffset.x.dp.toPx(),
                            step.arrowStartOffset.y.dp.toPx()
                        )
                    }
                    val start = Offset(
                        baseStartX + startOffsetPx.x,
                        baseStartY + startOffsetPx.y
                    )

                    // ── 끝점 계산 ────────────────────────────────────────────
                    // 툴팁 중앙에서 arrowEndOffset(dp) 으로 좌우/상하 추가 조절
                    // + 화살표 머리가 툴팁과 겹치지 않도록 툴팁 방향 반대로 gap 확보
                    val tooltipGapPx = with(density) { 12.dp.toPx() }

                    val baseEndX: Float
                    val baseEndY: Float
                    if (tooltipCenterY < top) {
                        // 툴팁이 앵커 위 → 툴팁 하단 중앙 바깥
                        baseEndX = tooltipCenterX
                        baseEndY = tooltipY + tooltipH + tooltipGapPx
                    } else if (tooltipCenterY > bottom) {
                        // 툴팁이 앵커 아래 → 툴팁 상단 중앙 바깥
                        baseEndX = tooltipCenterX
                        baseEndY = tooltipY - tooltipGapPx
                    } else if (tooltipCenterX < left) {
                        // 툴팁이 앵커 왼쪽 → 툴팁 우측 중앙 바깥
                        baseEndX = tooltipX + tooltipW + tooltipGapPx
                        baseEndY = tooltipCenterY
                    } else {
                        // 툴팁이 앵커 오른쪽 → 툴팁 좌측 중앙 바깥
                        baseEndX = tooltipX - tooltipGapPx
                        baseEndY = tooltipCenterY
                    }

                    val endOffsetPx = with(density) {
                        Offset(
                            step.arrowEndOffset.x.dp.toPx(),
                            step.arrowEndOffset.y.dp.toPx()
                        )
                    }
                    val end = Offset(
                        baseEndX + endOffsetPx.x,
                        baseEndY + endOffsetPx.y
                    )

                    val curvature = step.arrowCurvature

                    drawDashedCurvedArrow(
                        start = start,
                        end = end,
                        color = arrowColor,
                        strokeWidth = with(density) { 2.dp.toPx() },
                        dashLength = with(density) { 3.dp.toPx() },
                        gapLength = with(density) { 3.dp.toPx() },
                        arrowSize = with(density) { 7.dp.toPx() },
                        curvature = curvature
                    )
                }
            }
        }

        // 툴팁 렌더링
        currentSteps.forEach { step ->
            step.anchorRect?.let { rect ->
                val top = with(density) { rect.top.toDp() } - statusBarHeight
                val bottom = with(density) { rect.bottom.toDp() } - statusBarHeight
                val left = with(density) { rect.left.toDp() }
                val right = with(density) { rect.right.toDp() }

                val (x, y) = when (step.tooltipAlignment) {
                    TooltipAlignment.Step1 -> Pair(left + 40.dp, bottom + 20.dp)
                    TooltipAlignment.Step2 -> Pair(left, top - 60.dp)
                    TooltipAlignment.Step3 -> Pair(right - 170.dp, top - 100.dp)
                    TooltipAlignment.Step4 -> Pair(left, top - 80.dp)
                    TooltipAlignment.Step5 -> Pair(right - 60.dp, bottom + 25.dp)
                    TooltipAlignment.Step6 -> Pair(right - 240.dp, top - 60.dp)
                    TooltipAlignment.Step7 -> Pair(left, top - 80.dp)
                    TooltipAlignment.Step8 -> Pair(right - 220.dp, bottom + 50.dp)
                }

                TutorialTooltip(
                    step = step,
                    modifier = Modifier.offset(x = x, y = y)
                )
            } ?: run {
                android.util.Log.d("Tutorial", "step ${step.step} anchorRect is null")
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onNext() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!isLastPage) "탭하여 다음으로 넘어가기" else "Quiket 사용해보기",
                style = MaterialTheme.typography.labelSmall,
                color = White,
            )

            if (!isLastPage) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_home_tutorial_next),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 앵커(컴포넌트) → 툴팁 방향으로 대시(─ ─ ─) 곡선 화살표를 그립니다.
 *
 * - 선: PathEffect.dashPathEffect (dashedBorder 와 동일 방식)
 * - 화살표 각도: Compose PathMeasure.getTangent(distance) 사용
 *   (getPosTan 은 Compose PathMeasure 에 없음)
 */
private fun DrawScope.drawDashedCurvedArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float = 4f,
    dashLength: Float = 16f,
    gapLength: Float = 10f,
    arrowSize: Float = 16f,
    curvature: Float = 0.3f
) {
    val dx = end.x - start.x
    val dy = end.y - start.y

    // 2차 베지어 제어점
    val controlX = start.x + dx * 0.5f - dy * curvature
    val controlY = start.y + dy * 0.5f + dx * curvature

    // 베지어 곡선 Path
    val curvePath = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(controlX, controlY, end.x, end.y)
    }

    // 곡선 길이 측정
    val measure = PathMeasure()
    measure.setPath(curvePath, false)
    val curveLength = measure.length

    // 화살표 머리 공간만큼 dash 선을 짧게 잘라냄
    val arrowReserve = arrowSize * 1.6f
    val dashedPath = Path()
    measure.getSegment(
        startDistance = 0f,
        stopDistance = (curveLength - arrowReserve).coerceAtLeast(0f),
        destination = dashedPath,
        startWithMoveTo = true
    )

    // dashedBorder 와 동일: dashPathEffect + StrokeCap.Round
    drawPath(
        path = dashedPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength, gapLength),
                phase = 0f
            )
        )
    )

    // 화살표 머리 각도: getTangent(distance) 로 탄젠트 Offset 반환
    val tangentOffset: Offset = measure.getTangent(curveLength - 1f)
    val angle = atan2(tangentOffset.y, tangentOffset.x)

    val leftAngle = angle + Math.PI.toFloat() * 5f / 6f
    val rightAngle = angle - Math.PI.toFloat() * 5f / 6f

    // > 모양 화살표 머리
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
            join = StrokeJoin.Round
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeTutorialOverlayFirstPagePreview() {
    val firstPageSteps = listOf(
        TutorialStep(
            1,
            "공부하고 싶은 과목을 추가해\n",
            "챕터, 파트",
            " 별로 분류해 보관할\n수 있어요",
            TooltipAlignment.Step1,
            anchorRect = Rect(16f, 430f, 180f, 470f),
            arrowStartOffset = Offset(-20f, 0f),
            arrowEndOffset = Offset(0f, 10f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            2,
            "나의 강의 자료를 ",
            "pdf, 이미지,\n텍스트",
            "로 업로드할 수 있어요",
            TooltipAlignment.Step2,
            anchorRect = Rect(24f, 250f, 200f, 310f),
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(0f, 0f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            3,
            "업로드한 강의를 기반으로",
            "\nAI가 퀴즈를 만들어줘요",
            "",
            TooltipAlignment.Step3,
            anchorRect = Rect(210f, 250f, 400f, 310f),
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(0f, 0f),
            arrowCurvature = -0.3f
        )
    )

    QuiketTheme {
        HomeTutorialOverlay(
            currentPage = TutorialPage.FIRST,
            firstPageSteps = firstPageSteps,
            secondPageSteps = emptyList(),
            thirdPageSteps = emptyList(),
            onNext = {},
            onSkip = {},
            statusBarHeight = 0.dp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeTutorialOverlaySecondPagePreview() {
    val secondPageSteps = listOf(
        TutorialStep(
            4,
            "누르면 마이페이지로 이동해요.\n퀴즈로 모은 ",
            "도토리",
            "를 쓸 수 있어요!",
            TooltipAlignment.Step4,
            anchorRect = Rect(16f, 100f, 420f, 160f),
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(0f, 0f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            5,
            "최근에 생성하고 풀어본 퀴즈",
            " 항목",
            "을 볼 수 있어요",
            TooltipAlignment.Step5,
            anchorRect = Rect(140f, 270f, 280f, 320f),
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(0f, 0f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            6,
            "플로팅 버튼으로도 ",
            "과목 추가,\n강의 업로드, 퀴즈 만들기",
            " 등을 \n모두 할 수 있어요!",
            TooltipAlignment.Step6,
            anchorRect = Rect(340f, 600f, 420f, 680f),
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(0f, 0f),
            arrowCurvature = -0.3f
        )
    )
    QuiketTheme {
        HomeTutorialOverlay(
            currentPage = TutorialPage.SECOND,
            firstPageSteps = emptyList(),
            secondPageSteps = secondPageSteps,
            thirdPageSteps = emptyList(),
            onNext = {},
            onSkip = {},
            statusBarHeight = 0.dp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeTutorialOverlayThirdPagePreview() {
    val thirdPageSteps = listOf(
        TutorialStep(
            7,
            "퀴켓의 마스코트 다람쥐,\n",
            "",
            "'큐링이'에요.",
            TooltipAlignment.Step7,
            anchorRect = Rect(16f, 100f, 420f, 160f),
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(0f, 0f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            8,
            "획득한 도토리를 통해 '도토리\n",
            " ",
            "상점'에서 큐링이를 위한 아이\n템을 구매할 수 있게 돼요!",
            TooltipAlignment.Step8,
            anchorRect = Rect(16f, 100f, 420f, 160f),
            arrowStartOffset = Offset(50f, 0f),
            arrowEndOffset = Offset(80f, 100f),
            arrowCurvature = 0.3f
        )
    )
    QuiketTheme {
        HomeTutorialOverlay(
            currentPage = TutorialPage.THIRD,
            firstPageSteps = emptyList(),
            secondPageSteps = emptyList(),
            thirdPageSteps = thirdPageSteps,
            onNext = {},
            onSkip = {},
            statusBarHeight = 0.dp
        )
    }
}