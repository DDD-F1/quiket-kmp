package com.f1.quiket.feature.floating.presentation.component.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.modifier.dashedBorder
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R
import kotlin.math.roundToInt

private const val MAX_SECTIONS = 10
private const val ROW_HEIGHT_DP = 56  // BaseTextField(48dp) + spacedBy(8dp) ≈ 56dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPartBottomSheet(
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sections by remember { mutableStateOf(listOf("")) }

    // 드래그 상태
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            // ── 닫기 버튼 ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_addsubject_close),
                        contentDescription = "닫기",
                        tint = Gray700,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "파트명 직접 입력",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Gray950,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "분류하고 싶은 파트명을 순차적으로 입력해주세요",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Gray800,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 섹션 목록 (스크롤) ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEachIndexed { index, value ->
                    val isDragging = draggingIndex == index

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffsetY else 0f
                                shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                                shape = RoundedCornerShape(8.dp)
                                clip = isDragging
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // ── 드래그 핸들 ──────────────────────────────────────
                        Icon(
                            painter = painterResource(R.drawable.ic_self_change),
                            contentDescription = "순서 변경",
                            tint = if (isDragging) Brown950 else Gray400,
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(index) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                        },
                                        onDragEnd = {
                                            val idx = draggingIndex ?: return@detectDragGestures
                                            val rowHeightPx = ROW_HEIGHT_DP.dp.toPx()
                                            val steps = (dragOffsetY / rowHeightPx).roundToInt()
                                            val newIdx = (idx + steps).coerceIn(0, sections.size - 1)
                                            if (newIdx != idx) {
                                                val mutable = sections.toMutableList()
                                                val item = mutable.removeAt(idx)
                                                mutable.add(newIdx, item)
                                                sections = mutable
                                            }
                                            draggingIndex = null
                                            dragOffsetY = 0f
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            dragOffsetY = 0f
                                        },
                                    )
                                },
                        )

                        // ── 번호 뱃지 ─────────────────────────────────────────
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Gray100, RoundedCornerShape(1000.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Gray800,
                                ),
                            )
                        }

                        // ── 텍스트 입력 ───────────────────────────────────────
                        BaseTextField(
                            value = value,
                            onValueChange = { newValue ->
                                sections = sections.toMutableList().also { it[index] = newValue }
                            },
                            hint = "내용을 입력해주세요",
                            modifier = Modifier.weight(1f),
                        )

                        // ── 삭제 버튼 ─────────────────────────────────────────
                        IconButton(
                            onClick = {
                                if (sections.size > 1) {
                                    sections = sections.toMutableList().also { it.removeAt(index) }
                                }
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_detail_remove),
                                contentDescription = "섹션 삭제",
                                tint = Gray400,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ── + 섹션 추가하기 버튼 ────────────────────────────────────
                val canAdd = sections.size < MAX_SECTIONS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray50)
                        .dashedBorder(
                            color = Gray300,
                            strokeWidth = 1.5.dp,
                            cornerRadius = 12.dp,
                            dashLength = 5.dp,
                            gapLength = 4.dp,
                        )
                        .then(
                            if (canAdd) Modifier.clickable { sections = sections + "" }
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+ 섹션 추가하기 (${sections.size}/$MAX_SECTIONS)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = if (canAdd) Gray900 else Gray400,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 취소 / 적용 버튼 ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                    border = BorderStroke(2.dp, Gray950),
                ) {
                    Text(
                        "취소",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Gray950,
                    )
                }
                Button(
                    onClick = { onApply(sections.filter { it.isNotBlank() }) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown950,
                        contentColor = White,
                    ),
                ) {
                    Text(
                        "적용",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "파트명 직접 입력 바텀시트")
@Composable
private fun ManualPartBottomSheetPreview() {
    QuiketTheme {
        ManualPartBottomSheet(
            onDismiss = {},
            onApply = {},
        )
    }
}
