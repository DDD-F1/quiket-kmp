package com.f1.quiket.feature.floating.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.model.LectureFileUpload
import com.f1.quiket.feature.floating.domain.model.LectureFileUploadType
import com.f1.quiket.feature.floating.domain.model.LectureTextUpload
import com.f1.quiket.feature.floating.domain.model.LectureUploadStatus
import com.f1.quiket.feature.floating.domain.model.PartSplitMethod
import com.f1.quiket.feature.floating.domain.model.PartSplitPlan
import com.f1.quiket.feature.floating.domain.repository.LectureUploadRepository
import com.f1.quiket.feature.floating.presentation.component.upload.PartClassifyMethod
import com.f1.quiket.feature.floating.presentation.component.upload.UploadFile
import com.f1.quiket.feature.floating.presentation.component.upload.UploadImage
import com.f1.quiket.feature.floating.presentation.component.upload.UploadTab
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val lectureUploadRepository: LectureUploadRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _uploadedChapterId = MutableStateFlow<String?>(null)
    val uploadedChapterId: StateFlow<String?> = _uploadedChapterId.asStateFlow()

    private val _uploadedLectureUploadId = MutableStateFlow<String?>(null)
    val uploadedLectureUploadId: StateFlow<String?> = _uploadedLectureUploadId.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isFailed = MutableStateFlow(false)
    val isFailed: StateFlow<Boolean> = _isFailed.asStateFlow()

    private var pollingJob: Job? = null

    fun submit(
        subjectId: String,
        tab: UploadTab,
        classifyMethod: PartClassifyMethod,
        manualSections: List<String>,
        files: List<UploadFile>,
        images: List<UploadImage>,
        text: String,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = false
            _isFailed.value = false
            _progress.value = 0f
            _errorMessage.value = null

            val splitMethod = when (classifyMethod) {
                PartClassifyMethod.AI -> PartSplitMethod.Auto
                PartClassifyMethod.MANUAL -> PartSplitMethod.Manual
            }
            val splitPlans = manualSections.mapIndexed { index, name ->
                PartSplitPlan(partNumber = index + 1, intendedName = name)
            }

            val result = when (tab) {
                UploadTab.TEXT -> {
                    lectureUploadRepository.createTextUpload(
                        LectureTextUpload(
                            subjectId = subjectId,
                            chapterName = null,
                            partSplitMethod = splitMethod,
                            text = text,
                            partSplitPlans = splitPlans,
                        )
                    )
                }

                UploadTab.FILE -> {
                    val parts = files
                        .filter { it.uri != Uri.EMPTY }
                        .mapNotNull { file -> uriToMultipart(file.uri, file.name) }
                    lectureUploadRepository.createFileUpload(
                        request = LectureFileUpload(
                            subjectId = subjectId,
                            chapterName = null,
                            uploadType = LectureFileUploadType.Pdf,
                            partSplitMethod = splitMethod,
                            partSplitPlans = splitPlans,
                        ),
                        files = parts,
                    )
                }

                UploadTab.IMAGE -> {
                    val parts = images
                        .mapNotNull { image -> uriToMultipart(image.uri, "image_${image.id}.jpg") }
                    lectureUploadRepository.createFileUpload(
                        request = LectureFileUpload(
                            subjectId = subjectId,
                            chapterName = null,
                            uploadType = LectureFileUploadType.Image,
                            partSplitMethod = splitMethod,
                            partSplitPlans = splitPlans,
                        ),
                        files = parts,
                    )
                }
            }

            when (result) {
                is NetworkResult.Success -> {
                    val accepted = result.data
                    _uploadedChapterId.value = accepted.chapterId
                    _uploadedLectureUploadId.value = accepted.lectureUploadId
                    startPolling(accepted.lectureUploadId)
                }
                is NetworkResult.Failure -> {
                    _errorMessage.value = result.message
                    _isFailed.value = true
                    _isLoading.value = false
                }
            }
        }
    }

    private fun startPolling(lectureUploadId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var consecutivePollingFailureCount = 0

            while (true) {
                delay(PollingIntervalMillis)
                when (val result = lectureUploadRepository.getUploadStatus(lectureUploadId)) {
                    is NetworkResult.Success -> {
                        consecutivePollingFailureCount = 0
                        val statusData = result.data
                        _progress.value = (statusData.progressPct ?: 0) / 100f
                        when (statusData.status) {
                            LectureUploadStatus.Completed -> {
                                _progress.value = 1f
                                _isSuccess.value = true
                                _isLoading.value = false
                                return@launch
                            }
                            LectureUploadStatus.Failed -> {
                                if (statusData.chapterId.isNotBlank()) {
                                    _uploadedChapterId.value = statusData.chapterId
                                }
                                _errorMessage.value =
                                    statusData.failMessage ?: statusData.failReason ?: "업로드에 실패했어요"
                                _isSuccess.value = false
                                _isFailed.value = true
                                _isLoading.value = false
                                return@launch
                            }
                            else -> {}
                        }
                    }
                    is NetworkResult.Failure -> {
                        consecutivePollingFailureCount += 1
                        if (consecutivePollingFailureCount >= MaxConsecutivePollingFailures) {
                            _errorMessage.value = PollingFailureMessage
                            _isSuccess.value = false
                            _isFailed.value = true
                            _isLoading.value = false
                            return@launch
                        }
                    }
                }
            }
        }
    }

    fun resetSuccess() {
        _isSuccess.value = false
        _uploadedLectureUploadId.value = null
        _uploadedChapterId.value = null
        _progress.value = 0f
    }

    fun cancelUpload() {
        pollingJob?.cancel()
        pollingJob = null
        _isLoading.value = false
        _isSuccess.value = false
        _isFailed.value = false
        _progress.value = 0f
        _uploadedLectureUploadId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    private fun uriToMultipart(uri: Uri, fileName: String): MultipartBody.Part? {
        return runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            val mediaType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val requestBody = bytes.toRequestBody(mediaType.toMediaType())
            MultipartBody.Part.createFormData("files", fileName, requestBody)
        }.getOrNull()
    }

    private companion object {
        const val PollingIntervalMillis = 2_000L
        const val MaxConsecutivePollingFailures = 3
        const val PollingFailureMessage = "업로드 상태를 확인하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요."
    }
}
