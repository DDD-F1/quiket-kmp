package com.f1.quiket.feature.onboarding.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R as DesignSystemR
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.White
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = remember { onboardingPages }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage by remember {
        derivedStateOf { pagerState.currentPage }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 92.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OnboardingTopBar(
                pageCount = pages.size,
                currentPage = currentPage,
            )
            OnboardingText(
                title = pages[currentPage].title,
                description = pages[currentPage].description,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(388.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) { pageIndex ->
                OnboardingIllustration(
                    imageRes = pages[pageIndex].imageRes,
                    contentDescription = pages[pageIndex].title,
                )
            }
        }

        OnboardingButtonArea(
            isLastPage = currentPage == pages.lastIndex,
            onSkip = onSkip,
            onPrimaryClick = {
                if (currentPage == pages.lastIndex) {
                    onComplete()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
        )
    }
}

@Composable
private fun OnboardingTopBar(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = DesignSystemR.drawable.ic_acorn),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(24.dp),
        )
        OnboardingPageIndicator(
            pageCount = pageCount,
            currentPage = currentPage,
        )
    }
}

@Composable
private fun OnboardingText(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = description,
            color = Gray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun OnboardingIllustration(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val scale by animateFloatAsState(
                targetValue = if (index == currentPage) 1f else 0.9f,
                label = "OnboardingDotScale",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(if (index == currentPage) Brown950 else Gray100),
            )
        }
    }
}

@Composable
private fun OnboardingButtonArea(
    isLastPage: Boolean,
    onSkip: () -> Unit,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!isLastPage) {
            SkipButton(onClick = onSkip)
        }
        OnboardingPrimaryButton(
            label = if (isLastPage) "시작하기" else "다음",
            onClick = onPrimaryClick,
        )
    }
}

@Composable
private fun SkipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "지금은 건너뛰기",
            color = Gray600,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(modifier = Modifier.width(2.dp))
        ChevronRight(
            color = Gray600,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ChevronRight(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.4f, size.height * 0.24f),
            end = Offset(size.width * 0.65f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.5f),
            end = Offset(size.width * 0.4f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun OnboardingPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuiketPrimaryButton(
        text = label,
        modifier = modifier,
        onClick = onClick,
    )
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    @param:DrawableRes val imageRes: Int,
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "내 강의 노트로 만드는 나만의 시험",
        description = "노트를 올리면 AI가 알아서 정리하고 퀴즈까지\n뚝딱 생성해줘요",
        imageRes = DesignSystemR.drawable.img_onboarding_1,
    ),
    OnboardingPage(
        title = "풀 수록 업그레이드 되는 공부방",
        description = "문제를 풀 수록 도토리가 쌓이고 {다람쥐명}의\n공부 환경이 업그레이드 돼요",
        imageRes = DesignSystemR.drawable.img_onboarding_2,
    ),
    OnboardingPage(
        title = "과목 별로 쌓이는 나만의 아카이브",
        description = "강의를 업로드해서 챕터, 파트 별로 정리하고 차곡차곡 쌓아 나갈 수 있어요",
        imageRes = DesignSystemR.drawable.img_onboarding_3,
    ),
)
