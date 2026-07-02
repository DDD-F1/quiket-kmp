package com.f1.quiket.feature.floating.presentation.component.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.presentation.screen.ImagePreviewScreen

private const val MAX_TEXT_LENGTH = 30_000

enum class UploadTab { FILE, IMAGE, TEXT }

@Composable
fun LectureUploadSection(
    selectedTab: UploadTab,
    onTabSelect: (UploadTab) -> Unit,
    onReadyChange: (Boolean) -> Unit = {},
    onFilesChange: (List<UploadFile>) -> Unit = {},
    onImagesChange: (List<UploadImage>) -> Unit = {},
    onTextChange: (String) -> Unit = {},
    initialFiles: List<UploadFile> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val files = remember { mutableStateListOf<UploadFile>().also { it.addAll(initialFiles) } }
    val images = remember { mutableStateListOf<UploadImage>() }
    var lectureText by remember { mutableStateOf("") }
    var previewIndex by remember { mutableStateOf<Int?>(null) }

    // 탭별 readiness 계산
    val isReady = when (selectedTab) {
        UploadTab.FILE -> files.isNotEmpty() && files.all { it.status == UploadFileStatus.COMPLETED }
        UploadTab.IMAGE -> images.isNotEmpty()
        UploadTab.TEXT -> lectureText.isNotBlank()
    }
    LaunchedEffect(isReady, selectedTab) {
        onReadyChange(isReady)
    }

    // content 변경 시 상위로 전달
    LaunchedEffect(files.toList()) { onFilesChange(files.toList()) }
    LaunchedEffect(images.toList()) { onImagesChange(images.toList()) }
    LaunchedEffect(lectureText) { onTextChange(lectureText) }

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        // ── 탭 Row (HomeScreen TabItem 스타일, 맨 왼쪽) ────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            UploadTab.entries.forEach { tab ->
                val label = when (tab) {
                    UploadTab.FILE -> "파일"
                    UploadTab.IMAGE -> "이미지"
                    UploadTab.TEXT -> "텍스트"
                }
                UploadTabChip(
                    label = label,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelect(tab) },
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // ── 탭 콘텐츠 (White Surface, topEnd 라운드) ─────────────────────
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = White,
            shape = RoundedCornerShape(topEnd = 24.dp),
        ) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                when (selectedTab) {
                    UploadTab.FILE -> FileUploadZone(
                        files = files,
                        modifier = Modifier.matchParentSize(),
                    )

                    UploadTab.IMAGE -> ImageUploadZone(
                        images = images,
                        onRemove = { index -> if (index in images.indices) images.removeAt(index) },
                        onImageClick = { index -> previewIndex = index },
                        modifier = Modifier.matchParentSize(),
                    )

                    UploadTab.TEXT -> TextUploadZone(
                        value = lectureText,
                        onValueChange = { if (it.length <= MAX_TEXT_LENGTH) lectureText = it },
                        maxLength = MAX_TEXT_LENGTH,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }
    }

    // ── 이미지 미리보기 전체화면 Dialog ──────────────────────────────────
    previewIndex?.let { idx ->
        if (images.isNotEmpty()) {
            Dialog(
                onDismissRequest = { previewIndex = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            ) {
                ImagePreviewScreen(
                    images = images.toList(),
                    initialIndex = idx.coerceIn(0, images.size - 1),
                    onBack = { previewIndex = null },
                    onDelete = { deleteIdx ->
                        if (deleteIdx in images.indices) {
                            images.removeAt(deleteIdx)
                            previewIndex = if (images.isEmpty()) {
                                null
                            } else {
                                deleteIdx.coerceIn(0, images.size - 1)
                            }
                        }
                    },
                    onSaveCrop = { index, scale, offsetX, offsetY ->
                        if (index in images.indices) {
                            images[index] = images[index].copy(
                                cropScale = scale,
                                cropOffsetX = offsetX,
                                cropOffsetY = offsetY,
                            )
                        }
                    },
                )
            }
        }
    }
}

// ─── 탭 칩 (HomeScreen TabItem 동일) ─────────────────────────────────────────

@Composable
private fun UploadTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(80.dp)
            .height(48.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = if (isSelected) White else Gray100,
        contentColor = if (isSelected) Gray950 else Gray800,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
            )
        }
    }
}

// ─── 텍스트 입력 ──────────────────────────────────────────────────────────────

@Composable
private fun TextUploadZone(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        Text(
            text = "강의 텍스트",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Gray50, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(bottom = 28.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray950),
                cursorBrush = SolidColor(Gray950),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "업로드하고자 하는 강의 내용을 텍스트로 작성해 주세요.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Gray400)
                        )
                    }
                    innerTextField()
                }
            )
        }
        Text(
            text = "%,d/%,d".format(value.length, maxLength),
            style = MaterialTheme.typography.labelSmall.copy(color = Gray400),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 6.dp)
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "업로드 — 파일 탭 (빈 상태)")
@Composable
private fun LectureUploadSectionFilePreview() {
    QuiketTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brown50),
        ) {
            LectureUploadSection(
                selectedTab = UploadTab.FILE,
                onTabSelect = {},
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "업로드 — 파일 탭 (파일 상태)")
@Composable
private fun LectureUploadSectionFileStatesPreview() {
    val sampleFiles = listOf(
        UploadFile(
            id = 1,
            uri = android.net.Uri.EMPTY,
            name = "SQLD_정리노트.pdf",
            sizeLabel = "2.4MB",
            status = UploadFileStatus.COMPLETED
        ),
        UploadFile(
            id = 2,
            uri = android.net.Uri.EMPTY,
            name = "데이터모델링_강의자료.pdf",
            sizeLabel = "1.1MB",
            status = UploadFileStatus.FAILED
        ),
        UploadFile(
            id = 3,
            uri = android.net.Uri.EMPTY,
            name = "SQL활용_실습.pdf",
            sizeLabel = "890KB",
            status = UploadFileStatus.UPLOADING
        ),
    )
    QuiketTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brown50),
        ) {
            LectureUploadSection(
                selectedTab = UploadTab.FILE,
                onTabSelect = {},
                initialFiles = sampleFiles,
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "업로드 — 이미지 탭")
@Composable
private fun LectureUploadSectionImagePreview() {
    QuiketTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brown50),
        ) {
            LectureUploadSection(
                selectedTab = UploadTab.IMAGE,
                onTabSelect = {},
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "업로드 — 텍스트 탭")
@Composable
private fun LectureUploadSectionTextPreview() {
    QuiketTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brown50),
        ) {
            LectureUploadSection(
                selectedTab = UploadTab.TEXT,
                onTabSelect = {},
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}