package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.subject.domain.model.PartDetail
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail

internal sealed interface PartDetailUiState {
    data object Loading : PartDetailUiState
    data class Success(val part: PartDetail) : PartDetailUiState
    data class Error(val message: String) : PartDetailUiState
}

internal sealed interface SubjectDetailUiState {
    data class Loading(val subjectName: String) : SubjectDetailUiState
    data class Success(val subject: SubjectDetail) : SubjectDetailUiState
    data class Error(val subjectName: String, val message: String) : SubjectDetailUiState
}

internal fun SubjectDetail.withUpdatedPart(updatedPart: PartDetail): SubjectDetail = copy(
    chapters = chapters.map { chapter ->
        if (chapter.id != updatedPart.chapterId) {
            chapter
        } else {
            chapter.copy(
                parts = chapter.parts.map { part ->
                    if (part.id == updatedPart.id) {
                        part.copy(
                            name = updatedPart.name,
                            contentPreview = updatedPart.contentPreview
                                ?: updatedPart.content?.take(PartContentPreviewMaxLength),
                        )
                    } else {
                        part
                    }
                },
            )
        }
    },
)

internal fun SubjectDetail.withRenamedChapter(
    chapterId: String,
    name: String,
): SubjectDetail = copy(
    chapters = chapters.map { chapter ->
        if (chapter.id == chapterId) {
            chapter.copy(name = name)
        } else {
            chapter
        }
    },
)

internal fun SubjectDetail.withoutChapter(chapterId: String): SubjectDetail = copy(
    chapters = chapters.filterNot { chapter -> chapter.id == chapterId },
)

private const val PartContentPreviewMaxLength = 80
