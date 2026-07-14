package com.f1.quiket.composeapp.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.feature.auth.resources.Res
import com.f1.quiket.feature.auth.resources.ic_password
import com.f1.quiket.feature.auth.resources.ic_password_on
import com.f1.quiket.feature.auth.resources.ic_password_wrong
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignUpTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(QuiketWhite),
    ) {
        SignUpBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 50.dp),
        )
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp),
        )
    }
}

@Composable
internal fun SignUpPageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == currentPage) QuiketBrown950 else QuiketGray300,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
internal fun PasswordVisibilityIcon(
    isPasswordVisible: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconRes = when {
        isError -> Res.drawable.ic_password_wrong
        isPasswordVisible -> Res.drawable.ic_password_on
        else -> Res.drawable.ic_password
    }

    Image(
        painter = painterResource(iconRes),
        contentDescription = if (isPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                enabled = !isError,
                role = Role.Button,
                onClick = onClick,
            ),
    )
}

@Composable
private fun SignUpBackButton(
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
