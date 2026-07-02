package com.f1.quiket.feature.home.presentation.model

data class Subject(
    val id: String,
    val title: String,
    val chapter: String,
    val purpose: String,
    val isStarred: Boolean
)