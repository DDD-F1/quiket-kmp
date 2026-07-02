package com.f1.quiket.feature.home.presentation.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

fun buildTutorialPages(
    subjectTabRect: Rect?,
    uploadButtonRect: Rect?,
    quizButtonRect: Rect?,
    profileCardRect: Rect?,
    activityTabRect: Rect?,
    fabRect: Rect?,
): Triple<List<TutorialStep>, List<TutorialStep>, List<TutorialStep>> {
    val firstPageSteps = listOf(
        TutorialStep(
            1,
            "공부하고 싶은 과목을 추가해\n",
            "챕터, 파트",
            " 별로 분류해 보관할\n수 있어요",
            TooltipAlignment.Step1,
            subjectTabRect,
            arrowStartOffset = Offset(-30f, -1f),
            arrowEndOffset = Offset(0f, -10f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            2,
            "나의 강의 자료를 ",
            "pdf, 이미지,\n텍스트",
            "로 업로드할 수 있어요",
            TooltipAlignment.Step2,
            uploadButtonRect,
            arrowStartOffset = Offset(-150f, -60f),
            arrowEndOffset = Offset(40f, -30f),
            arrowCurvature = -0.3f
        ),
        TutorialStep(
            3,
            "업로드한 강의를 기반으로",
            "\nAI가 퀴즈를 만들어줘요",
            "",
            TooltipAlignment.Step3,
            quizButtonRect,
            arrowStartOffset = Offset(30f, 0f),
            arrowEndOffset = Offset(40f, -50f),
            arrowCurvature = 0.3f
        )
    )
    val secondPageSteps = listOf(
        TutorialStep(
            4,
            "누르면 마이페이지로 이동해요.퀴즈로\n모은 ",
            "도토리",
            "를 쓸 수 있어요!",
            TooltipAlignment.Step4,
            profileCardRect,
            arrowStartOffset = Offset(-160f, 5f),
            arrowEndOffset = Offset(-30f, -55f),
            arrowCurvature = -0.3f
        ),
        TutorialStep(
            5,
            "최근에 생성하고 풀어본 퀴즈",
            " 항목",
            "을 볼 수 있어요",
            TooltipAlignment.Step5,
            activityTabRect,
            arrowStartOffset = Offset(-30f, -1f),
            arrowEndOffset = Offset(0f, -15f),
            arrowCurvature = 0.3f
        ),
        TutorialStep(
            6,
            "플로팅 버튼으로도 ",
            "과목 추가,\n강의 업로드, 퀴즈 만들기",
            " 등을 \n모두 할 수 있어요!",
            TooltipAlignment.Step6,
            fabRect,
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(-40f, -30f),
            arrowCurvature = 0.3f
        )
    )
    val thirdPageSteps = listOf(
        TutorialStep(
            7,
            "퀴켓의 마스코트 다람쥐,\n",
            "",
            "'큐링이'에요.",
            TooltipAlignment.Step7,
            profileCardRect,
            arrowStartOffset = Offset(-160f, 5f),
            arrowEndOffset = Offset(-30f, -55f),
            arrowCurvature = -0.3f
        ),
        TutorialStep(
            8,
            "획득한 도토리를 통해 '도토리\n",
            "",
            "상점'에서 큐링이를 위한 아이\n템을 구매할 수 있게 돼요!",
            TooltipAlignment.Step8,
            profileCardRect,
            arrowStartOffset = Offset(100f, 0f),
            arrowEndOffset = Offset(170f, 50f),
            arrowCurvature = 0.3f
        )
    )
    return Triple(firstPageSteps, secondPageSteps, thirdPageSteps)
}