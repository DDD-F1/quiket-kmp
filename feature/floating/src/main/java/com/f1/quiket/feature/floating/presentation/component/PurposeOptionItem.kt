package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.UsagePurpose
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PurposeOptionItem(
    purpose: StudyPurpose,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Brown950 else Color.Transparent
    val backgroundColor = if (isSelected) Brown50 else Gray50

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = purpose.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Gray950,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = purpose.description,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Gray600,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PurposeOptionItemPreview() {
    QuiketTheme {
        PurposeOptionItem(
            purpose = StudyPurpose.EXAM,
            isSelected = false,
            onClick = {},
        )
    }
}

@Composable
fun PurposeSelectionItem(
    usagePurpose: UsagePurpose,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Brown950 else Color.Transparent
    val backgroundColor = if (isSelected) Brown50 else Gray50

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column {
            Text(
                text = usagePurpose.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Gray950,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = usagePurpose.description,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Gray600,
                ),
            )
        }
    }
}