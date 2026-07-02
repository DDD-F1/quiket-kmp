package com.f1.quiket.composeapp.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.mypage.PrivacyPolicyRawText
import com.f1.quiket.composeapp.mypage.ServiceTermsRawText

@Composable
internal fun SignUpTermsRoute(
    onBackClick: () -> Unit,
    onSubmitClick: (SignUpTermsState) -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    initialTermsState: SignUpTermsState = SignUpTermsState(),
) {
    var termsState by remember(initialTermsState) { mutableStateOf(initialTermsState) }
    var detailType by remember { mutableStateOf<SignUpTermsDetailType?>(null) }

    val currentDetailType = detailType
    if (currentDetailType != null) {
        SignUpTermsDetailScreen(
            content = currentDetailType.toContent(),
            onBackClick = { detailType = null },
            onAgreeClick = {
                termsState = when (currentDetailType) {
                    SignUpTermsDetailType.Service -> termsState.copy(serviceTermsAgreed = true)
                    SignUpTermsDetailType.Privacy -> termsState.copy(privacyTermsAgreed = true)
                }
                detailType = null
            },
            modifier = modifier,
        )
    } else {
        SignUpTermsScreen(
            termsState = termsState,
            onBackClick = onBackClick,
            onAllTermsClick = {
                val next = !termsState.allAgreed
                termsState = termsState.copy(
                    serviceTermsAgreed = next,
                    privacyTermsAgreed = next,
                    marketingTermsAgreed = next,
                )
            },
            onServiceTermsClick = {
                termsState = termsState.copy(serviceTermsAgreed = !termsState.serviceTermsAgreed)
            },
            onServiceTermsDetailClick = { detailType = SignUpTermsDetailType.Service },
            onPrivacyTermsClick = {
                termsState = termsState.copy(privacyTermsAgreed = !termsState.privacyTermsAgreed)
            },
            onPrivacyTermsDetailClick = { detailType = SignUpTermsDetailType.Privacy },
            onMarketingTermsClick = {
                termsState = termsState.copy(marketingTermsAgreed = !termsState.marketingTermsAgreed)
            },
            onSubmitClick = { onSubmitClick(termsState) },
            isSubmitting = isSubmitting,
            errorMessage = errorMessage,
            modifier = modifier,
        )
    }
}

@Composable
private fun SignUpTermsScreen(
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
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
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
                .padding(top = 124.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SignUpTermsPageIndicator(currentPage = 2, pageCount = 3)
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
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = com.f1.quiket.composeapp.designsystem.QuiketNegative,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }

        QuiketPrimaryButton(
            text = if (isSubmitting) "가입 중..." else "회원가입",
            enabled = termsState.requiredAgreed && !isSubmitting,
            onClick = onSubmitClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )
    }
}

@Composable
private fun SignUpTermsDetailScreen(
    content: SignUpTermsDetailContent,
    onBackClick: () -> Unit,
    onAgreeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = remember(content.body) { content.body.toTermsSections() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        SignUpTermsTopBar(
            title = "약관 동의",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 124.dp, bottom = 124.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = content.title,
                color = QuiketGray950,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            sections.forEach { section ->
                TermsDetailSection(section = section)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to QuiketWhite.copy(alpha = 0f),
                        0.25f to QuiketWhite,
                    ),
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 28.dp),
        ) {
            QuiketPrimaryButton(
                text = "확인 및 동의하기",
                enabled = true,
                onClick = onAgreeClick,
                modifier = Modifier.zIndex(1f),
            )
        }
    }
}

