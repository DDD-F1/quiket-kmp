package com.f1.quiket.feature.floating.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.floating.data.mapper.toDomain
import com.f1.quiket.feature.floating.data.mapper.toRequest
import com.f1.quiket.feature.floating.data.remote.ChapterNameUpdateRequest
import com.f1.quiket.feature.floating.data.remote.PartUpdateRequest
import com.f1.quiket.feature.floating.data.remote.SubjectApi
import com.f1.quiket.feature.floating.data.remote.SubjectExamScheduleUpsertRequest
import com.f1.quiket.feature.floating.data.remote.SubjectNameUpdateRequest
import com.f1.quiket.feature.floating.domain.model.Certificate
import com.f1.quiket.feature.floating.domain.model.Part
import com.f1.quiket.feature.floating.domain.model.Subject
import com.f1.quiket.feature.floating.domain.model.SubjectChapter
import com.f1.quiket.feature.floating.domain.model.SubjectCreate
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.SubjectExamSchedule
import com.f1.quiket.feature.floating.domain.model.SubjectSummary
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val api: SubjectApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : SubjectRepository {
    override suspend fun getSubjects(page: Int, size: Int): NetworkResult<List<SubjectSummary>> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getSubjects(page = page, size = size) },
                mapper = { response -> response.content.map { subject -> subject.toDomain() } },
            )
        }

    override suspend fun createSubject(request: SubjectCreate): NetworkResult<Subject> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.createSubject(request.toRequest()) },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun getSubject(subjectId: String): NetworkResult<SubjectDetail> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getSubject(subjectId = subjectId) },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun deleteSubject(subjectId: String): NetworkResult<Unit> =
        withContext(dispatchers.io) {
            responseHandler.executeEmpty { api.deleteSubject(subjectId = subjectId) }
        }

    override suspend fun updateSubjectName(
        subjectId: String,
        name: String,
    ): NetworkResult<Subject> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.updateSubjectName(subjectId, SubjectNameUpdateRequest(name)) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun updateSubjectDetails(
        subjectId: String,
        request: SubjectCreate,
    ): NetworkResult<Subject> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.updateSubjectDetails(subjectId, request.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun upsertExamSchedule(
        subjectId: String,
        examName: String?,
        examDate: String,
    ): NetworkResult<SubjectExamSchedule> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = {
                api.upsertSubjectExamSchedule(
                    subjectId = subjectId,
                    request = SubjectExamScheduleUpsertRequest(
                        examName = examName,
                        examDate = examDate,
                    ),
                )
            },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun deleteExamSchedule(subjectId: String): NetworkResult<Unit> =
        withContext(dispatchers.io) {
            responseHandler.executeEmpty { api.deleteSubjectExamSchedule(subjectId = subjectId) }
        }

    override suspend fun getCertificates(): NetworkResult<List<Certificate>> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getCertificates() },
                mapper = { response -> response.map { certificate -> certificate.toDomain() } },
            )
        }

    override suspend fun deleteChapter(chapterId: String): NetworkResult<Unit> =
        withContext(dispatchers.io) {
            responseHandler.executeEmpty { api.deleteChapter(chapterId = chapterId) }
        }

    override suspend fun updateChapterName(
        chapterId: String,
        name: String,
    ): NetworkResult<SubjectChapter> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.updateChapterName(chapterId, ChapterNameUpdateRequest(name)) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun getPart(partId: String): NetworkResult<Part> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getPart(partId = partId) },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun updatePart(
        partId: String,
        name: String,
        content: String,
    ): NetworkResult<Part> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.updatePart(partId, PartUpdateRequest(name = name, content = content)) },
            mapper = { response -> response.toDomain() },
        )
    }
}
