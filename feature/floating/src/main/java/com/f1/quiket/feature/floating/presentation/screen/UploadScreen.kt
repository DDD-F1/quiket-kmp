package com.f1.quiket.feature.floating.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.presentation.component.upload.LectureUploadSection
import com.f1.quiket.feature.floating.presentation.component.upload.PartClassifyMethod
import com.f1.quiket.feature.floating.presentation.component.upload.UploadFile
import com.f1.quiket.feature.floating.presentation.component.upload.UploadImage
import com.f1.quiket.feature.floating.presentation.component.upload.UploadNextButton
import com.f1.quiket.feature.floating.presentation.component.upload.UploadTab
import com.f1.quiket.feature.floating.presentation.component.upload.UploadTopBar
import com.f1.quiket.feature.floating.presentation.viewmodel.UploadViewModel

@Composable
fun UploadScreen(
    subjectId: String? = null,
    chapterTitle: String? = null,
    lecturePurpose: String? = null,
    chapterCount: Int? = null,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onUploadSuccess: (lectureUploadId: String, chapterNumber: Int) -> Unit = { _, _ -> },
    viewModel: UploadViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(UploadTab.FILE) }
    var isContentReady by remember { mutableStateOf(false) }
    var currentFiles by remember { mutableStateOf<List<UploadFile>>(emptyList()) }
    var currentImages by remember { mutableStateOf<List<UploadImage>>(emptyList()) }
    var currentText by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()
    val isFailed by viewModel.isFailed.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val uploadedLectureUploadId by viewModel.uploadedLectureUploadId.collectAsStateWithLifecycle()

    // Cancel upload when back is pressed during loading — preserve file state by staying in UploadScreen
    BackHandler(enabled = isLoading || isFailed) {
        viewModel.cancelUpload()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            val luid = uploadedLectureUploadId
            viewModel.resetSuccess()
            if (subjectId != null && luid != null) {
                onUploadSuccess(luid, (chapterCount ?: 0) + 1)
            } else {
                onNextClick()
            }
        }
    }

    // Show loading/error screen while API is processing or failed
    if (isLoading || isFailed) {
        UploadLoadingScreen(
            progress = progress,
            isFailed = isFailed,
            onBack = {
                // cancelUpload resets isLoading/isFailed → UploadScreenContent re-renders with preserved files
                viewModel.cancelUpload()
            },
            onRetry = {
                if (subjectId != null) {
                    viewModel.submit(
                        subjectId = subjectId,
                        tab = selectedTab,
                        classifyMethod = PartClassifyMethod.AI,
                        manualSections = emptyList(),
                        files = currentFiles,
                        images = currentImages,
                        text = currentText,
                    )
                }
            },
        )
        return
    }

    val isNextEnabled = isContentReady
    UploadScreenContent(
        selectedTab = selectedTab,
        onTabSelect = { selectedTab = it },
        onReadyChange = { isContentReady = it },
        onFilesChange = { currentFiles = it },
        onImagesChange = { currentImages = it },
        onTextChange = { currentText = it },
        isNextEnabled = isNextEnabled,
        onBackClick = onBackClick,
        onNextClick = {
            if (subjectId != null) {
                viewModel.submit(
                    subjectId = subjectId,
                    tab = selectedTab,
                    classifyMethod = PartClassifyMethod.AI,
                    manualSections = emptyList(),
                    files = currentFiles,
                    images = currentImages,
                    text = currentText,
                )
            } else {
                onNextClick()
            }
        },
    )
}

@Composable
private fun UploadScreenContent(
    selectedTab: UploadTab,
    onTabSelect: (UploadTab) -> Unit,
    onReadyChange: (Boolean) -> Unit,
    onFilesChange: (List<UploadFile>) -> Unit,
    onImagesChange: (List<UploadImage>) -> Unit,
    onTextChange: (String) -> Unit,
    isNextEnabled: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brown50)
    ) {
        // ── TopBar (고정) ─────────────────────────────────────────────
        Surface(color = Brown50) {
            UploadTopBar(onBackClick = onBackClick)
        }

        // ── 탭 + 업로드 영역 (남은 화면 꽉 채움) ─────────────────────
        LectureUploadSection(
            selectedTab = selectedTab,
            onTabSelect = onTabSelect,
            onReadyChange = onReadyChange,
            onFilesChange = onFilesChange,
            onImagesChange = onImagesChange,
            onTextChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .background(Brown50),
        )

        // ── 다음 버튼 (고정) ─────────────────────────────────────────
        Surface(color = White) {
            UploadNextButton(
                enabled = isNextEnabled,
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "자료추가 — 파일 탭")
@Composable
private fun UploadScreenFileTabPreview() {
    QuiketTheme {
        UploadScreenContent(
            selectedTab = UploadTab.FILE,
            onTabSelect = {},
            onReadyChange = {},
            onFilesChange = {},
            onImagesChange = {},
            onTextChange = {},
            isNextEnabled = false,
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "자료추가 — 다음 버튼 활성화")
@Composable
private fun UploadScreenNextEnabledPreview() {
    QuiketTheme {
        UploadScreenContent(
            selectedTab = UploadTab.FILE,
            onTabSelect = {},
            onReadyChange = {},
            onFilesChange = {},
            onImagesChange = {},
            onTextChange = {},
            isNextEnabled = true,
            onBackClick = {},
            onNextClick = {},
        )
    }
}