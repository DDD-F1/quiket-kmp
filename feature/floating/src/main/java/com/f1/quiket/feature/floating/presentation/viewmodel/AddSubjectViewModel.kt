package com.f1.quiket.feature.floating.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.model.AddSubjectState
import com.f1.quiket.feature.floating.domain.model.ChineseTestType
import com.f1.quiket.feature.floating.domain.model.CourseType
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.EnglishTestType
import com.f1.quiket.feature.floating.domain.model.JapaneseTestType
import com.f1.quiket.feature.floating.domain.model.LanguageType
import com.f1.quiket.feature.floating.domain.model.MiddleHighSubjectType
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.SubjectCreate
import com.f1.quiket.feature.floating.domain.model.SubjectExamDetail
import com.f1.quiket.feature.floating.domain.model.SubjectOtherDetail
import com.f1.quiket.feature.floating.domain.model.SubjectReviewDetail
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import com.f1.quiket.feature.floating.presentation.contract.AddSubjectContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddSubjectContract.State())
    val state: StateFlow<AddSubjectContract.State> = _state.asStateFlow()

    private val _effect = Channel<AddSubjectContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private val _createdSubjectId = MutableStateFlow<String?>(null)
    val createdSubjectId: StateFlow<String?> = _createdSubjectId.asStateFlow()

    private val _updateDetailsSuccess = Channel<Unit>(Channel.BUFFERED)
    val updateDetailsSuccess = _updateDetailsSuccess.receiveAsFlow()

    private val _subjectDetailReady = Channel<Unit>(Channel.BUFFERED)
    val subjectDetailReady = _subjectDetailReady.receiveAsFlow()

    private val _isSavingSubject = MutableStateFlow(false)
    val isSavingSubject: StateFlow<Boolean> = _isSavingSubject.asStateFlow()

    private val _subjectSaveErrorMessage = MutableStateFlow<String?>(null)
    val subjectSaveErrorMessage: StateFlow<String?> = _subjectSaveErrorMessage.asStateFlow()

    private val _existingSubjectNames = MutableStateFlow<List<String>>(emptyList())
    val existingSubjectNames: StateFlow<List<String>> = _existingSubjectNames.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = subjectRepository.getSubjects()) {
                is NetworkResult.Success -> _existingSubjectNames.value = result.data.map { it.name }
                is NetworkResult.Failure -> {}
            }
        }
    }

    fun handleIntent(intent: AddSubjectContract.Intent) {
        when (intent) {
            is AddSubjectContract.Intent.UpdateSubjectName ->
                _state.update { it.copy(subjectName = intent.name) }

            is AddSubjectContract.Intent.SelectStudyPurpose ->
                _state.update { it.copy(studyPurpose = intent.purpose) }

            is AddSubjectContract.Intent.SelectExamType ->
                _state.update { it.copy(examType = intent.type) }

            is AddSubjectContract.Intent.SelectStudyField ->
                _state.update { it.copy(studyField = intent.field) }

            is AddSubjectContract.Intent.SelectUsagePurpose ->
                _state.update { it.copy(usagePurpose = intent.purpose) }

            AddSubjectContract.Intent.GoToNextStep ->
                _state.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }

            AddSubjectContract.Intent.GoToPreviousStep ->
                _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }

            AddSubjectContract.Intent.Skip ->
                viewModelScope.launch { _effect.send(AddSubjectContract.Effect.NavigateToSuccess) }

            AddSubjectContract.Intent.Finish ->
                viewModelScope.launch { _effect.send(AddSubjectContract.Effect.NavigateToSuccess) }
        }
    }

    fun resetForNewSubject() {
        _createdSubjectId.value = null
        _subjectSaveErrorMessage.value = null
        _isSavingSubject.value = false
    }

    fun createSubject(addSubjectState: AddSubjectState) {
        if (_isSavingSubject.value) return

        _isSavingSubject.value = true
        _subjectSaveErrorMessage.value = null
        viewModelScope.launch {
            try {
                val request = addSubjectState.toSubjectCreate()
                when (val result = subjectRepository.createSubject(request)) {
                    is NetworkResult.Success -> {
                        _createdSubjectId.value = result.data.id
                        _subjectDetailReady.send(Unit)
                    }

                    is NetworkResult.Failure -> {
                        _subjectSaveErrorMessage.value =
                            result.message.ifBlank { "과목을 만들지 못했어요." }
                    }
                }
            } finally {
                _isSavingSubject.value = false
            }
        }
    }

    fun updateSubjectDetails(subjectId: String, addSubjectState: AddSubjectState) {
        if (_isSavingSubject.value) return

        _isSavingSubject.value = true
        _subjectSaveErrorMessage.value = null
        viewModelScope.launch {
            try {
                val request = addSubjectState.toSubjectCreate()
                when (val result = subjectRepository.updateSubjectDetails(subjectId, request)) {
                    is NetworkResult.Success -> {
                        _updateDetailsSuccess.send(Unit)
                        _subjectDetailReady.send(Unit)
                    }

                    is NetworkResult.Failure -> {
                        _subjectSaveErrorMessage.value =
                            result.message.ifBlank { "과목 정보를 저장하지 못했어요." }
                    }
                }
            } finally {
                _isSavingSubject.value = false
            }
        }
    }

    fun clearSubjectSaveError() {
        _subjectSaveErrorMessage.value = null
    }
}

