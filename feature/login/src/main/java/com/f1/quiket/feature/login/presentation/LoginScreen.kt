package com.f1.quiket.feature.login.presentation

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R as DesignSystemR
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

private val KakaoYellow = Color(0xFFFFE500)

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onQuiketLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
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
                .height(558.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            LoginLogo()
            LoginCharacter()
            LoginActionArea(
                onQuiketLoginClick = onQuiketLoginClick,
                onKakaoLoginClick = onKakaoLoginClick,
                onSignUpClick = onSignUpClick,
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
            painter = painterResource(id = DesignSystemR.drawable.logo_splash),
            contentDescription = "Quiket",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 132.dp, height = 41.5.dp),
        )
        Text(
            text = "AI 퀴즈로 채워지는\n나만의 도토리 창고",
            color = Gray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.width(104.dp),
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
            painter = painterResource(id = DesignSystemR.drawable.img_login_main),
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
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = Gray700,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Gray700,
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
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(157.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QuiketPrimaryButton(
            text = "Quiket 로그인",
            onClick = onQuiketLoginClick,
        )
        Spacer(modifier = Modifier.height(12.dp))
        KakaoLoginButton(onClick = onKakaoLoginClick)
        Spacer(modifier = Modifier.height(20.dp))
        SignUpText(onClick = onSignUpClick)
    }
}

@Composable
private fun KakaoLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuiketPrimaryButton(
        text = "카카오로 시작하기",
        containerColor = KakaoYellow,
        contentColor = Black,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Image(
                painter = painterResource(id = DesignSystemR.drawable.ic_kakao_talk),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp),
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
            color = Gray700,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "회원가입",
            color = Orange500,
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    QuiketTheme {
        LoginScreen(
            onBackClick = {},
            onQuiketLoginClick = {},
            onKakaoLoginClick = {},
            onSignUpClick = {},
        )
    }
}
