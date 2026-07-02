package com.f1.quiket.feature.mypage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.mypage.domain.model.FeedbackCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InquiryScreen(
    state: InquiryState,
    onIntent: (InquiryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState()),
    ) {
        InquiryTopBar(onBackClick = { onIntent(InquiryIntent.NavigateBack) })

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "문의 유형",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray950,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedbackCategory.entries
                    .filter { it != FeedbackCategory.Unknown }
                    .forEach { category ->
                        CategoryChip(
                            label = category.displayLabel(),
                            selected = state.selectedCategory == category,
                            onClick = { onIntent(InquiryIntent.SelectCategory(category)) },
                        )
                    }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "내용",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray950,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.body,
                onValueChange = { onIntent(InquiryIntent.UpdateBody(it)) },
                placeholder = { Text("문의 내용을 입력해주세요. (최대 1000자)", color = Gray700) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "답변 받을 이메일 (선택)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray950,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.replyEmail,
                onValueChange = { onIntent(InquiryIntent.UpdateReplyEmail(it)) },
                placeholder = { Text("이메일 주소", color = Gray700) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(InquiryIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isSubmitEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = "제출하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InquiryTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
            contentDescription = "뒤로",
            tint = Gray700,
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() },
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "문의",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray950,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) Orange500 else White
    val textColor = if (selected) White else Gray950
    val borderColor = if (selected) Orange500 else Gray100

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

private fun FeedbackCategory.displayLabel(): String = when (this) {
    FeedbackCategory.Feature -> "기능 제안"
    FeedbackCategory.Bug -> "버그 신고"
    FeedbackCategory.Inquiry -> "문의"
    FeedbackCategory.Other -> "기타"
    FeedbackCategory.Unknown -> "기타"
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "문의 — 초기 상태")
@Composable
private fun InquiryScreenDefaultPreview() {
    QuiketTheme {
        InquiryScreen(
            state = InquiryState(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "문의 — 내용 입력 중")
@Composable
private fun InquiryScreenFilledPreview() {
    QuiketTheme {
        InquiryScreen(
            state = InquiryState(
                selectedCategory = com.f1.quiket.feature.mypage.domain.model.FeedbackCategory.Bug,
                body = "퀴즈 결과 화면에서 앱이 종료됩니다.",
                replyEmail = "user@example.com",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "문의 — 제출 중")
@Composable
private fun InquiryScreenLoadingPreview() {
    QuiketTheme {
        InquiryScreen(
            state = InquiryState(
                isLoading = true,
                selectedCategory = com.f1.quiket.feature.mypage.domain.model.FeedbackCategory.Feature,
                body = "퀴즈 복습 알림 기능을 추가해주세요.",
            ),
            onIntent = {},
        )
    }
}