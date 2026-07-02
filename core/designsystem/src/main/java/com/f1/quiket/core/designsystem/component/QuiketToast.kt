package com.f1.quiket.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.R
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.Tutorial

@Composable
fun QuiketToast(
    message: String,
    @DrawableRes icon: Int? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(
                color = Tutorial,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = message,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun QuiketToastPreview() {
    QuiketTheme {
        Column (
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuiketToast(
                "챕터를 수정할 수 없습니다.",
                icon = null,
                modifier = Modifier
            )
            QuiketToast(
                "챕터 수정이 성공했습니다.",
                icon = R.drawable.ic_upload_ok,
                modifier = Modifier
            )
            QuiketToast(
                "챕터 수정이 실패했습니다.",
                icon = R.drawable.ic_upload_fail,
                modifier = Modifier
            )
            QuiketToast(
                "챕터 수정이 실패했습니다.챕터 수정이 실패했습니다.챕터 수정이 실패했습니다.챕터 수정이 실패했습니다.",
                icon = R.drawable.ic_upload_fail,
                modifier = Modifier
            )
        }
    }
}