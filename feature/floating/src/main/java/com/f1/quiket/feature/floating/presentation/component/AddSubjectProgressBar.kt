package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.QuiketTheme

@Composable
fun AddSubjectProgressBar(
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val totalStep = 3
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalStep) { index ->
            val stepNumber = index + 1
            val isCompleted = stepNumber <= currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCompleted) Brown950 else Gray100)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$currentStep/$totalStep",
            style = MaterialTheme.typography.bodySmall.copy(color = Gray900),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddSubjectProgressBarPreview() {
    QuiketTheme {
        AddSubjectProgressBar(currentStep = 2)
    }
}