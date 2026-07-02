package com.f1.quiket.feature.floating.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import com.f1.quiket.feature.floating.presentation.screen.lectureselect.LectureSelectItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureSelectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
) : ViewModel() {

    private val _subjects = MutableStateFlow<List<LectureSelectItem>>(emptyList())
    val subjects: StateFlow<List<LectureSelectItem>> = _subjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = subjectRepository.getSubjects()) {
                is NetworkResult.Success -> {
                    _subjects.value = result.data.map { subject ->
                        LectureSelectItem(
                            id = subject.id,
                            category = subject.purpose,
                            title = subject.name,
                            chapterCount = subject.chapterCount,
                        )
                    }
                }
                is NetworkResult.Failure -> {}
            }
            _isLoading.value = false
        }
    }
}