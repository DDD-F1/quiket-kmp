package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.subject.domain.model.*

import org.koin.compose.koinInject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray800
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.subject.presentation.TextLectureUploadStateHolder
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_acorn
import com.f1.quiket.core.designsystem.resources.ic_info
import com.f1.quiket.core.designsystem.resources.ic_qring_profile
import com.f1.quiket.feature.subject.resources.Res
import com.f1.quiket.feature.subject.resources.ic_upload_image
import com.f1.quiket.feature.subject.resources.ic_upload_ok
import com.f1.quiket.feature.subject.resources.ic_upload_pdf

@Composable
internal fun TextUploadSection(
    lectureText: String,
    enabled: Boolean,
    onLectureTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "강의 텍스트",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        LectureTextInput(
            value = lectureText,
            onValueChange = onLectureTextChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${lectureText.length.toUploadCountLabel()}/${MaxTextUploadLength.toUploadCountLabel()}",
                color = QuiketGray400,
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
internal fun BinaryUploadSection(
    selectedFiles: List<PickedUploadFile>,
    enabled: Boolean,
    onPickFileClick: () -> Unit,
    onRemoveFileClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        UploadDropZone(
            icon = Res.drawable.ic_upload_pdf,
            text = "업로드할 파일 선택해주세요",
            description = "PDF 최대 50MB",
            enabled = enabled,
            onClick = onPickFileClick,
        )

        selectedFiles.forEachIndexed { index, file ->
            UploadFileCard(
                file = file,
                enabled = enabled,
                onRemoveClick = { onRemoveFileClick(index) },
            )
        }
    }
}

@Composable
internal fun ImageUploadSection(
    selectedFiles: List<PickedUploadFile>,
    enabled: Boolean,
    canRemove: Boolean,
    onPickFileClick: () -> Unit,
    onRemoveFileClick: (Int) -> Unit,
    onMoveFile: (from: Int, to: Int) -> Unit,
    onPreviewFileClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        UploadDropZone(
            icon = Res.drawable.ic_upload_image,
            text = if (selectedFiles.isEmpty()) {
                "이미지를 순서대로 선택해주세요"
            } else {
                "이미지 더 추가하기"
            },
            description = "PNG, JPG 최대 ${MaxImageUploadCount}장",
            enabled = enabled,
            onClick = onPickFileClick,
        )

        Text(
            text = if (selectedFiles.isEmpty()) {
                "정확한 결과를 위해 손글씨보다는 인쇄된 이미지를 권장드려요."
            } else {
                "이미지 순서대로 AI가 읽어요. 순서가 맞는지 확인해주세요."
            },
            color = QuiketGray600,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
        )

        if (selectedFiles.isNotEmpty()) {
            DraggableImageGrid(
                files = selectedFiles,
                canMove = canRemove,
                canRemove = canRemove,
                onMoveFile = onMoveFile,
                onPreviewFileClick = onPreviewFileClick,
                onRemoveFileClick = onRemoveFileClick,
            )
        }
    }
}

