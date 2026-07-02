package com.f1.quiket.feature.mypage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.mypage.R

private val ButtonShape = RoundedCornerShape(12.dp)

@Composable
fun AcornShopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Gray100, ButtonShape)
                .clip(ButtonShape)
                .background(White)
                .clickable(enabled = !isLocked) { onClick() }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_my_store),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "도토리 상점",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray900,
                )
            }
        }

        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(ButtonShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_my_lock),
                    contentDescription = "잠금",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AcornShopButtonPreview() {
    QuiketTheme {
        AcornShopButton(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AcornShopButtonLockedPreview() {
    QuiketTheme {
        AcornShopButton(onClick = {}, isLocked = true)
    }
}