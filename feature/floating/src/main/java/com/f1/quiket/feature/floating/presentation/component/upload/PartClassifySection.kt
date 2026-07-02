package com.f1.quiket.feature.floating.presentation.component.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.theme.Brown100
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray950

enum class PartClassifyMethod { AI, MANUAL }

@Composable
fun PartClassifySection(
    selected: PartClassifyMethod,
    onSelect: (PartClassifyMethod) -> Unit,
    onManualApply: (List<String>) -> Unit = {},
) {
    var showManualSheet by remember { mutableStateOf(false) }

    if (showManualSheet) {
        ManualPartBottomSheet(
            onDismiss = { showManualSheet = false },
            onApply = { sections ->
                onSelect(PartClassifyMethod.MANUAL)
                onManualApply(sections)
                showManualSheet = false
            },
        )
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "파트 분류 방법",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_info),
                contentDescription = "파트 분류 방법 안내",
                tint = Gray400,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PartClassifyCard(
                iconRes = com.f1.quiket.feature.floating.R.drawable.ic_upload_ai,
                label = "AI에게 맡기기",
                isSelected = selected == PartClassifyMethod.AI,
                onClick = { onSelect(PartClassifyMethod.AI) },
                modifier = Modifier.weight(1f),
            )
            PartClassifyCard(
                iconRes = com.f1.quiket.feature.floating.R.drawable.ic_upload_self,
                label = "직접 분류하기",
                isSelected = selected == PartClassifyMethod.MANUAL,
                onClick = { showManualSheet = true },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PartClassifyCard(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Brown50 else Gray50)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Brown950 else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) Brown100 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Gray950,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
