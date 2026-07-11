package com.f1.quiket.composeapp.login

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.composeapp.designsystem.QuiketBlack
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_kakao_talk
import quiket.composeapp.generated.resources.img_login_main
import quiket.composeapp.generated.resources.logo_splash

private val KakaoYellow = Color(0xFFFFE500)

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onQuiketLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    isKakaoLoading: Boolean = false,
    kakaoErrorMessage: String? = null,
    isQuiketLoginVisible: Boolean = false,
    isSignUpVisible: Boolean = false,
    isAppleLoginVisible: Boolean = false,
    isAppleLoading: Boolean = false,
    appleErrorMessage: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        BackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 26.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 144.dp)
                .height(
                    558.dp +
                        if (isQuiketLoginVisible) 68.dp else 0.dp +
                        if (isAppleLoginVisible) 76.dp else 0.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            LoginLogo()
            LoginCharacter()
            LoginActionArea(
                onQuiketLoginClick = onQuiketLoginClick,
                onKakaoLoginClick = onKakaoLoginClick,
                onAppleLoginClick = onAppleLoginClick,
                onSignUpClick = onSignUpClick,
                isKakaoLoading = isKakaoLoading,
                kakaoErrorMessage = kakaoErrorMessage,
                isQuiketLoginVisible = isQuiketLoginVisible,
                isSignUpVisible = isSignUpVisible,
                isAppleLoginVisible = isAppleLoginVisible,
                isAppleLoading = isAppleLoading,
                appleErrorMessage = appleErrorMessage,
            )
        }
    }
}

@Composable
private fun LoginLogo(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(132.dp)
            .height(99.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_splash),
            contentDescription = "Quiket",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 132.dp, height = 41.5.dp),
        )
        Text(
            text = "AI 퀴즈로 채워지는\n나만의 도토리 창고",
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.width(132.dp),
        )
    }
}

@Composable
private fun LoginCharacter(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_login_main),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 328.dp, height = 230.dp),
        )
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .semantics { contentDescription = "뒤로" }
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = QuiketGray700,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray700,
                start = Offset(size.width * 0.32f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun LoginActionArea(
    onQuiketLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    isKakaoLoading: Boolean = false,
    kakaoErrorMessage: String? = null,
    isQuiketLoginVisible: Boolean = false,
    isSignUpVisible: Boolean = false,
    isAppleLoginVisible: Boolean = false,
    isAppleLoading: Boolean = false,
    appleErrorMessage: String? = null,
) {
    val socialErrorMessage = appleErrorMessage ?: kakaoErrorMessage

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(
                176.dp +
                    if (isQuiketLoginVisible) 68.dp else 0.dp +
                    if (isAppleLoginVisible) 76.dp else 0.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isQuiketLoginVisible) {
            QuiketPrimaryButton(
                text = "Quiket 로그인",
                onClick = onQuiketLoginClick,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        KakaoLoginButton(
            isLoading = isKakaoLoading,
            onClick = onKakaoLoginClick,
        )
        if (isAppleLoginVisible) {
            Spacer(modifier = Modifier.height(12.dp))
            AppleLoginButton(
                isLoading = isAppleLoading,
                onClick = onAppleLoginClick,
            )
        }
        if (!socialErrorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = socialErrorMessage,
                color = QuiketNegative,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (isSignUpVisible) {
            Spacer(modifier = Modifier.height(if (socialErrorMessage.isNullOrBlank()) 20.dp else 8.dp))
            SignUpText(onClick = onSignUpClick)
        }
    }
}

@Composable
private fun KakaoLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuiketPrimaryButton(
        text = if (isLoading) "카카오 로그인 중..." else "카카오로 시작하기",
        containerColor = KakaoYellow,
        contentColor = QuiketBlack,
        enabled = !isLoading,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Image(
                painter = painterResource(Res.drawable.ic_kakao_talk),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp),
            )
        },
    )
}

@Composable
private fun AppleLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuiketPrimaryButton(
        text = if (isLoading) "Apple 로그인 중..." else "Apple로 계속하기",
        containerColor = QuiketBlack,
        contentColor = QuiketWhite,
        enabled = !isLoading,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Text(
                text = "\uF8FF",
                color = QuiketWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            )
        },
    )
}

@Composable
private fun SignUpText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(29.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Quiket이 처음이신가요?",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "회원가입",
            color = QuiketOrange500,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onClick,
                ),
        )
    }
}
