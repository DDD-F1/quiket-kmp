package com.f1.quiket.feature.floating.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.floating.domain.model.LectureFileUpload
import com.f1.quiket.feature.floating.domain.model.LectureTextUpload
import com.f1.quiket.feature.floating.domain.model.LectureUploadAccepted
import com.f1.quiket.feature.floating.domain.model.LectureUploadProgress
import com.f1.quiket.feature.floating.domain.model.PartFileAdd
import com.f1.quiket.feature.floating.domain.model.PartTextAdd
import okhttp3.MultipartBody

interface LectureUploadRepository {
    suspend fun createTextUpload(request: LectureTextUpload): NetworkResult<LectureUploadAccepted>

    suspend fun createFileUpload(
        request: LectureFileUpload,
        files: List<MultipartBody.Part>,
    ): NetworkResult<LectureUploadAccepted>

    suspend fun addTextPartToChapter(
        chapterId: String,
        request: PartTextAdd,
    ): NetworkResult<LectureUploadAccepted>

    suspend fun addFilePartToChapter(
        chapterId: String,
        request: PartFileAdd,
        files: List<MultipartBody.Part>,
    ): NetworkResult<LectureUploadAccepted>

    suspend fun getUploadStatus(lectureUploadId: String): NetworkResult<LectureUploadProgress>
}
