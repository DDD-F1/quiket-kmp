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
import androidx.compose.runtime.DisposableEffect
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

internal const val UploadProcessingLottieResource = "files/upload_processing.json"

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

    DisposableEffect(stateHolder) {
        onDispose(stateHolder::clearPickedFiles)
    }

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
