package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.floating.R

@Composable
fun AddSubjectBottomCta(
    buttonText: String = "다음",
    isEnabled: Boolean,
    onSkipClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onSkipClick() }
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "지금은 건너뛰기",
                style = MaterialTheme.typography.bodySmall.copy(color = Gray500),
                modifier = Modifier
                    .clickable { onSkipClick() }
            )
            Icon(
                painter = painterResource(R.drawable.ic_addsubject_next),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Unspecified,
            )
        }
        QuiketPrimaryButton(
            text = buttonText,
            enabled = isEnabled,
            onClick = onNextClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddSubjectBottomCtaPreview() {
    QuiketTheme {
        AddSubjectBottomCta(
            buttonText = "다음",
            isEnabled = true,
            onSkipClick = {},
            onNextClick = {},
        )
    }
}