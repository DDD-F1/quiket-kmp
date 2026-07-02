package com.f1.quiket.feature.floating.presentation.contract

import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.UsagePurpose

object AddSubjectContract {

    data class State(
        val currentStep: Int = 1,
        val subjectName: String = "",
        val studyPurpose: StudyPurpose? = null,
        val examType: ExamType? = null,
        val studyField: StudyField? = null,
        val usagePurpose: UsagePurpose? = null,
    )

    sealed class Intent {
        data class UpdateSubjectName(val name: String) : Intent()
        data class SelectStudyPurpose(val purpose: StudyPurpose) : Intent()
        data class SelectExamType(val type: ExamType) : Intent()
        data class SelectStudyField(val field: StudyField) : Intent()
        data class SelectUsagePurpose(val purpose: UsagePurpose) : Intent()
        object GoToNextStep : Intent()
        object GoToPreviousStep : Intent()
        object Skip : Intent()
        object Finish : Intent()
    }

    sealed class Effect {
        object NavigateBack : Effect()
        object NavigateToSuccess : Effect()
    }
}