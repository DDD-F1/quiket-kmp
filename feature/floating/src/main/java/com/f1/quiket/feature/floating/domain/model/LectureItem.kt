package com.f1.quiket.feature.floating.domain.model

data class LectureItem(
    val id: Int,
    val number: String,   // "1.", "1-1." 등
    val title: String,
    val content: String,
    val isClipped: Boolean = false,
)

data class TocChapter(
    val id: String,
    val number: Int,
    val title: String,
    val parts: List<TocPart>,
)

data class TocPart(
    val id: String,
    val partNumber: Int,
    val title: String,
    val isSelected: Boolean = false,
)