@Composable
internal fun DraggableImageGrid(
    files: List<PickedUploadFile>,
    canMove: Boolean,
    canRemove: Boolean,
    onMoveFile: (from: Int, to: Int) -> Unit,
    onPreviewFileClick: (Int) -> Unit,
    onRemoveFileClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingIndex by remember(files) { mutableStateOf<Int?>(null) }
    var accumulatedDrag by remember(files) { mutableStateOf(Offset.Zero) }
    val itemRects = remember(files) { mutableStateMapOf<Int, Rect>() }
    val rows = (files.size + ImageGridColumnCount - 1) / ImageGridColumnCount

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (column in 0 until ImageGridColumnCount) {
                    val index = row * ImageGridColumnCount + column
                    if (index < files.size) {
                        val isDragging = draggingIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.positionInParent()
                                    itemRects[index] = Rect(
                                        left = position.x,
                                        top = position.y,
                                        right = position.x + coordinates.size.width,
                                        bottom = position.y + coordinates.size.height,
                                    )
                                }
                                .then(
                                    if (canMove && files.size > 1) {
                                        Modifier.pointerInput(files, index) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingIndex = index
                                                    accumulatedDrag = Offset.Zero
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val fromIndex = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                                    accumulatedDrag += dragAmount
                                                    val currentRect = itemRects[fromIndex]
                                                        ?: return@detectDragGesturesAfterLongPress
                                                    val dragCenter = currentRect.center + accumulatedDrag
                                                    val targetIndex = itemRects.entries
                                                        .firstOrNull { (candidateIndex, rect) ->
                                                            candidateIndex != fromIndex &&
                                                                candidateIndex < files.size &&
                                                                rect.contains(dragCenter)
                                                        }
                                                        ?.key

                                                    if (targetIndex != null) {
                                                        onMoveFile(fromIndex, targetIndex)
                                                        draggingIndex = targetIndex
                                                        accumulatedDrag = Offset.Zero
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggingIndex = null
                                                    accumulatedDrag = Offset.Zero
                                                },
                                                onDragCancel = {
                                                    draggingIndex = null
                                                    accumulatedDrag = Offset.Zero
                                                },
                                            )
                                        }
                                    } else {
                                        Modifier
                                    },
                                )
                                .graphicsLayer {
                                    if (isDragging) {
                                        scaleX = 1.04f
                                        scaleY = 1.04f
                                        shadowElevation = 14f
                                    }
                                },
                        ) {
                            ImageUploadCard(
                                file = files[index],
                                index = index,
                                canRemove = canRemove,
                                onPreviewClick = { onPreviewFileClick(index) },
                                onRemoveClick = { onRemoveFileClick(index) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun ImageUploadCard(
    file: PickedUploadFile,
    index: Int,
    canRemove: Boolean,
    onPreviewClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.78f),
        color = QuiketWhite,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, QuiketGray100),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(role = Role.Button, onClick = onPreviewClick),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(QuiketBrown50),
                    contentAlignment = Alignment.Center,
                ) {
                    DecodedUploadImage(
                        file = file,
                        fallbackIndex = index,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = file.name,
                    color = QuiketGray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = file.sizeBytes.toUploadSizeLabel(),
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(QuiketWhite)
                    .border(1.dp, QuiketGray300, CircleShape)
                    .then(
                        if (canRemove) {
                            Modifier.clickable(
                                role = Role.Button,
                                onClick = onRemoveClick,
                            )
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×",
                    color = if (canRemove) QuiketGray700 else QuiketGray300,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
internal fun DecodedUploadImage(
    file: PickedUploadFile,
    fallbackIndex: Int,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(file) {
        file.previewBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = file.name,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Column(
            modifier = modifier.background(QuiketBrown50),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${fallbackIndex + 1}",
                color = QuiketBrown950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = file.imageTypeLabel(),
                color = QuiketGray700,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
internal fun ImagePreviewDialog(
    files: List<PickedUploadFile>,
    initialIndex: Int,
    canRemove: Boolean,
    onDismiss: () -> Unit,
    onRemoveFileClick: (Int) -> Unit,
) {
    if (files.isEmpty()) return
    var currentIndex by remember(files, initialIndex) {
        mutableStateOf(initialIndex.coerceIn(0, files.lastIndex))
    }
    val currentFile = files[currentIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PreviewScrim)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PreviewTextButton(
                    text = "닫기",
                    onClick = onDismiss,
                )
                Text(
                    text = "${currentIndex + 1}/${files.size}",
                    color = QuiketWhite,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                )
                PreviewTextButton(
                    text = "삭제",
                    enabled = canRemove,
                    onClick = { onRemoveFileClick(currentIndex) },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                DecodedUploadImage(
                    file = currentFile,
                    fallbackIndex = currentIndex,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                text = currentFile.name,
                color = QuiketWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 18.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(files) { index, file ->
                    val selected = index == currentIndex
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) QuiketWhite else QuiketGray700,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .background(Color.Black)
                            .clickable(role = Role.Button) { currentIndex = index },
                    ) {
                        DecodedUploadImage(
                            file = file,
                            fallbackIndex = index,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PreviewTextButton(
                    text = "이전",
                    enabled = currentIndex > 0,
                    onClick = { currentIndex -= 1 },
                )
                PreviewTextButton(
                    text = "다음",
                    enabled = currentIndex < files.lastIndex,
                    onClick = { currentIndex += 1 },
                )
            }
        }
    }
}

@Composable
internal fun PreviewTextButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) QuiketWhite else QuiketGray700)
            .then(
                if (enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) QuiketGray950 else QuiketGray400,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
internal fun UploadDropZone(
    icon: DrawableResource,
    text: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(QuiketGray50)
            .dashedBorder(
                color = QuiketGray300,
                strokeWidth = 1.5.dp,
                cornerRadius = 16.dp,
                dashLength = 5.dp,
                gapLength = 4.dp,
            )
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(QuiketWhite),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = QuiketGray400,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
internal fun UploadFileCard(
    file: PickedUploadFile,
    enabled: Boolean,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(QuiketWhite)
            .border(1.dp, QuiketGray100, RoundedCornerShape(8.dp))
            .padding(bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(QuiketGray50),
                )
                Image(
                    painter = painterResource(Res.drawable.ic_upload_pdf),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = QuiketGray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.sizeBytes.toUploadSizeLabel(),
                        color = QuiketGray600,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = " · ",
                        color = QuiketGray400,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Image(
                        painter = painterResource(Res.drawable.ic_upload_ok),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "업로드 완료",
                        color = QuiketBrown950,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }

            Text(
                text = "삭제",
                color = if (enabled) QuiketGray600 else QuiketGray300,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.then(
                    if (enabled) Modifier.clickable(role = Role.Button, onClick = onRemoveClick) else Modifier,
                ),
            )
        }
    }
}

@Composable
internal fun LectureTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(QuiketGray50, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Transparent, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = QuiketGray950,
                fontWeight = FontWeight.Medium,
            ),
            cursorBrush = SolidColor(QuiketGray950),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (value.isBlank()) {
                        Text(
                            text = "업로드하고자 하는 강의 내용을 텍스트로 작성해 주세요.",
                            color = QuiketGray400,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
internal fun UploadFeedbackCard(
    message: String,
    progressPercent: Int,
    isError: Boolean,
    isUploading: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketWhite,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = message,
                color = if (isError) QuiketNegative else QuiketGray950,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            )
            if (isUploading || progressPercent > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(QuiketGray100),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((progressPercent.coerceIn(0, 100) / 100f).coerceAtLeast(0.08f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(QuiketBrown950),
                    )
                }
                Text(
                    text = "$progressPercent%",
                    color = QuiketGray600,
                    textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun UploadTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(QuiketBrown50),
    ) {
        UploadBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 50.dp),
        )
        Text(
            text = title,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp),
        )
    }
}

@Composable
internal fun UploadBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .semantics { contentDescription = "뒤로" }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = QuiketGray950,
                start = Offset(size.width * 0.62f, size.height * 0.18f),
                end = Offset(size.width * 0.32f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray950,
                start = Offset(size.width * 0.32f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

internal enum class UploadTab(
    val label: String,
) {
    File("파일"),
    Image("이미지"),
    Text("텍스트"),
}

internal enum class PartClassifyMethod {
    Ai,
    Manual,
}

internal fun PickedUploadFile.imageTypeLabel(): String =
    name.substringAfterLast('.', missingDelimiterValue = "")
        .takeIf { it.isNotBlank() }
        ?.uppercase()
        ?: mimeType.substringAfter('/', missingDelimiterValue = "IMG").uppercase()

internal fun <T> List<T>.swapItems(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().also { items ->
        val temp = items[from]
        items[from] = items[to]
        items[to] = temp
    }
}

internal fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp,
    dashLength: Dp,
    gapLength: Dp,
): Modifier = drawWithContent {
    drawContent()
    val strokePx = strokeWidth.toPx()
    val inset = strokePx / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = strokePx,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            ),
        ),
    )
}

internal const val ImageGridColumnCount = 3
internal const val MaxManualPartCount = 10
internal val PreviewScrim = Color(0xF2000000)
