package com.f1.quiket.feature.floating.presentation.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.presentation.component.upload.UploadImage

private val PreviewBackground = Color(0xFF1A1A1A)

// A4 비율: 210 × 297 → width : height = 1 : √2 ≈ 1 : 1.414
private const val A4_RATIO = 1f / 1.414f

// 이미지별 크롭 상태 (scale, offsetX, offsetY)
private data class CropState(val scale: Float, val offsetX: Float, val offsetY: Float)

@Composable
fun ImagePreviewScreen(
    images: List<UploadImage>,
    initialIndex: Int,
    onBack: () -> Unit,
    onDelete: (index: Int) -> Unit,
    onSaveCrop: (index: Int, scale: Float, offsetX: Float, offsetY: Float) -> Unit = { _, _, _, _ -> },
) {
    var currentIndex by remember(initialIndex) {
        mutableStateOf(initialIndex.coerceIn(0, maxOf(0, images.size - 1)))
    }

    LaunchedEffect(images.size) {
        if (images.isEmpty()) return@LaunchedEffect
        if (currentIndex >= images.size) currentIndex = images.size - 1
    }

    // 이미지별 크롭 상태 맵 — UploadImage에 저장된 값으로 초기화
    val cropStates = remember {
        mutableStateMapOf<Int, CropState>().also { map ->
            images.forEachIndexed { idx, img ->
                map[idx] = CropState(img.cropScale, img.cropOffsetX, img.cropOffsetY)
            }
        }
    }

    // 현재 이미지의 scale / offset (초기값은 저장된 크롭 상태)
    var scale by remember {
        val s = images.getOrNull(initialIndex)?.cropScale ?: 1f
        mutableStateOf(s)
    }
    var offset by remember {
        val img = images.getOrNull(initialIndex)
        mutableStateOf(Offset(img?.cropOffsetX ?: 0f, img?.cropOffsetY ?: 0f))
    }

    // 이전 인덱스 추적 — 이미지 전환 시 이전 크롭 상태를 저장하기 위해
    var prevIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(currentIndex) {
        // 이전 이미지 크롭 상태 저장
        prevIndex?.let { prev ->
            val saved = CropState(scale, offset.x, offset.y)
            cropStates[prev] = saved
            onSaveCrop(prev, saved.scale, saved.offsetX, saved.offsetY)
        }
        // 새 이미지 크롭 상태 복원
        val s = cropStates[currentIndex] ?: CropState(1f, 0f, 0f)
        scale = s.scale
        offset = Offset(s.offsetX, s.offsetY)
        prevIndex = currentIndex
    }

    // 뒤로가기 — 현재 크롭 상태 저장 후 닫기
    val handleBack: () -> Unit = {
        val saved = CropState(scale, offset.x, offset.y)
        cropStates[currentIndex] = saved
        onSaveCrop(currentIndex, saved.scale, saved.offsetX, saved.offsetY)
        onBack()
    }

    // DisposableEffect: Dialog가 어떤 방식으로 닫혀도 (인앱 ← 버튼 / 시스템 백) 크롭 상태를 저장
    // rememberUpdatedState로 최신 scale/offset/index를 항상 읽도록 보장
    val latestIndex by rememberUpdatedState(currentIndex)
    val latestScale by rememberUpdatedState(scale)
    val latestOffset by rememberUpdatedState(offset)
    DisposableEffect(Unit) {
        onDispose {
            onSaveCrop(latestIndex, latestScale, latestOffset.x, latestOffset.y)
        }
    }

    val thumbnailListState = rememberLazyListState()
    LaunchedEffect(currentIndex) {
        if (images.isNotEmpty()) thumbnailListState.animateScrollToItem(currentIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PreviewBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TopBar ──────────────────────────────────────────────────
            PreviewTopBar(
                onBack = handleBack,
                onDelete = { if (images.isNotEmpty()) onDelete(currentIndex) },
            )

            // ── A4 비율 이미지 뷰어 ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(A4_RATIO)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .pointerInput(currentIndex) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) offset += pan
                            }
                        }
                        .pointerInput(currentIndex) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (images.isNotEmpty()) {
                        AsyncImage(
                            model = images[currentIndex].uri,
                            contentDescription = "이미지 ${currentIndex + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y,
                                    clip = true,
                                ),
                        )
                    }
                }
            }

            // ── 썸네일 스트립 ────────────────────────────────────────────
            LazyRow(
                state = thumbnailListState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(images) { idx, image ->
                    val isSelected = idx == currentIndex
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = 2.dp,
                                color = if (isSelected) White else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                            )
                            .clickable { currentIndex = idx },
                    ) {
                        AsyncImage(
                            model = image.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // ── 하단 네비게이션 (white 배경, 양 끝 정렬) ──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Black)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 이전 버튼 (white 원)
                val canPrev = currentIndex > 0
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(White)
                        .border(1.dp, if (canPrev) Gray300 else Gray100, CircleShape)
                        .clickable(enabled = canPrev) { currentIndex-- },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_common_back),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 이미지 n/n Chip (white 배경)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(White)
                        .border(1.dp, Gray300, RoundedCornerShape(100.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "이미지 ${currentIndex + 1}/${images.size}",
                        color = Gray950,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // 다음 버튼 (white 원)
                val canNext = currentIndex < images.size - 1
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(White)
                        .border(1.dp, if (canNext) Gray300 else Gray100, CircleShape)
                        .clickable(enabled = canNext) { currentIndex++ },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_common_next),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@Composable
private fun PreviewTopBar(
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
                contentDescription = "뒤로가기",
                tint = White,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "미리보기",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = White,
            ),
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_detail_remove),
                contentDescription = "이미지 삭제",
                tint = White,
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "이미지 미리보기")
@Composable
private fun ImagePreviewScreenPreview() {
    QuiketTheme {
        ImagePreviewScreen(
            images = listOf(
                UploadImage(id = 0, uri = Uri.EMPTY, sizeLabel = "1.2MB"),
                UploadImage(id = 1, uri = Uri.EMPTY, sizeLabel = "890KB"),
                UploadImage(id = 2, uri = Uri.EMPTY, sizeLabel = "2.1MB"),
            ),
            initialIndex = 0,
            onBack = {},
            onDelete = {},
        )
    }
}