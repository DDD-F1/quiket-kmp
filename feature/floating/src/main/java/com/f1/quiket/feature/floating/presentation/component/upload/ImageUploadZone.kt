package com.f1.quiket.feature.floating.presentation.component.upload

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.f1.quiket.core.designsystem.modifier.dashedBorder
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Brown300
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White

private const val MAX_IMAGES = 15
private const val GRID_COLUMNS = 3

data class UploadImage(
    val id: Int,
    val uri: Uri,
    val sizeLabel: String,
    val cropScale: Float = 1f,
    val cropOffsetX: Float = 0f,
    val cropOffsetY: Float = 0f,
)

@Composable
fun ImageUploadZone(
    images: SnapshotStateList<UploadImage>,
    onRemove: (index: Int) -> Unit,
    onImageClick: (index: Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Android 13+ → Photo Picker (권한 불필요)
    // Android 12 이하 → ACTION_GET_CONTENT + READ_EXTERNAL_STORAGE 권한
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGES),
    ) { uris ->
        val remaining = MAX_IMAGES - images.size
        uris.take(remaining).forEach { uri ->
            val sizeBytes = runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
            }.getOrNull() ?: 0L
            val sizeLabel = when {
                sizeBytes >= 1_048_576 -> "%.1fMB".format(sizeBytes / 1_048_576f)
                sizeBytes >= 1_024 -> "${sizeBytes / 1_024}KB"
                else -> "${sizeBytes}B"
            }
            images.add(UploadImage(id = images.size, uri = uri, sizeLabel = sizeLabel))
        }
    }

    // Android 12 이하 레거시 피커
    val legacyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        val remaining = MAX_IMAGES - images.size
        uris.take(remaining).forEach { uri ->
            val sizeBytes = runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
            }.getOrNull() ?: 0L
            val sizeLabel = when {
                sizeBytes >= 1_048_576 -> "%.1fMB".format(sizeBytes / 1_048_576f)
                sizeBytes >= 1_024 -> "${sizeBytes / 1_024}KB"
                else -> "${sizeBytes}B"
            }
            images.add(UploadImage(id = images.size, uri = uri, sizeLabel = sizeLabel))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            legacyPicker.launch("image/*")
        } else {
            showPermissionDeniedDialog = true
        }
    }

    fun launchPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ → Photo Picker, 권한 불필요
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                legacyPicker.launch("image/*")
            } else {
                permissionLauncher.launch(permission)
            }
        }
    }

    Column(modifier = modifier) {
        EmptyImageZone(onAdd = ::launchPicker)

        if (images.isEmpty()) {
            Text(
                text = "정확한 결과를 위해 손글씨보다는 인쇄된 이미지를 권장드려요.",
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                textAlign = TextAlign.Center,
            )
        }

        if (images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp)
            )
            Text(
                text = "이미지 순서대로 AI가 읽어줘요. 드래그로 서로 위치를 바꿀 수 있어요.",
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))
            DraggableImageGrid(
                images = images,
                onRemove = onRemove,
                onImageClick = onImageClick,
                onAddMore = null,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            )
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("사진 접근 권한 필요") },
            text = { Text("이미지를 업로드하려면 갤러리 접근 권한이 필요해요.\n설정에서 권한을 허용해 주세요.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("확인")
                }
            },
        )
    }
}

// ─── 빈 상태 ──────────────────────────────────────────────────────────────────

@Composable
private fun EmptyImageZone(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50)
            .dashedBorder(
                color = Gray300,
                strokeWidth = 1.5.dp,
                cornerRadius = 16.dp,
                dashLength = 5.dp,
                gapLength = 4.dp,
            )
            .clickable { onAdd() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(White),
                )
                Icon(
                    painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_upload_image),
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이미지를 순서대로 선택해주세요",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Gray900,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PNG, JPG 최대 ${MAX_IMAGES}장",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400,
            )
        }
    }
}

// ─── 드래그 그리드 ────────────────────────────────────────────────────────────

