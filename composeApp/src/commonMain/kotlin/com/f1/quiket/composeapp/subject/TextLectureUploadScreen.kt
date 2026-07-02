package com.f1.quiket.composeapp.subject

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
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_acorn
import quiket.composeapp.generated.resources.ic_info
import quiket.composeapp.generated.resources.ic_qring_profile
import quiket.composeapp.generated.resources.ic_upload_image
import quiket.composeapp.generated.resources.ic_upload_ok
import quiket.composeapp.generated.resources.ic_upload_pdf

private const val UploadProcessingLottieResource = "files/upload_processing.json"

@Composable
internal fun TextLectureUploadRoute(
    subject: SubjectDetail,
    targetChapter: ChapterWithParts? = null,
    onBackClick: () -> Unit,
    onUploadCompleted: (lectureUploadId: String) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetChapterId = targetChapter?.id
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<TextLectureUploadStateHolder>()

    LaunchedEffect(subject.id, targetChapterId) {
        stateHolder.bindSubject(subject = subject, targetChapter = targetChapter)
    }

    val filePicker = rememberUploadFilePicker(
        onFilesPicked = stateHolder::handlePickedFiles,
        onError = stateHolder::showPickerError,
    )

    fun submit() {
        coroutineScope.launch {
            stateHolder.submit(
                onSessionExpired = onSessionExpired,
                onUploadCompleted = onUploadCompleted,
            )
        }
    }

    if (stateHolder.isUploading) {
        UploadProcessingScreen(
            progressPercent = stateHolder.progressPercent,
            modifier = modifier,
        )
        return
    }

    if (stateHolder.uploadFailed) {
        UploadFailedScreen(
            message = stateHolder.feedbackMessage ?: "자료 추가 중에 문제가 발생했어요.",
            onBackClick = stateHolder::resetUploadFailure,
            onRetryClick = {
                stateHolder.resetUploadFailure()
                submit()
            },
            modifier = modifier,
        )
        return
    }

    LectureUploadScreen(
        targetChapterName = targetChapter?.name,
        selectedTab = stateHolder.selectedTab,
        nameInput = stateHolder.nameInput,
        lectureText = stateHolder.lectureText,
        selectedFiles = stateHolder.selectedFiles,
        partClassifyMethod = stateHolder.partClassifyMethod,
        manualPartNames = stateHolder.manualPartNames,
        isUploading = stateHolder.isUploading,
        progressPercent = stateHolder.progressPercent,
        feedbackMessage = stateHolder.feedbackMessage,
        isError = stateHolder.isError,
        onTabClick = stateHolder::selectTab,
        onNameInputChange = stateHolder::updateNameInput,
        onLectureTextChange = stateHolder::updateLectureText,
        onPickFileClick = {
            when (stateHolder.selectedTab) {
                UploadTab.File -> filePicker.pickPdf()
                UploadTab.Image -> filePicker.pickImages(
                    MaxImageUploadCount - stateHolder.selectedImageFiles.size,
                )
                UploadTab.Text -> Unit
            }
        },
        onRemoveFileClick = stateHolder::removeFile,
        onMoveFile = stateHolder::moveFile,
        onPartClassifyMethodChange = stateHolder::selectPartClassifyMethod,
        onManualPartNamesChange = stateHolder::updateManualPartNames,
        onBackClick = onBackClick,
        onSubmitClick = ::submit,
        modifier = modifier,
    )
}

