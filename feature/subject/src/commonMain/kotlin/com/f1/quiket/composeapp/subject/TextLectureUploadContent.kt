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
internal fun LectureUploadScreen(
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
internal fun LectureUploadContent(
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
internal fun TargetChapterPartNameSection(
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
internal fun UploadNextButton(
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
internal fun UploadProcessingScreen(
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
internal fun UploadProcessingAnimation(
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
internal fun UploadProcessingFallback(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(DesignSystemRes.drawable.ic_qring_profile),
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
                painter = painterResource(DesignSystemRes.drawable.ic_acorn),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
@Composable
internal fun UploadFailedScreen(
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
                    painter = painterResource(DesignSystemRes.drawable.ic_qring_profile),
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
