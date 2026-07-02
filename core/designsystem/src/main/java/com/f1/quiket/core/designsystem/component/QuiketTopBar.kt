package com.f1.quiket.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R
import com.f1.quiket.core.designsystem.theme.QuiketTheme

@Composable
fun QuiketTopBar(
    onNoteIconClick: () -> Unit,
    onNoteIconPositioned: (offset: Offset, size: IntSize) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_quiket_logo),
            contentDescription = "Home Quiket Logo",
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(width = 90.dp, height = 28.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onNoteIconClick) {
            Icon(
                painter = painterResource(R.drawable.ic_home_note),
                contentDescription = "Home Quiket Note",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .onGloballyPositioned { coordinates ->
                        onNoteIconPositioned(
                            coordinates.positionInRoot(),
                            coordinates.size,
                        )
                    },
            )
        }
        IconButton(onClick = {}) {
            Icon(
                painter = painterResource(R.drawable.ic_alert),
                contentDescription = "Home Quiket Alert",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuiketTopBarPreview() {
    QuiketTheme {
        QuiketTopBar(onNoteIconClick = {})
    }
}