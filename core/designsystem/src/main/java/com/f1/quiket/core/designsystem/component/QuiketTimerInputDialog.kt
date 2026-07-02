package com.f1.quiket.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

enum class QuiketTimerInputUnit(
    val label: String,
) {
    Seconds("초"),
    Minutes("분"),
}

@Composable
fun QuiketTimerInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    selectedUnit: QuiketTimerInputUnit,
    onUnitClick: (QuiketTimerInputUnit) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    applyEnabled: Boolean = value.toIntOrNull()?.let { it > 0 } == true,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Black.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-97).dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        color = Gray950,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    BaseTextField(
                        value = value,
                        onValueChange = onValueChange,
                        hint = "내용을 입력해주세요",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            QuiketTimerUnitSegmentedControl(
                                selectedUnit = selectedUnit,
                                onUnitClick = onUnitClick,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuiketOutlinedButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    QuiketPrimaryButton(
                        text = "적용",
                        enabled = applyEnabled,
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        fillMaxWidth = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuiketTimerUnitSegmentedControl(
    selectedUnit: QuiketTimerInputUnit,
    onUnitClick: (QuiketTimerInputUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray100)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuiketTimerInputUnit.entries.forEach { unit ->
            val selected = selectedUnit == unit
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) White else Color.Transparent)
                    .clickable(
                        role = Role.RadioButton,
                        onClick = { onUnitClick(unit) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = unit.label,
                    color = if (selected) Gray950 else Gray400,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun QuiketTimerInputDialogPreview() {
    QuiketTheme {
        QuiketTimerInputDialog(
            title = "문제당 타이머 설정",
            value = "30",
            onValueChange = {},
            selectedUnit = QuiketTimerInputUnit.Seconds,
            onUnitClick = {},
            onDismiss = {},
            onApply = {},
        )
    }
}
