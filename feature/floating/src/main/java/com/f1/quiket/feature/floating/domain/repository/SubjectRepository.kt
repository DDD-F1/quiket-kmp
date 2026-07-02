package com.f1.quiket.feature.floating.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.model.Certificate
import com.f1.quiket.feature.floating.domain.model.Part
import com.f1.quiket.feature.floating.domain.model.Subject
import com.f1.quiket.feature.floating.domain.model.SubjectChapter
import com.f1.quiket.feature.floating.domain.model.SubjectCreate
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.SubjectExamSchedule
import com.f1.quiket.feature.floating.domain.model.SubjectSummary

interface SubjectRepository {
    suspend fun getSubjects(page: Int = 0, size: Int = 50): NetworkResult<List<SubjectSummary>>

    suspend fun createSubject(request: SubjectCreate): NetworkResult<Subject>

    suspend fun getSubject(subjectId: String): NetworkResult<SubjectDetail>

    suspend fun deleteSubject(subjectId: String): NetworkResult<Unit>

    suspend fun updateSubjectName(subjectId: String, name: String): NetworkResult<Subject>

    suspend fun updateSubjectDetails(subjectId: String, request: SubjectCreate): NetworkResult<Subject>

    suspend fun upsertExamSchedule(
        subjectId: String,
        examName: String?,
        examDate: String,
    ): NetworkResult<SubjectExamSchedule>

    suspend fun deleteExamSchedule(subjectId: String): NetworkResult<Unit>

    suspend fun getCertificates(): NetworkResult<List<Certificate>>

    suspend fun deleteChapter(chapterId: String): NetworkResult<Unit>

    suspend fun updateChapterName(chapterId: String, name: String): NetworkResult<SubjectChapter>

    suspend fun getPart(partId: String): NetworkResult<Part>

    suspend fun updatePart(partId: String, name: String, content: String): NetworkResult<Part>
}
