package com.f1.quiket.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

@Composable
internal fun SignUpTermsDetailScreen(
    content: SignUpTermsDetailContent,
    onBackClick: () -> Unit,
    onAgreeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = remember(content.body) {
        content.body.toTermsSections()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        SignUpTermsTopBar(
            title = "약관 동의",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 92.dp, bottom = 124.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = content.title,
                color = Gray950,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
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
                        0f to White.copy(alpha = 0f),
                        0.2f to White,
                    ),
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 28.dp),
        ) {
            QuiketPrimaryButton(
                text = "확인 및 동의하기",
                onClick = onAgreeClick,
            )
        }
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
            color = Gray800,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
        if (section.body.isNotBlank()) {
            Text(
                text = section.body,
                color = Gray700,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpTermsDetailPreview() {
    QuiketTheme {
        SignUpTermsDetailScreen(
            content = SignUpTermsDetailType.Service.toContent(),
            onBackClick = {},
            onAgreeClick = {},
        )
    }
}
