package com.f1.quiket.feature.home.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Orange300
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.presentation.model.TooltipAlignment
import com.f1.quiket.feature.home.presentation.model.TutorialStep

@Composable
fun TutorialTooltip(
    step: TutorialStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(220.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Orange500),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${step.step}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = White
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = White)) {
                    append(step.startText)
                }
                withStyle(SpanStyle(color = Orange300)) {
                    append(step.highlightedText)
                }
                withStyle(SpanStyle(color = White)) {
                    append(step.endText)
                }
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}