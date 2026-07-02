package com.f1.quiket.feature.home.presentation.model

sealed class FabAction {
    object ScheduleExam: FabAction()
    object CreateQuiz: FabAction()
    object  Upload: FabAction()
    object  AddSubject: FabAction()
}