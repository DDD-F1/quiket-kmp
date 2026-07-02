package com.f1.quiket.composeapp.subject.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.subject.domain.model.ChapterWithParts
import com.f1.quiket.composeapp.subject.domain.model.LectureFileUploadType
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadProgress
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadStatus
import com.f1.quiket.composeapp.subject.MaxImageUploadBytes
import com.f1.quiket.composeapp.subject.MaxImageUploadCount
import com.f1.quiket.composeapp.subject.MaxPdfUploadBytes
import com.f1.quiket.composeapp.subject.MaxTextUploadLength
import com.f1.quiket.composeapp.subject.PartClassifyMethod
import com.f1.quiket.composeapp.subject.domain.model.PartSplitMethod
import com.f1.quiket.composeapp.subject.domain.model.PartSplitPlan
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail
import com.f1.quiket.composeapp.subject.domain.model.SubjectException
import com.f1.quiket.composeapp.subject.UploadTab
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import com.f1.quiket.composeapp.subject.toSubjectNetworkAwareMessage
import com.f1.quiket.composeapp.subject.withSubjectNetworkRetryGuide
import kotlinx.coroutines.delay
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class TextLectureUploadStateHolder(
    private val subjectUseCases: SubjectUseCases,
) {
    private var subject: SubjectDetail? = null
    private var targetChapter: ChapterWithParts? = null

    var selectedTab by mutableStateOf(UploadTab.File)
        private set

    var nameInput by mutableStateOf("")
        private set

    var lectureText by mutableStateOf("")
        private set

    var selectedPdfFiles by mutableStateOf<List<PickedUploadFile>>(emptyList())
        private set

    var selectedImageFiles by mutableStateOf<List<PickedUploadFile>>(emptyList())
        private set

    var partClassifyMethod by mutableStateOf(PartClassifyMethod.Ai)
        private set

    var manualPartNames by mutableStateOf<List<String>>(emptyList())
        private set

    var isUploading by mutableStateOf(false)
        private set

    var progressPercent by mutableStateOf(0)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    var isError by mutableStateOf(false)
        private set

    var uploadFailed by mutableStateOf(false)
        private set

    val selectedFiles: List<PickedUploadFile>
        get() = when (selectedTab) {
            UploadTab.File -> selectedPdfFiles
            UploadTab.Image -> selectedImageFiles
            UploadTab.Text -> emptyList()
        }

    fun bindSubject(
        subject: SubjectDetail,
        targetChapter: ChapterWithParts?,
    ) {
        val subjectChanged = this.subject?.id != subject.id
        val targetChanged = this.targetChapter?.id != targetChapter?.id
        if (!subjectChanged && !targetChanged) return

        this.subject = subject
        this.targetChapter = targetChapter
        selectedTab = UploadTab.File
        nameInput = ""
        lectureText = ""
        selectedPdfFiles = emptyList()
        selectedImageFiles = emptyList()
        partClassifyMethod = PartClassifyMethod.Ai
        manualPartNames = emptyList()
        isUploading = false
        progressPercent = 0
        feedbackMessage = null
        isError = false
        uploadFailed = false
    }

    fun selectTab(tab: UploadTab) {
        selectedTab = tab
        clearFeedback()
    }

    fun updateNameInput(input: String) {
        nameInput = input
        feedbackMessage = null
    }

    fun updateLectureText(input: String) {
        lectureText = input.take(MaxTextUploadLength)
        feedbackMessage = null
    }

    fun handlePickedFiles(files: List<PickedUploadFile>) {
        isError = false
        feedbackMessage = null
        when (selectedTab) {
            UploadTab.File -> selectedPdfFiles = validatePickedFiles(UploadTab.File, files)
            UploadTab.Image -> selectedImageFiles = validatePickedFiles(UploadTab.Image, files)
            UploadTab.Text -> Unit
        }
    }

    fun showPickerError(message: String) {
        showError(message)
    }

    fun removeFile(index: Int) {
        when (selectedTab) {
            UploadTab.File -> {
                selectedPdfFiles = selectedPdfFiles.filterIndexed { fileIndex, _ -> fileIndex != index }
            }

            UploadTab.Image -> {
                selectedImageFiles = selectedImageFiles.filterIndexed { fileIndex, _ -> fileIndex != index }
            }

            UploadTab.Text -> Unit
        }
        feedbackMessage = null
    }

    fun moveFile(from: Int, to: Int) {
        if (selectedTab == UploadTab.Image) {
            selectedImageFiles = selectedImageFiles.swapItems(from, to)
        }
        feedbackMessage = null
    }

    fun selectPartClassifyMethod(method: PartClassifyMethod) {
        partClassifyMethod = method
        feedbackMessage = null
    }

    fun updateManualPartNames(partNames: List<String>) {
        manualPartNames = partNames
        partClassifyMethod = PartClassifyMethod.Manual
        feedbackMessage = null
    }

    fun resetUploadFailure() {
        uploadFailed = false
        isError = false
        feedbackMessage = null
        progressPercent = 0
    }

    suspend fun submit(
        onSessionExpired: () -> Unit,
        onUploadCompleted: (lectureUploadId: String) -> Unit,
    ) {
        if (isUploading) return

        val currentSubject = subject
        if (currentSubject == null) {
            showError("과목 정보를 불러오지 못했어요.")
            return
        }

        val currentTargetChapter = targetChapter
        val trimmedText = lectureText.trim()
        val uploadFiles = selectedFiles

        val requestError = when (selectedTab) {
            UploadTab.Text -> when {
                trimmedText.isBlank() -> "텍스트를 입력해주세요."
                trimmedText.length > MaxTextUploadLength -> "텍스트는 최대 ${MaxTextUploadLength}자까지 입력할 수 있어요."
                else -> null
            }

            UploadTab.File -> if (uploadFiles.isEmpty()) "업로드할 PDF를 선택해주세요." else null
            UploadTab.Image -> if (uploadFiles.isEmpty()) "업로드할 이미지를 선택해주세요." else null
        }
        if (requestError != null) {
            showError(requestError)
            return
        }
        if (currentTargetChapter != null && nameInput.isBlank()) {
            showError("파트명을 입력해주세요.")
            return
        }

        val splitPlans = manualPartNames
            .map(String::trim)
            .filter(String::isNotBlank)
            .mapIndexed { index, name ->
                PartSplitPlan(partNumber = index + 1, intendedName = name)
            }
        if (currentTargetChapter == null &&
            partClassifyMethod == PartClassifyMethod.Manual &&
            splitPlans.isEmpty()
        ) {
            showError("직접 분류할 파트명을 1개 이상 입력해주세요.")
            return
        }
        val splitMethod = when (partClassifyMethod) {
            PartClassifyMethod.Ai -> PartSplitMethod.Auto
            PartClassifyMethod.Manual -> PartSplitMethod.Manual
        }

        isUploading = true
        isError = false
        uploadFailed = false
        progressPercent = 0
        feedbackMessage = "자료를 분석하고 있어요"
        val uploadStartedAt = TimeSource.Monotonic.markNow()

        runCatching {
            if (currentTargetChapter != null) {
                when (selectedTab) {
                    UploadTab.Text -> subjectUseCases.addTextPartToChapter(
                        chapterId = currentTargetChapter.id,
                        partName = nameInput.trim(),
                        text = trimmedText,
                    )

                    UploadTab.File -> subjectUseCases.addFilePartToChapter(
                        chapterId = currentTargetChapter.id,
                        partName = nameInput.trim(),
                        uploadType = LectureFileUploadType.Pdf,
                        files = uploadFiles.take(1),
                    )

                    UploadTab.Image -> subjectUseCases.addFilePartToChapter(
                        chapterId = currentTargetChapter.id,
                        partName = nameInput.trim(),
                        uploadType = LectureFileUploadType.Image,
                        files = uploadFiles,
                    )
                }
            } else {
                when (selectedTab) {
                    UploadTab.Text -> subjectUseCases.createTextLectureUpload(
                        subjectId = currentSubject.id,
                        chapterName = nameInput.trim().ifBlank { null },
                        text = trimmedText,
                        partSplitMethod = splitMethod,
                        partSplitPlans = splitPlans,
                    )

                    UploadTab.File -> subjectUseCases.createFileLectureUpload(
                        subjectId = currentSubject.id,
                        chapterName = nameInput.trim().ifBlank { null },
                        uploadType = LectureFileUploadType.Pdf,
                        files = uploadFiles.take(1),
                        partSplitMethod = splitMethod,
                        partSplitPlans = splitPlans,
                    )

                    UploadTab.Image -> subjectUseCases.createFileLectureUpload(
                        subjectId = currentSubject.id,
                        chapterName = nameInput.trim().ifBlank { null },
                        uploadType = LectureFileUploadType.Image,
                        files = uploadFiles,
                        partSplitMethod = splitMethod,
                        partSplitPlans = splitPlans,
                    )
                }
            }
        }.onSuccess { accepted ->
            progressPercent = if (accepted.status == LectureUploadStatus.Completed) 100 else 10
            if (accepted.status == LectureUploadStatus.Completed) {
                keepUploadLoadingVisible(uploadStartedAt)
                isUploading = false
                uploadFailed = false
                feedbackMessage = "자료 업로드가 완료됐어요"
                onUploadCompleted(accepted.lectureUploadId)
            } else {
                pollUploadStatus(
                    lectureUploadId = accepted.lectureUploadId,
                    onSessionExpired = onSessionExpired,
                    onCompleted = { progress ->
                        keepUploadLoadingVisible(uploadStartedAt)
                        isUploading = false
                        isError = false
                        uploadFailed = false
                        progressPercent = 100
                        feedbackMessage = "자료 업로드가 완료됐어요"
                        onUploadCompleted(progress.lectureUploadId)
                    },
                    onFailed = { message ->
                        isUploading = false
                        isError = true
                        uploadFailed = true
                        feedbackMessage = message
                    },
                )
            }
        }.onFailure { error ->
            if (error is SubjectException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                isUploading = false
                isError = true
                uploadFailed = true
                feedbackMessage = error.toSubjectNetworkAwareMessage(
                    fallback = "자료 업로드를 시작하지 못했습니다.",
                    timeoutMessage = "자료 업로드를 시작하지 못했습니다.".withSubjectNetworkRetryGuide(),
                )
            }
        }
    }

    private fun validatePickedFiles(
        tab: UploadTab,
        files: List<PickedUploadFile>,
    ): List<PickedUploadFile> =
        when (tab) {
            UploadTab.File -> {
                val file = files.firstOrNull()
                if (file == null) {
                    emptyList()
                } else if (file.sizeBytes > MaxPdfUploadBytes) {
                        showError("PDF는 최대 50MB까지 업로드할 수 있어요.")
                        emptyList()
                } else {
                    listOf(file)
                }
            }

            UploadTab.Image -> {
                val remaining = MaxImageUploadCount - selectedImageFiles.size
                val accepted = files
                    .filter { file ->
                        val valid = file.sizeBytes <= MaxImageUploadBytes
                        if (!valid) showError("이미지는 장당 최대 15MB까지 업로드할 수 있어요.")
                        valid
                    }
                    .take(remaining.coerceAtLeast(0))
                if (files.size > accepted.size && selectedImageFiles.size + accepted.size >= MaxImageUploadCount) {
                    showError("이미지는 최대 ${MaxImageUploadCount}장까지 업로드할 수 있어요.")
                }
                selectedImageFiles + accepted
            }

            UploadTab.Text -> emptyList()
        }

    private suspend fun pollUploadStatus(
        lectureUploadId: String,
        onSessionExpired: () -> Unit,
        onCompleted: suspend (LectureUploadProgress) -> Unit,
        onFailed: (String) -> Unit,
    ) {
        var consecutiveFailureCount = 0

        repeat(MaxUploadPollingAttempts) {
            delay(UploadPollingIntervalMillis)
            runCatching {
                subjectUseCases.getLectureUploadStatus(lectureUploadId = lectureUploadId)
            }.onSuccess { progress ->
                consecutiveFailureCount = 0
                progressPercent = progress.progressPct ?: progressPercent
                feedbackMessage = when (progress.status) {
                    LectureUploadStatus.Failed ->
                        progress.failMessage ?: progress.failReason ?: "자료 업로드에 실패했어요"

                    else -> "자료를 분석하고 있어요"
                }
                when (progress.status) {
                    LectureUploadStatus.Completed -> {
                        onCompleted(progress)
                        return
                    }

                    LectureUploadStatus.Failed -> {
                        onFailed(progress.failMessage ?: progress.failReason ?: "자료 업로드에 실패했어요")
                        return
                    }

                    else -> Unit
                }
            }.onFailure { error ->
                if (error is SubjectException && error.isUnauthorized) {
                    onSessionExpired()
                    return
                }
                consecutiveFailureCount += 1
                if (consecutiveFailureCount >= MaxUploadPollingFailures) {
                    onFailed("업로드 상태를 확인하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.")
                    return
                }
            }
        }

        onFailed("자료 분석 시간이 예상보다 길어지고 있어요. 잠시 후 과목 화면에서 다시 확인해주세요.")
    }

    private fun showError(message: String) {
        isError = true
        uploadFailed = false
        feedbackMessage = message
    }

    private fun clearFeedback() {
        feedbackMessage = null
        isError = false
    }
}

private suspend fun keepUploadLoadingVisible(startedAt: TimeMark) {
    val remainingMillis = MinimumUploadLoadingVisibleMillis - startedAt.elapsedNow().inWholeMilliseconds
    if (remainingMillis > 0) delay(remainingMillis)
}

private fun <T> List<T>.swapItems(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().also { items ->
        val temp = items[from]
        items[from] = items[to]
        items[to] = temp
    }
}

private const val UploadPollingIntervalMillis = 2_000L
private const val MaxUploadPollingAttempts = 60
private const val MaxUploadPollingFailures = 3
private const val MinimumUploadLoadingVisibleMillis = 1_800L
