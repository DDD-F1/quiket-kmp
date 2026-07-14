package com.f1.quiket.composeapp.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.designsystem.QuiketTopBar
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.appshell.resources.Res
import com.f1.quiket.appshell.resources.ic_coming_soon

@Composable
internal fun ReviewRoute(
    modifier: Modifier = Modifier,
) {
    ReviewPlaceholderScreen(modifier = modifier)
}

@Composable
private fun ReviewPlaceholderScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketBrown50),
    ) {
        QuiketTopBar(
            onNoteIconClick = {},
        )
        Text(
            text = "오답은 지금 공사 중이에요",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 36.dp, start = 16.dp),
        )
        Text(
            text = "곧 만나요!",
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, bottom = 30.dp),
            color = QuiketGray600,
        )
        Image(
            painter = painterResource(Res.drawable.ic_coming_soon),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp),
        )
    }
}
