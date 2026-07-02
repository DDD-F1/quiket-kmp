package com.f1.quiket.feature.floating.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.floating.data.mapper.toDomain
import com.f1.quiket.feature.floating.data.mapper.toRequest
import com.f1.quiket.feature.floating.data.remote.LectureUploadApi
import com.f1.quiket.feature.floating.domain.model.LectureFileUpload
import com.f1.quiket.feature.floating.domain.model.LectureTextUpload
import com.f1.quiket.feature.floating.domain.model.LectureUploadAccepted
import com.f1.quiket.feature.floating.domain.model.LectureUploadProgress
import com.f1.quiket.feature.floating.domain.model.PartSplitPlan
import com.f1.quiket.feature.floating.domain.model.PartFileAdd
import com.f1.quiket.feature.floating.domain.model.PartTextAdd
import com.f1.quiket.feature.floating.domain.repository.LectureUploadRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class LectureUploadRepositoryImpl @Inject constructor(
    private val api: LectureUploadApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : LectureUploadRepository {
    override suspend fun createTextUpload(
        request: LectureTextUpload,
    ): NetworkResult<LectureUploadAccepted> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.createTextLectureUpload(request.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun createFileUpload(
        request: LectureFileUpload,
        files: List<MultipartBody.Part>,
    ): NetworkResult<LectureUploadAccepted> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = {
                api.createFileLectureUpload(
                    subjectId = request.subjectId.toPlainRequestBody(),
                    chapterName = request.chapterName?.toPlainRequestBody(),
                    uploadType = request.uploadType.wireValue.toPlainRequestBody(),
                    partSplitMethod = request.partSplitMethod.wireValue.toPlainRequestBody(),
                    partSplitPlansJson = request.partSplitPlans.toJsonRequestBody(),
                    files = files,
                )
            },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun addTextPartToChapter(
        chapterId: String,
        request: PartTextAdd,
    ): NetworkResult<LectureUploadAccepted> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = {
                api.addTextPartToChapter(
                    chapterId = chapterId,
                    request = request.toRequest(),
                )
            },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun addFilePartToChapter(
        chapterId: String,
        request: PartFileAdd,
        files: List<MultipartBody.Part>,
    ): NetworkResult<LectureUploadAccepted> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = {
                api.addFilePartToChapter(
                    chapterId = chapterId,
                    partName = request.partName.toPlainRequestBody(),
                    uploadType = request.uploadType.wireValue.toPlainRequestBody(),
                    files = files,
                )
            },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun getUploadStatus(
        lectureUploadId: String,
    ): NetworkResult<LectureUploadProgress> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getLectureUploadStatus(lectureUploadId = lectureUploadId) },
            mapper = { response -> response.toDomain() },
        )
    }

    private fun String.toPlainRequestBody() =
        toRequestBody("text/plain".toMediaType())

    private fun List<PartSplitPlan>.toJsonRequestBody(): RequestBody? =
        takeIf { plans -> plans.isNotEmpty() }
            ?.map { plan -> plan.toRequest() }
            ?.let { plans -> Json.encodeToString(plans).toPlainRequestBody() }
}