@Composable
private fun LectureUploadScreen(
    targetChapterName: String?,
    selectedTab: UploadTab,
    nameInput: String,
    lectureText: String,
    selectedFiles: List<PickedUploadFile>,
    partClassifyMethod: PartClassifyMethod,
    manualPartNames: List<String>,
    isUploading: Boolean,
    progressPercent: Int,
    feedbackMessage: String?,
    isError: Boolean,
    onTabClick: (UploadTab) -> Unit,
    onNameInputChange: (String) -> Unit,
    onLectureTextChange: (String) -> Unit,
    onPickFileClick: () -> Unit,
    onRemoveFileClick: (Int) -> Unit,
    onMoveFile: (from: Int, to: Int) -> Unit,
    onPartClassifyMethodChange: (PartClassifyMethod) -> Unit,
    onManualPartNamesChange: (List<String>) -> Unit,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var previewImageIndex by remember(selectedTab, selectedFiles) { mutableStateOf<Int?>(null) }
    val isSubmitEnabled = when (selectedTab) {
        UploadTab.Text -> lectureText.isNotBlank()
        UploadTab.File,
        UploadTab.Image,
        -> selectedFiles.isNotEmpty()
    } && targetChapterName?.let { nameInput.isNotBlank() } != false && !isUploading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketBrown50),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(QuiketBrown50),
        ) {
            UploadTopBar(
                title = "자료 추가",
                onBackClick = onBackClick,
            )

            LectureUploadContent(
                targetChapterName = targetChapterName,
                selectedTab = selectedTab,
                nameInput = nameInput,
                lectureText = lectureText,
                selectedFiles = selectedFiles,
                partClassifyMethod = partClassifyMethod,
                manualPartNames = manualPartNames,
                feedbackMessage = feedbackMessage,
                progressPercent = progressPercent,
                isError = isError,
                isUploading = isUploading,
                onTabClick = onTabClick,
                onNameInputChange = onNameInputChange,
                onLectureTextChange = onLectureTextChange,
                onPickFileClick = onPickFileClick,
                onRemoveFileClick = onRemoveFileClick,
                onMoveFile = onMoveFile,
                onPreviewFileClick = { previewImageIndex = it },
                onPartClassifyMethodChange = onPartClassifyMethodChange,
                onManualPartNamesChange = onManualPartNamesChange,
                modifier = Modifier.weight(1f),
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = QuiketWhite,
            ) {
                UploadNextButton(
                    text = "다음",
                    enabled = isSubmitEnabled,
                    onClick = {
                        focusManager.clearFocus()
                        onSubmitClick()
                    },
                    modifier = Modifier
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        }

        previewImageIndex?.let { initialIndex ->
            if (selectedTab == UploadTab.Image && selectedFiles.isNotEmpty()) {
                ImagePreviewDialog(
                    files = selectedFiles,
                    initialIndex = initialIndex,
                    canRemove = !isUploading,
                    onDismiss = { previewImageIndex = null },
                    onRemoveFileClick = { index ->
                        onRemoveFileClick(index)
                        previewImageIndex = if (selectedFiles.size <= 1) {
                            null
                        } else {
                            index.coerceAtMost(selectedFiles.size - 2)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun LectureUploadContent(
    targetChapterName: String?,
    selectedTab: UploadTab,
    nameInput: String,
    lectureText: String,
    selectedFiles: List<PickedUploadFile>,
    partClassifyMethod: PartClassifyMethod,
    manualPartNames: List<String>,
    feedbackMessage: String?,
    progressPercent: Int,
    isError: Boolean,
    isUploading: Boolean,
    onTabClick: (UploadTab) -> Unit,
    onNameInputChange: (String) -> Unit,
    onLectureTextChange: (String) -> Unit,
    onPickFileClick: () -> Unit,
    onRemoveFileClick: (Int) -> Unit,
    onMoveFile: (from: Int, to: Int) -> Unit,
    onPreviewFileClick: (Int) -> Unit,
    onPartClassifyMethodChange: (PartClassifyMethod) -> Unit,
    onManualPartNamesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(QuiketBrown50),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        UploadTabRow(
            selectedTab = selectedTab,
            enabled = !isUploading,
            onTabClick = onTabClick,
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = QuiketWhite,
            shape = RoundedCornerShape(topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (targetChapterName != null) {
                    TargetChapterPartNameSection(
                        targetChapterName = targetChapterName,
                        nameInput = nameInput,
                        onNameInputChange = onNameInputChange,
                    )
                }

                when (selectedTab) {
                    UploadTab.Text -> TextUploadSection(
                        lectureText = lectureText,
                        enabled = !isUploading,
                        onLectureTextChange = onLectureTextChange,
                    )
                    UploadTab.File -> BinaryUploadSection(
                        selectedFiles = selectedFiles,
                        enabled = !isUploading,
                        onPickFileClick = onPickFileClick,
                        onRemoveFileClick = onRemoveFileClick,
                    )
                    UploadTab.Image -> ImageUploadSection(
                        selectedFiles = selectedFiles,
                        enabled = !isUploading && selectedFiles.size < MaxImageUploadCount,
                        canRemove = !isUploading,
                        onPickFileClick = onPickFileClick,
                        onRemoveFileClick = onRemoveFileClick,
                        onMoveFile = onMoveFile,
                        onPreviewFileClick = onPreviewFileClick,
                    )
                }

                if (targetChapterName == null && partClassifyMethod == PartClassifyMethod.Manual) {
                    PartClassifySection(
                        selectedMethod = partClassifyMethod,
                        manualPartNames = manualPartNames,
                        enabled = !isUploading,
                        onMethodChange = onPartClassifyMethodChange,
                        onManualPartNamesChange = onManualPartNamesChange,
                    )
                }

                if (!feedbackMessage.isNullOrBlank()) {
                    UploadFeedbackCard(
                        message = feedbackMessage,
                        progressPercent = progressPercent,
                        isError = isError,
                        isUploading = isUploading,
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetChapterPartNameSection(
    targetChapterName: String,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "추가할 챕터",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = targetChapterName,
            color = QuiketGray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        )
        QuiketTextField(
            value = nameInput,
            onValueChange = onNameInputChange,
            hint = "파트명을 입력해주세요",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UploadNextButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) QuiketBrown950 else QuiketGray100)
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) QuiketWhite else QuiketGray400,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun UploadProcessingScreen(
    progressPercent: Int,
    modifier: Modifier = Modifier,
) {
    val clampedProgress = progressPercent.coerceIn(0, 100)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center,
            ) {
                UploadProcessingAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "큐링이가 강의를 가져와 파트를\n열심히 나누고 있어요!",
                    color = QuiketGray950,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "화면을 나가면 업로드가 취소돼요\n보통 10~30초 정도 걸려요",
                    color = QuiketGray700,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearProgressIndicator(
                    progress = { clampedProgress / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = QuiketBrown950,
                    trackColor = QuiketGray100,
                )
                Text(
                    text = "$clampedProgress%",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun UploadProcessingAnimation(
    modifier: Modifier = Modifier,
) {
    var lottieJson by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        lottieJson = Res.readBytes(UploadProcessingLottieResource).decodeToString()
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(lottieJson.orEmpty()),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
    )

    if (composition != null && lottieJson != null) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = progress,
            ),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        UploadProcessingFallback(modifier = modifier)
    }
}

@Composable
private fun UploadProcessingFallback(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_qring_profile),
            contentDescription = null,
            modifier = Modifier.size(220.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(68.dp)
                .clip(CircleShape)
                .background(QuiketBrown50),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_acorn),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun UploadFailedScreen(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(QuiketGray50),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_qring_profile),
                    contentDescription = null,
                    modifier = Modifier.size(170.dp),
                )
                Text(
                    text = "!",
                    color = QuiketNegative,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp, end = 34.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "앗 ! 자료 추가 중에\n문제가 발생했어요.",
                    color = QuiketGray950,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = message,
                    color = QuiketGray700,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onBackClick),
                color = QuiketWhite,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, QuiketBrown950),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "이전",
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
            QuiketPrimaryButton(
                text = "다시 시도하기",
                onClick = onRetryClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun UploadTabRow(
    selectedTab: UploadTab,
    enabled: Boolean,
    onTabClick: (UploadTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        UploadTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Surface(
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .then(
                        if (enabled) {
                            Modifier.clickable(role = Role.Button) { onTabClick(tab) }
                        } else {
                            Modifier
                        },
                    ),
                color = if (selected) QuiketWhite else QuiketGray100,
                contentColor = if (selected) QuiketGray950 else QuiketGray800,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = tab.label,
                        color = if (selected) QuiketGray950 else QuiketGray800,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PartClassifySection(
    selectedMethod: PartClassifyMethod,
    manualPartNames: List<String>,
    enabled: Boolean,
    onMethodChange: (PartClassifyMethod) -> Unit,
    onManualPartNamesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showManualDialog by remember { mutableStateOf(false) }

    if (showManualDialog) {
        ManualPartDialog(
            initialPartNames = manualPartNames,
            onDismiss = { showManualDialog = false },
            onApply = { names ->
                onManualPartNamesChange(names)
                showManualDialog = false
            },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "파트 분류 방법",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Image(
                painter = painterResource(Res.drawable.ic_info),
                contentDescription = "파트 분류 방법 안내",
                modifier = Modifier.size(16.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PartClassifyCard(
                title = "AI에게 맡기기",
                description = "자료를 자동으로 나눠요",
                selected = selectedMethod == PartClassifyMethod.Ai,
                enabled = enabled,
                onClick = { onMethodChange(PartClassifyMethod.Ai) },
                modifier = Modifier.weight(1f),
            )
            PartClassifyCard(
                title = "직접 분류하기",
                description = if (manualPartNames.isEmpty()) {
                    "파트명을 직접 정해요"
                } else {
                    "${manualPartNames.size}개 파트 입력됨"
                },
                selected = selectedMethod == PartClassifyMethod.Manual,
                enabled = enabled,
                onClick = { showManualDialog = true },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PartClassifyCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        color = if (selected) QuiketBrown50 else QuiketGray50,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) QuiketBrown950 else QuiketGray100,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                color = if (enabled) QuiketGray950 else QuiketGray400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = if (enabled) QuiketGray600 else QuiketGray400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun ManualPartDialog(
    initialPartNames: List<String>,
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit,
) {
    var partNames by remember(initialPartNames) {
        mutableStateOf(initialPartNames.ifEmpty { listOf("") })
    }
    val trimmedPartNames = partNames.map(String::trim).filter(String::isNotBlank)
    val canAdd = partNames.size < MaxManualPartCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clickable(onClick = {}),
                color = QuiketWhite,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "파트명 직접 입력",
                                color = QuiketGray950,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "분류하고 싶은 파트명을 순서대로 입력해주세요",
                                color = QuiketGray700,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            text = "닫기",
                            color = QuiketGray600,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(role = Role.Button, onClick = onDismiss)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 336.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        partNames.forEachIndexed { index, partName ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(QuiketGray100),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = QuiketGray700,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                                QuiketTextField(
                                    value = partName,
                                    onValueChange = { value ->
                                        partNames = partNames.toMutableList().also { it[index] = value }
                                    },
                                    hint = "파트명을 입력해주세요",
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        keyboardType = KeyboardType.Text,
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "삭제",
                                    color = if (partNames.size > 1) QuiketNegative else QuiketGray400,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (partNames.size > 1) {
                                                Modifier.clickable(role = Role.Button) {
                                                    partNames = partNames.toMutableList().also { it.removeAt(index) }
                                                }
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (canAdd) {
                                    Modifier.clickable(role = Role.Button) { partNames = partNames + "" }
                                } else {
                                    Modifier
                                },
                            ),
                        color = QuiketGray50,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, QuiketGray300),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+ 파트 추가하기 (${partNames.size}/$MaxManualPartCount)",
                                color = if (canAdd) QuiketGray700 else QuiketGray400,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button, onClick = onDismiss),
                            color = QuiketWhite,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, QuiketBrown950),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "취소",
                                    color = QuiketGray950,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        QuiketPrimaryButton(
                            text = "적용",
                            enabled = trimmedPartNames.isNotEmpty(),
                            onClick = { onApply(trimmedPartNames) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextUploadSection(
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
private fun BinaryUploadSection(
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
private fun ImageUploadSection(
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
private fun DraggableImageGrid(
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
private fun ImageUploadCard(
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
private fun DecodedUploadImage(
    file: PickedUploadFile,
    fallbackIndex: Int,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(file) {
        runCatching { file.bytes.decodeToImageBitmap() }.getOrNull()
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
private fun ImagePreviewDialog(
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
private fun PreviewTextButton(
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
private fun UploadDropZone(
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
private fun UploadFileCard(
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
private fun LectureTextInput(
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
private fun UploadFeedbackCard(
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
private fun UploadTopBar(
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
private fun UploadBackButton(
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

private fun PickedUploadFile.imageTypeLabel(): String =
    name.substringAfterLast('.', missingDelimiterValue = "")
        .takeIf { it.isNotBlank() }
        ?.uppercase()
        ?: mimeType.substringAfter('/', missingDelimiterValue = "IMG").uppercase()

private fun <T> List<T>.swapItems(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().also { items ->
        val temp = items[from]
        items[from] = items[to]
        items[to] = temp
    }
}

private fun Modifier.dashedBorder(
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

private const val ImageGridColumnCount = 3
private const val MaxManualPartCount = 10
private val PreviewScrim = Color(0xF2000000)
