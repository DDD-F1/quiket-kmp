package com.f1.quiket.feature.floating.presentation.component.upload

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Negative

@Composable
fun ChapterNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    nextChapterNumber: Int,
) {
    Column {
        Row {
            Text(
                text = "챕터명",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = " *",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Negative,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        BaseTextField(
            value = value,
            onValueChange = onValueChange,
            hint = "챕터명을 입력해주세요",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "챕터 ${nextChapterNumber}로 저장될 예정이에요",
            style = MaterialTheme.typography.labelSmall.copy(color = Gray500),
        )
    }
}
