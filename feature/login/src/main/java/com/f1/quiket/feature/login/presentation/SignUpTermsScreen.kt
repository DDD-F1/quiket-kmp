package com.f1.quiket.feature.login.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

@Composable
fun SignUpTermsScreen(
    termsState: SignUpTermsState,
    onBackClick: () -> Unit,
    onAllTermsClick: () -> Unit,
    onServiceTermsClick: () -> Unit,
    onServiceTermsDetailClick: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
    onPrivacyTermsDetailClick: () -> Unit,
    onMarketingTermsClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        SignUpTermsTopBar(
            title = "회원가입",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 92.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PasswordResetPageIndicator(currentPage = 2, pageCount = 3)
            }
            SignUpTermsTitleSection()
            SignUpTermsList(
                termsState = termsState,
                onAllTermsClick = onAllTermsClick,
                onServiceTermsClick = onServiceTermsClick,
                onServiceTermsDetailClick = onServiceTermsDetailClick,
                onPrivacyTermsClick = onPrivacyTermsClick,
                onPrivacyTermsDetailClick = onPrivacyTermsDetailClick,
                onMarketingTermsClick = onMarketingTermsClick,
            )
        }

        QuiketPrimaryButton(
            text = "회원가입",
            enabled = termsState.requiredAgreed,
            onClick = onSubmitClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 28.dp),
        )
    }
}

@Composable
internal fun SignUpTermsTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(White),
    ) {
        SignUpTermsBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 26.dp),
        )
        Text(
            text = title,
            color = Gray950,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 38.dp),
        )
    }
}

@Composable
private fun SignUpTermsBackButton(
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
private fun SignUpTermsTitleSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "약관에 동의해주세요",
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = "안전하게 공부하고 도토리 모으러 가요!",
            color = Gray700,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SignUpTermsList(
    termsState: SignUpTermsState,
    onAllTermsClick: () -> Unit,
    onServiceTermsClick: () -> Unit,
    onServiceTermsDetailClick: () -> Unit,
    onPrivacyTermsClick: () -> Unit,
    onPrivacyTermsDetailClick: () -> Unit,
    onMarketingTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SignUpAllTermsItem(
            checked = termsState.allAgreed,
            onClick = onAllTermsClick,
        )
        SignUpTermsItem(
            text = "서비스 이용 약관 (필수)",
            checked = termsState.serviceTermsAgreed,
            onClick = onServiceTermsClick,
            onDetailClick = onServiceTermsDetailClick,
        )
        SignUpTermsItem(
            text = "필수 개인 정보 수집 및 이용 (필수)",
            checked = termsState.privacyTermsAgreed,
            onClick = onPrivacyTermsClick,
            onDetailClick = onPrivacyTermsDetailClick,
        )
        SignUpTermsItem(
            text = "마케팅 프로모션 알림 수신 동의 (선택)",
            checked = termsState.marketingTermsAgreed,
            onClick = onMarketingTermsClick,
        )
    }
}

@Composable
private fun SignUpAllTermsItem(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .clickable(
                role = Role.Checkbox,
                onClick = onClick,
            )
            .background(if (checked) White else Gray50)
            .border(
                width = if (checked) 2.dp else 0.dp,
                color = if (checked) Brown950 else Gray50,
                shape = shape,
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SignUpTermsCheckbox(checked = checked)
        Text(
            text = "전체 동의",
            color = Gray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun SignUpTermsItem(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDetailClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 34.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 34.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    role = Role.Checkbox,
                    onClick = onClick,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SignUpTermsCheckbox(checked = checked)
            Text(
                text = text,
                color = Gray700,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        if (onDetailClick != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = onDetailClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                SignUpTermsChevron()
            }
        }
    }
}

@Composable
private fun SignUpTermsCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        if (checked) {
            drawCircle(
                color = Brown950,
                radius = size.minDimension / 2f,
                center = center,
            )
            drawLine(
                color = White,
                start = Offset(size.width * 0.28f, size.height * 0.52f),
                end = Offset(size.width * 0.43f, size.height * 0.68f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = White,
                start = Offset(size.width * 0.43f, size.height * 0.68f),
                end = Offset(size.width * 0.74f, size.height * 0.34f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        } else {
            drawCircle(
                color = Gray100,
                radius = size.minDimension / 2f,
                center = center,
            )
            drawCircle(
                color = Gray500,
                radius = size.minDimension / 2f - 0.75.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

@Composable
private fun SignUpTermsChevron(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 3.2.dp.toPx()
        drawLine(
            color = Gray500,
            start = Offset(size.width * 0.38f, size.height * 0.24f),
            end = Offset(size.width * 0.64f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Gray500,
            start = Offset(size.width * 0.64f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpTermsEmptyPreview() {
    QuiketTheme {
        SignUpTermsScreen(
            termsState = SignUpTermsState(),
            onBackClick = {},
            onAllTermsClick = {},
            onServiceTermsClick = {},
            onServiceTermsDetailClick = {},
            onPrivacyTermsClick = {},
            onPrivacyTermsDetailClick = {},
            onMarketingTermsClick = {},
            onSubmitClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpTermsAllAgreedPreview() {
    QuiketTheme {
        SignUpTermsScreen(
            termsState = SignUpTermsState(
                serviceTermsAgreed = true,
                privacyTermsAgreed = true,
                marketingTermsAgreed = true,
            ),
            onBackClick = {},
            onAllTermsClick = {},
            onServiceTermsClick = {},
            onServiceTermsDetailClick = {},
            onPrivacyTermsClick = {},
            onPrivacyTermsDetailClick = {},
            onMarketingTermsClick = {},
            onSubmitClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpTermsRequiredAgreedPreview() {
    QuiketTheme {
        SignUpTermsScreen(
            termsState = SignUpTermsState(
                serviceTermsAgreed = true,
                privacyTermsAgreed = true,
                marketingTermsAgreed = false,
            ),
            onBackClick = {},
            onAllTermsClick = {},
            onServiceTermsClick = {},
            onServiceTermsDetailClick = {},
            onPrivacyTermsClick = {},
            onPrivacyTermsDetailClick = {},
            onMarketingTermsClick = {},
            onSubmitClick = {},
        )
    }
}
