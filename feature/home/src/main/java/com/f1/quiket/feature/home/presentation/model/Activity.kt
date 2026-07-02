package com.f1.quiket.feature.home.presentation.model

data class Activity(
    val title: String,
    val questionCount: Int,
    val activityType: ActivityType,
    val description: String,
    val progressPercent: Int? = null,
    val isQuizCreated: Boolean = false,
    val quizSessionId: String? = null,
    val playSessionId: String? = null,
    val resultId: String? = null,
)