@Composable
private fun DraggableImageGrid(
    images: SnapshotStateList<UploadImage>,
    onRemove: (index: Int) -> Unit,
    onImageClick: (index: Int) -> Unit = {},
    onAddMore: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var accDrag by remember { mutableStateOf(Offset.Zero) }
    val itemRects = remember { mutableStateMapOf<Int, Rect>() }

    val totalItems = images.size + (if (onAddMore != null) 1 else 0)
    val rows = (totalItems + GRID_COLUMNS - 1) / GRID_COLUMNS

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (col in 0 until GRID_COLUMNS) {
                    val index = row * GRID_COLUMNS + col
                    when {
                        index < images.size -> {
                            val isDragging = draggingIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .onGloballyPositioned { coords ->
                                        val pos = coords.positionInParent()
                                        itemRects[index] = Rect(
                                            pos.x, pos.y,
                                            pos.x + coords.size.width,
                                            pos.y + coords.size.height,
                                        )
                                    }
                                    .pointerInput(index) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingIndex = index
                                                accDrag = Offset.Zero
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                accDrag += dragAmount
                                                val currentRect = itemRects[draggingIndex]
                                                    ?: return@detectDragGesturesAfterLongPress
                                                val dragCenter = currentRect.center + accDrag
                                                val target = itemRects.entries
                                                    .filter { it.key != draggingIndex && it.key < images.size }
                                                    .firstOrNull { (_, rect) ->
                                                        rect.contains(
                                                            dragCenter
                                                        )
                                                    }
                                                if (target != null) {
                                                    val from = draggingIndex!!
                                                    val to = target.key
                                                    val temp = images[from]
                                                    images[from] = images[to]
                                                    images[to] = temp
                                                    draggingIndex = to
                                                    accDrag = Offset.Zero
                                                }
                                            },
                                            onDragEnd = {
                                                draggingIndex = null; accDrag = Offset.Zero
                                            },
                                            onDragCancel = {
                                                draggingIndex = null; accDrag = Offset.Zero
                                            },
                                        )
                                    }
                                    .graphicsLayer {
                                        if (isDragging) {
                                            scaleX = 1.06f; scaleY = 1.06f; shadowElevation = 16f
                                        }
                                    },
                            ) {
                                ImageTile(
                                    image = images[index],
                                    order = index + 1,
                                    onRemove = { onRemove(index) },
                                    onImageClick = { onImageClick(index) },
                                )
                            }
                        }

                        index == images.size && onAddMore != null -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Gray300, RoundedCornerShape(8.dp))
                                    .background(White)
                                    .clickable { onAddMore() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("+", fontSize = 24.sp, color = Gray400)
                                    Text("추가", fontSize = 11.sp, color = Gray400)
                                }
                            }
                        }

                        else -> Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─── 이미지 타일 ──────────────────────────────────────────────────────────────

@Composable
private fun ImageTile(
    image: UploadImage,
    order: Int,
    onRemove: () -> Unit,
    onImageClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onImageClick() },
    ) {
        AsyncImage(
            model = image.uri,
            contentDescription = "이미지 $order",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = image.cropScale,
                    scaleY = image.cropScale,
                    translationX = image.cropOffsetX,
                    translationY = image.cropOffsetY,
                    clip = true,
                ),
        )

        // 순서 번호 (좌상단)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Brown300),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$order",
                color = White,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        // X 버튼 (우상단)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Black.copy(alpha = 0.5f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕", fontSize = 10.sp, color = White,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // 파일 크기 (좌하단)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Black.copy(alpha = 0.4f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        ) {
            Text(text = image.sizeLabel, fontSize = 9.sp, color = White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "이미지 업로드 — 빈 상태")
@Composable
private fun ImageUploadZoneEmptyPreview() {
    QuiketTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brown50),
        ) {
            ImageUploadZone(
                images = remember { SnapshotStateList() },
                onRemove = {},
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "이미지 업로드 — 이미지 있음")
@Composable
private fun ImageUploadZoneWithImagesPreview() {
    val images = remember {
        SnapshotStateList<UploadImage>().also {
            it.add(UploadImage(id = 0, uri = Uri.EMPTY, sizeLabel = "1.2MB"))
            it.add(UploadImage(id = 1, uri = Uri.EMPTY, sizeLabel = "890KB"))
            it.add(UploadImage(id = 2, uri = Uri.EMPTY, sizeLabel = "2.1MB"))
            it.add(UploadImage(id = 3, uri = Uri.EMPTY, sizeLabel = "540KB"))
        }
    }
    QuiketTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brown50),
        ) {
            ImageUploadZone(
                images = images,
                onRemove = {},
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}