private fun AddSubjectState.toSubjectCreate(): SubjectCreate {
    val purposeKey = when (studyPurpose) {
        StudyPurpose.EXAM -> "exam"
        StudyPurpose.SELF_STUDY -> "review"
        StudyPurpose.OTHER -> "other"
        null -> "other"
    }

    val examDetail = if (studyPurpose == StudyPurpose.EXAM) {
        SubjectExamDetail(
            examType = when (examType) {
                ExamType.UNIVERSITY -> "university"
                ExamType.MIDDLE_HIGH -> "middle_high"
                ExamType.CERTIFICATE -> "certificate"
                ExamType.CIVIL_SERVICE -> "civil_service"
                ExamType.LANGUAGE -> "language"
                ExamType.OTHER_EXAM -> "other_exam"
                null -> "other_exam"
            },
            univMajorField = majorCategory?.name?.lowercase(),
            univMajorName = majorName.ifBlank { null },
            univCourseType = when (courseType) {
                CourseType.MAJOR -> "major"
                CourseType.LIBERAL -> "liberal_arts"
                null -> null
            },
            mhGrade = curriculum?.name?.lowercase(),
            mhSubjectType = if (subjectType == MiddleHighSubjectType.CUSTOM) {
                customSubjectType.ifBlank { null }
            } else {
                subjectType?.name?.lowercase()
            },
            certificateName = certificateName.ifBlank { null },
            langType = languageType?.name?.lowercase(),
            langExamName = when (languageType) {
                LanguageType.ENGLISH -> if (englishTest == EnglishTestType.CUSTOM) customLanguageTest.ifBlank { null } else englishTest?.name?.lowercase()
                LanguageType.JAPANESE -> if (japaneseTest == JapaneseTestType.CUSTOM) customLanguageTest.ifBlank { null } else japaneseTest?.name?.lowercase()
                LanguageType.CHINESE -> if (chineseTest == ChineseTestType.CUSTOM) customLanguageTest.ifBlank { null } else chineseTest?.name?.lowercase()
                null -> null
            },
            civilRank = civilServantGrade?.name?.lowercase(),
            civilSeries = civilServantSeries?.name?.lowercase(),
            otherExamName = otherExamText.ifBlank { null },
        )
    } else null

    val reviewDetail = if (studyPurpose == StudyPurpose.SELF_STUDY) {
        SubjectReviewDetail(
            field = studyField?.name?.lowercase() ?: "",
            studyLevel = familiarityLevel?.name?.lowercase() ?: "",
        )
    } else null

    val otherDetail = if (studyPurpose == StudyPurpose.OTHER) {
        SubjectOtherDetail(
            usagePurpose = usagePurpose?.name?.lowercase() ?: "",
            description = additionalDescription.ifBlank { null },
        )
    } else null

    return SubjectCreate(
        name = subjectName.ifBlank { "새 과목" },
        purpose = purposeKey,
        examDetail = examDetail,
        reviewDetail = reviewDetail,
        otherDetail = otherDetail,
    )
}
