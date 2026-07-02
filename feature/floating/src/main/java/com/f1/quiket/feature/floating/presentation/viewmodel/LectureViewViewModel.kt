package com.f1.quiket.feature.floating.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.model.TocChapter
import com.f1.quiket.feature.floating.domain.model.TocPart
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureViewViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
) : ViewModel() {

    private val _tocChapters = MutableStateFlow<List<TocChapter>>(emptyList())
    val tocChapters: StateFlow<List<TocChapter>> = _tocChapters.asStateFlow()

    // Flat ordered list of all part IDs for prev/next navigation
    private val _allPartIds = MutableStateFlow<List<String>>(emptyList())
    val allPartIds: StateFlow<List<String>> = _allPartIds.asStateFlow()

    private val _currentPartId = MutableStateFlow<String?>(null)
    val currentPartId: StateFlow<String?> = _currentPartId.asStateFlow()

    private val _currentPartName = MutableStateFlow<String?>(null)
    val currentPartName: StateFlow<String?> = _currentPartName.asStateFlow()

    private val _currentPartContent = MutableStateFlow<String?>(null)
    val currentPartContent: StateFlow<String?> = _currentPartContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPartLoading = MutableStateFlow(false)
    val isPartLoading: StateFlow<Boolean> = _isPartLoading.asStateFlow()

    private val _isUpdatingPartName = MutableStateFlow(false)
    val isUpdatingPartName: StateFlow<Boolean> = _isUpdatingPartName.asStateFlow()

    private val _events = MutableSharedFlow<LectureViewEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<LectureViewEvent> = _events.asSharedFlow()

    private var loadedPartId: String? = null
    private val partNameOverrides = mutableMapOf<String, String>()

    fun loadSubject(subjectId: String, initialChapterId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = subjectRepository.getSubject(subjectId)) {
                is NetworkResult.Success -> {
                    val detail = result.data

                    _tocChapters.value = detail.chapters.map { chapter ->
                        TocChapter(
                            id = chapter.id,
                            number = chapter.displayOrder,
                            title = chapter.name,
                            parts = chapter.parts.map { part ->
                                TocPart(
                                    id = part.id,
                                    partNumber = part.partNumber,
                                    title = partNameOverrides[part.id] ?: part.name,
                                )
                            },
                        )
                    }

                    val allPartIds = detail.chapters.flatMap { chapter ->
                        chapter.parts.map { it.id }
                    }
                    _allPartIds.value = allPartIds

                    // Start at first part of initial chapter, fallback to first part overall
                    val initialPartId = detail.chapters
                        .firstOrNull { it.id == initialChapterId }
                        ?.parts?.firstOrNull()?.id
                        ?: allPartIds.firstOrNull()

                    initialPartId?.let { selectPart(it) }
                }
                is NetworkResult.Failure -> {}
            }
            _isLoading.value = false
        }
    }

    fun selectPart(partId: String) {
        if (_currentPartId.value == partId && loadedPartId == partId) return
        _currentPartId.value = partId
        loadedPartId = null
        _currentPartName.value = null
        _currentPartContent.value = null
        viewModelScope.launch {
            _isPartLoading.value = true
            when (val result = subjectRepository.getPart(partId)) {
                is NetworkResult.Success -> {
                    loadedPartId = partId
                    _currentPartName.value = partNameOverrides[partId] ?: result.data.name
                    _currentPartContent.value = result.data.content
                }
                is NetworkResult.Failure -> {}
            }
            _isPartLoading.value = false
        }
    }

    fun updatePartName(partId: String, name: String) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) {
                _events.emit(LectureViewEvent.ShowMessage("파트명을 입력해주세요."))
                return@launch
            }
            if (_isUpdatingPartName.value) return@launch
            if (_isPartLoading.value || loadedPartId != partId) {
                _events.emit(LectureViewEvent.ShowMessage("파트 정보를 불러온 뒤 다시 시도해주세요."))
                return@launch
            }

            _isUpdatingPartName.value = true
            val content = _currentPartContent.value.orEmpty()
            when (val result = subjectRepository.updatePart(partId, trimmedName, content)) {
                is NetworkResult.Success -> {
                    val updatedName = trimmedName
                    partNameOverrides[partId] = updatedName
                    _currentPartName.value = updatedName
                    updateTocPartName(partId = partId, name = updatedName)
                    _events.emit(LectureViewEvent.PartNameUpdated(updatedName))
                }
                is NetworkResult.Failure -> {
                    _events.emit(LectureViewEvent.ShowMessage(result.message))
                }
            }
            _isUpdatingPartName.value = false
        }
    }

    private fun updateTocPartName(partId: String, name: String) {
        _tocChapters.value = _tocChapters.value.map { chapter ->
            chapter.copy(
                parts = chapter.parts.map { part ->
                    if (part.id == partId) part.copy(title = name) else part
                },
            )
        }
    }

    fun updatePartContent(partId: String, content: String) {
        viewModelScope.launch {
            val name = _currentPartName.value ?: ""
            when (val result = subjectRepository.updatePart(partId, name, content)) {
                is NetworkResult.Success -> {
                    _currentPartContent.value = result.data.content
                }
                is NetworkResult.Failure -> {}
            }
        }
    }
}

sealed interface LectureViewEvent {
    data class ShowMessage(val message: String) : LectureViewEvent
    data class PartNameUpdated(val name: String) : LectureViewEvent
}
