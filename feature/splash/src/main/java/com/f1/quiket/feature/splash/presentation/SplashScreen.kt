package com.f1.quiket.feature.splash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.f1.quiket.core.designsystem.R as DesignSystemR
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray700

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/anim_quiket_splash.json"),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        speed = 2f,
        iterations = 1,
        ignoreSystemAnimatorScale = true,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brown50),
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * SPLASH_ANIMATION_TOP_RATIO)
                .size(SPLASH_ANIMATION_SIZE),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * LOGO_TOP_RATIO),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = DesignSystemR.drawable.logo_splash),
                contentDescription = "Quiket",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(LOGO_WIDTH)
                    .height(LOGO_HEIGHT),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AI 퀴즈로 채워지는\n나만의 도토리 창고",
                color = Gray700,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val LOGO_TOP_RATIO = 148.46667f / 800f
private const val SPLASH_ANIMATION_TOP_RATIO = 171.5f / 800f

private val LOGO_WIDTH = 132.dp
private val LOGO_HEIGHT = 41.5.dp
private val SPLASH_ANIMATION_SIZE = 486.dp
