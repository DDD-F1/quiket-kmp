package com.f1.quiket.feature.review.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R
import com.f1.quiket.core.designsystem.component.QuiketTopBar
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.QuiketTheme

@Composable
fun ReviewScreen() {
    Scaffold(
        containerColor = Brown50,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            QuiketTopBar(
                onNoteIconClick = {}
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = "오답은 지금 공사 중이에요",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top=36.dp, start = 16.dp)
            )
            Text(
                text = "곧 만나요!",
                modifier = Modifier.padding(top=12.dp, start = 16.dp, bottom = 30.dp),
                color = Gray600
            )
            Icon(
                painter = painterResource(R.drawable.ic_coming_soon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxWidth()
                    .height(390.dp)
            )
        }
    }
}


@Preview
@Composable
fun ReviewScreenPreview() {
    QuiketTheme {
        ReviewScreen()
    }
}