@Composable
private fun SignUpTermsTopBar(
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
private fun SignUpBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .semantics { contentDescription = "뒤로" }
            .clickable(role = Role.Button, onClick = onClick),
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
private fun SignUpTermsPageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (index == currentPage) QuiketBrown950 else QuiketGray100),
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
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = "안전하게 공부하고 도토리 모으러 가요!",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
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
            .clickable(role = Role.Checkbox, onClick = onClick)
            .background(if (checked) QuiketWhite else QuiketGray100.copy(alpha = 0.45f))
            .border(
                width = if (checked) 2.dp else 0.dp,
                color = if (checked) QuiketBrown950 else QuiketGray100,
                shape = shape,
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SignUpTermsCheckbox(checked = checked)
        Text(
            text = "전체 동의",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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
            .heightIn(min = 40.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Checkbox, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SignUpTermsCheckbox(checked = checked)
            Text(
                text = text,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp),
            )
        }
        if (onDetailClick != null) {
            val detailContentDescription = "$text 보기"
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onDetailClick)
                    .semantics { contentDescription = detailContentDescription },
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
                color = QuiketBrown950,
                radius = size.minDimension / 2f,
                center = center,
            )
            drawLine(
                color = QuiketWhite,
                start = Offset(size.width * 0.28f, size.height * 0.52f),
                end = Offset(size.width * 0.43f, size.height * 0.68f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketWhite,
                start = Offset(size.width * 0.43f, size.height * 0.68f),
                end = Offset(size.width * 0.74f, size.height * 0.34f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        } else {
            drawCircle(
                color = QuiketWhite,
                radius = size.minDimension / 2f,
                center = center,
            )
            drawCircle(
                color = QuiketGray400,
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
            color = QuiketGray400,
            start = Offset(size.width * 0.38f, size.height * 0.24f),
            end = Offset(size.width * 0.64f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = QuiketGray400,
            start = Offset(size.width * 0.64f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun TermsDetailSection(
    section: TermsSection,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = section.title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        if (section.body.isNotBlank()) {
            Text(
                text = section.body,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

internal data class SignUpTermsState(
    val serviceTermsAgreed: Boolean = false,
    val privacyTermsAgreed: Boolean = false,
    val marketingTermsAgreed: Boolean = false,
) {
    val requiredAgreed: Boolean
        get() = serviceTermsAgreed && privacyTermsAgreed

    val allAgreed: Boolean
        get() = serviceTermsAgreed && privacyTermsAgreed && marketingTermsAgreed
}

private enum class SignUpTermsDetailType {
    Service,
    Privacy,
}

private data class SignUpTermsDetailContent(
    val title: String,
    val body: String,
)

private fun SignUpTermsDetailType.toContent(): SignUpTermsDetailContent =
    when (this) {
        SignUpTermsDetailType.Service -> ServiceTermsRawText.toDetailContent(
            fallbackTitle = "서비스 이용 약관",
        )

        SignUpTermsDetailType.Privacy -> PrivacyPolicyRawText.toDetailContent(
            fallbackTitle = "필수 개인 정보 수집 및 이용",
        )
    }

private fun String.toDetailContent(fallbackTitle: String): SignUpTermsDetailContent {
    val lines = trim().lines()
    return SignUpTermsDetailContent(
        title = lines.firstOrNull().orEmpty().ifBlank { fallbackTitle },
        body = lines.drop(1).joinToString("\n").trim(),
    )
}

private data class TermsSection(
    val title: String,
    val body: String,
)

private fun String.toTermsSections(): List<TermsSection> {
    val sections = mutableListOf<TermsSection>()
    var currentTitle: String? = null
    val currentBody = mutableListOf<String>()

    fun flush() {
        val title = currentTitle ?: return
        sections += TermsSection(
            title = title,
            body = currentBody.joinToString("\n").trim(),
        )
        currentBody.clear()
    }

    lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach { line ->
            if (line.isTermsSectionTitle()) {
                flush()
                currentTitle = line
            } else {
                currentBody += line
            }
        }

    flush()
    return sections
}

private fun String.isTermsSectionTitle(): Boolean =
    this == "부칙" || ArticleTitleRegex.matches(this)

private val ArticleTitleRegex = Regex("""^제\d+조\s*\([^)]+\)$""")
