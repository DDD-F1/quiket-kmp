package com.f1.quiket.feature.floating.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.SubjectExamSchedule
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
) : ViewModel() {

    private val _subjectDetail = MutableStateFlow<SubjectDetail?>(null)
    val subjectDetail: StateFlow<SubjectDetail?> = _subjectDetail.asStateFlow()

    private val _examSchedule = MutableStateFlow<SubjectExamSchedule?>(null)
    val examSchedule: StateFlow<SubjectExamSchedule?> = _examSchedule.asStateFlow()

    private val _examScheduleUpdated = MutableSharedFlow<SubjectExamSchedule>(extraBufferCapacity = 1)
    val examScheduleUpdated: SharedFlow<SubjectExamSchedule> = _examScheduleUpdated.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _examScheduleSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val examScheduleSaved: SharedFlow<Unit> = _examScheduleSaved.asSharedFlow()

    private val _deleteSubjectSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val deleteSubjectSuccess: SharedFlow<Unit> = _deleteSubjectSuccess.asSharedFlow()

    private val _deleteChapterSuccess = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deleteChapterSuccess: SharedFlow<String> = _deleteChapterSuccess.asSharedFlow()

    private var loadedSubjectId: String? = null
    private var loadSubjectJob: Job? = null

    fun loadSubject(subjectId: String) {
        loadSubjectJob?.cancel()
        if (loadedSubjectId != subjectId) {
            _subjectDetail.value = null
            _examSchedule.value = null
        }
        loadSubjectJob = viewModelScope.launch {
            _isLoading.value = true
            when (val result = subjectRepository.getSubject(subjectId)) {
                is NetworkResult.Success -> {
                    loadedSubjectId = subjectId
                    _subjectDetail.value = result.data
                    _examSchedule.value = result.data.examSchedule
                }
                is NetworkResult.Failure -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateSubjectName(subjectId: String, name: String) {
        viewModelScope.launch {
            when (val result = subjectRepository.updateSubjectName(subjectId, name)) {
                is NetworkResult.Success -> {
                    _subjectDetail.value = _subjectDetail.value?.copy(name = result.data.name)
                }
                is NetworkResult.Failure -> {}
            }
        }
    }

    fun updateChapterName(chapterId: String, name: String) {
        viewModelScope.launch {
            when (val result = subjectRepository.updateChapterName(chapterId, name)) {
                is NetworkResult.Success -> {
                    _subjectDetail.value = _subjectDetail.value?.let { detail ->
                        detail.copy(
                            chapters = detail.chapters.map { chapter ->
                                if (chapter.id == chapterId) chapter.copy(name = name) else chapter
                            },
                        )
                    }
                }
                is NetworkResult.Failure -> {}
            }
        }
    }

    fun deleteChapter(chapterId: String) {
        viewModelScope.launch {
            when (subjectRepository.deleteChapter(chapterId)) {
                is NetworkResult.Success -> {
                    _subjectDetail.value = _subjectDetail.value?.let { detail ->
                        detail.copy(chapters = detail.chapters.filter { it.id != chapterId })
                    }
                    _deleteChapterSuccess.emit(chapterId)
                }
                is NetworkResult.Failure -> {}
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            when (subjectRepository.deleteSubject(subjectId)) {
                is NetworkResult.Success -> _deleteSubjectSuccess.emit(Unit)
                is NetworkResult.Failure -> {}
            }
        }
    }

    fun upsertExamSchedule(subjectId: String, examName: String?, examDate: String) {
        viewModelScope.launch {
            when (val result = subjectRepository.upsertExamSchedule(subjectId, examName, examDate)) {
                is NetworkResult.Success -> {
                    _examSchedule.value = result.data
                    _examScheduleUpdated.emit(result.data)
                    _examScheduleSaved.emit(Unit)
                }
                is NetworkResult.Failure -> {}
            }
        }
    }

    fun deleteExamSchedule(subjectId: String) {
        viewModelScope.launch {
            when (subjectRepository.deleteExamSchedule(subjectId)) {
                is NetworkResult.Success -> _examSchedule.value = null
                is NetworkResult.Failure -> {}
            }
        }
    }
}