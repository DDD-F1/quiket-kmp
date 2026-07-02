package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.floating.R

@Composable
fun AddSubjectTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
                contentDescription = "뒤로가기",
                tint = Gray950,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "과목 추가",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Gray950,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
            contentDescription = "Null Icon",
            tint = Color.Transparent,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddSubjectTopBarPreview() {
    QuiketTheme {
        AddSubjectTopBar(
            onBackClick = {}
        )
    }
}
