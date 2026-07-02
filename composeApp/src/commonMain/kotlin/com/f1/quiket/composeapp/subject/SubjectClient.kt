package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.content.ByteArrayContent
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal class SubjectClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getSubjects(
        session: SessionSnapshot,
        page: Int = 0,
        size: Int = 50,
    ): List<SubjectListItem> {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}subjects") {
            header("Authorization", authorization)
            parameter("page", page)
            parameter("size", size)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "과목 정보를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("과목 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectPageResponse>(data).content.map { it.toDomain() }
        }.getOrElse {
            throw SubjectException("과목 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun createSubject(
        session: SessionSnapshot,
        input: SubjectCreateInput,
    ): CreatedSubject {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}subjects") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(input.toRequest())
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "과목을 만들지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("과목 생성 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("과목 생성 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun getSubject(
        session: SessionSnapshot,
        subjectId: String,
    ): SubjectDetail {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}subjects/$subjectId") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "과목 정보를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("과목 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectDetailResponse>(data).toDomain(json)
        }.getOrElse {
            throw SubjectException("과목 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun deleteSubject(
        session: SessionSnapshot,
        subjectId: String,
    ) {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.delete("${baseUrl.ensureTrailingSlash()}subjects/$subjectId") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "과목을 삭제하지 못했습니다." })
        }
    }

    suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ): SubjectExamSchedule {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.put("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/exam-schedule") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                SubjectExamScheduleUpsertRequest(
                    examName = examName,
                    examDate = examDate,
                ),
            )
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "시험 일정을 저장하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("시험 일정 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectExamScheduleResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("시험 일정 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun deleteExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
    ) {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.delete("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/exam-schedule") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "시험 일정을 삭제하지 못했습니다." })
        }
    }

    suspend fun getCertificates(session: SessionSnapshot): List<Certificate> {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}certificates") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "자격증 목록을 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("자격증 목록 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<List<CertificateResponse>>(data)
                .map { it.toDomain() }
                .sortedWith(compareBy<Certificate> { it.displayOrder }.thenBy { it.name })
        }.getOrElse {
            throw SubjectException("자격증 목록 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun updateSubjectName(
        session: SessionSnapshot,
        subjectId: String,
        name: String,
    ): CreatedSubject {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.patch("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/name") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(SubjectNameUpdateRequest(name = name))
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "과목명을 수정하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("과목명 수정 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("과목명 수정 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun updateSubjectDetails(
        session: SessionSnapshot,
        subjectId: String,
        input: SubjectCreateInput,
    ): CreatedSubject {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.put("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/details") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(input.toRequest())
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "과목 유형을 수정하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("과목 유형 수정 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("과목 유형 수정 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun updateChapterName(
        session: SessionSnapshot,
        chapterId: String,
        name: String,
    ): Chapter {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.patch("${baseUrl.ensureTrailingSlash()}chapters/$chapterId/name") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(ChapterNameUpdateRequest(name = name))
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "챕터명을 수정하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("챕터명 수정 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<ChapterResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("챕터명 수정 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun deleteChapter(
        session: SessionSnapshot,
        chapterId: String,
    ) {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.delete("${baseUrl.ensureTrailingSlash()}chapters/$chapterId") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(
                message = envelope.message.ifBlank { "로그인이 만료되었습니다." },
                isUnauthorized = true,
                statusCode = response.status.value,
            )
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(
                message = envelope.message.ifBlank { "챕터를 삭제하지 못했습니다." },
                statusCode = response.status.value,
            )
        }
    }

    suspend fun getPart(
        session: SessionSnapshot,
        partId: String,
    ): PartDetail {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}parts/$partId") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "파트 정보를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("파트 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<PartResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("파트 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun updatePart(
        session: SessionSnapshot,
        partId: String,
        name: String,
        content: String,
    ): PartDetail {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.patch("${baseUrl.ensureTrailingSlash()}parts/$partId") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(PartUpdateRequest(name = name, content = content))
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "파트 정보를 저장하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("파트 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<PartResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("파트 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun createTextLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        text: String,
        partSplitMethod: PartSplitMethod = PartSplitMethod.Auto,
        partSplitPlans: List<PartSplitPlan> = emptyList(),
    ): LectureUploadAccepted {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}lecture-uploads") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                LectureTextUploadRequest(
                    subjectId = subjectId,
                    chapterName = chapterName,
                    uploadType = "text",
                    partSplitMethod = partSplitMethod.wireValue,
                    text = text,
                    partSplitPlans = partSplitPlans.takeIf { it.isNotEmpty() }
                        ?.map { it.toRequest() },
                ),
            )
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "자료 업로드를 시작하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("자료 업로드 응답에 데이터가 없습니다.")
        return decodeLectureUploadAccepted(data)
    }

    suspend fun createFileLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
        partSplitMethod: PartSplitMethod = PartSplitMethod.Auto,
        partSplitPlans: List<PartSplitPlan> = emptyList(),
    ): LectureUploadAccepted {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)
        if (files.isEmpty()) {
            throw SubjectException("업로드할 파일을 선택해주세요.")
        }

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}lecture-uploads") {
            header("Authorization", authorization)
            setBody(
                buildMultipartContent(
                    textParts = buildList {
                        add("subjectId" to subjectId)
                        chapterName?.takeIf { it.isNotBlank() }?.let { add("chapterName" to it) }
                        add("uploadType" to uploadType.wireValue)
                        add("partSplitMethod" to partSplitMethod.wireValue)
                        partSplitPlans.takeIf { it.isNotEmpty() }?.let { plans ->
                            add(
                                "partSplitPlansJson" to json.encodeToString(
                                    ListSerializer(PartSplitPlanRequest.serializer()),
                                    plans.map { it.toRequest() },
                                ),
                            )
                        }
                    },
                    files = files,
                ),
            )
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "자료 업로드를 시작하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("자료 업로드 응답에 데이터가 없습니다.")
        return decodeLectureUploadAccepted(data)
    }

    suspend fun addTextPartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        text: String,
    ): LectureUploadAccepted {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}chapters/$chapterId/parts") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                PartTextAddRequest(
                    partName = partName,
                    uploadType = "text",
                    text = text,
                ),
            )
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "파트 추가를 시작하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("파트 추가 응답에 데이터가 없습니다.")
        return decodeLectureUploadAccepted(data)
    }

    suspend fun addFilePartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
    ): LectureUploadAccepted {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)
        if (files.isEmpty()) {
            throw SubjectException("업로드할 파일을 선택해주세요.")
        }

        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}chapters/$chapterId/parts") {
            header("Authorization", authorization)
            setBody(
                buildMultipartContent(
                    textParts = listOf(
                        "partName" to partName,
                        "uploadType" to uploadType.wireValue,
                    ),
                    files = files,
                ),
            )
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "파트 추가를 시작하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("파트 추가 응답에 데이터가 없습니다.")
        return decodeLectureUploadAccepted(data)
    }

    suspend fun getLectureUploadStatus(
        session: SessionSnapshot,
        lectureUploadId: String,
    ): LectureUploadProgress {
        val authorization = session.authorizationHeader()
            ?: throw SubjectException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}lecture-uploads/$lectureUploadId/status") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw SubjectException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw SubjectException(envelope.message.ifBlank { "업로드 상태를 확인하지 못했습니다." })
        }

        val data = envelope.data ?: throw SubjectException("업로드 상태 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<LectureUploadStatusResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("업로드 상태 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw SubjectException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }

    private fun decodeLectureUploadAccepted(data: JsonElement): LectureUploadAccepted =
        runCatching {
            json.decodeFromJsonElement<LectureUploadAcceptedResponse>(data).toDomain()
        }.getOrElse {
            throw SubjectException("자료 업로드 응답을 해석하지 못했습니다.")
        }
}

internal class SubjectException(
    message: String,
    val isUnauthorized: Boolean = false,
    val statusCode: Int? = null,
) : Exception(message)

internal data class SubjectDetail(
    val id: String,
    val name: String,
    val purpose: String,
    val detailLabel: String?,
    val examDetail: SubjectExamDetail? = null,
    val reviewDetail: SubjectReviewDetail? = null,
    val otherDetail: SubjectOtherDetail? = null,
    val createdAt: String,
    val examSchedule: SubjectExamSchedule?,
    val chapters: List<ChapterWithParts>,
)

internal data class SubjectListItem(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
)

internal data class CreatedSubject(
    val id: String,
    val name: String,
    val purpose: String,
    val createdAt: String,
)

internal data class SubjectCreateInput(
    val name: String,
    val purpose: SubjectCreatePurpose,
    val examDetail: SubjectExamDetailInput? = null,
    val reviewDetail: SubjectReviewDetailInput? = null,
    val otherDetail: SubjectOtherDetailInput? = null,
)

internal data class SubjectExamDetailInput(
    val examType: String,
    val univMajorField: String? = null,
    val univMajorName: String? = null,
    val univCourseType: String? = null,
    val mhGrade: String? = null,
    val mhSubjectType: String? = null,
    val certificateId: String? = null,
    val certificateName: String? = null,
    val civilRank: String? = null,
    val civilSeries: String? = null,
    val langType: String? = null,
    val langExamName: String? = null,
    val otherExamName: String? = null,
)

internal data class SubjectReviewDetailInput(
    val field: String,
    val studyLevel: String,
)

internal data class SubjectOtherDetailInput(
    val usagePurpose: String,
    val description: String? = null,
)

internal data class SubjectExamDetail(
    val examType: String,
    val univMajorField: String? = null,
    val univMajorName: String? = null,
    val univCourseType: String? = null,
    val mhGrade: String? = null,
    val mhSubjectType: String? = null,
    val certificateId: String? = null,
    val certificateName: String? = null,
    val civilRank: String? = null,
    val civilSeries: String? = null,
    val langType: String? = null,
    val langExamName: String? = null,
    val otherExamName: String? = null,
)

internal data class SubjectReviewDetail(
    val field: String,
    val studyLevel: String,
)

internal data class SubjectOtherDetail(
    val usagePurpose: String,
    val description: String? = null,
)

internal data class Chapter(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
)

internal enum class SubjectCreatePurpose(
    val title: String,
    val description: String,
    val wireValue: String,
) {
    Exam(
        title = "시험·자격증 대비",
        description = "시험일, 범위를 체계적으로 준비해요",
        wireValue = "exam",
    ),
    Review(
        title = "자기계발·일반 복습",
        description = "언제든 퀴즈로 복습해요",
        wireValue = "review",
    ),
    Other(
        title = "기타",
        description = "자유롭게 설정할게요",
        wireValue = "other",
    ),
}

internal data class SubjectExamSchedule(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int?,
)

internal data class Certificate(
    val id: Long,
    val name: String,
    val featured: Boolean,
    val displayOrder: Int,
)

internal data class ChapterWithParts(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<PartSummary>,
)

internal data class PartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)

internal data class PartDetail(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
    val subjectId: String?,
    val lectureUploadId: String?,
    val content: String?,
)

internal data class LectureUploadAccepted(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: LectureUploadStatus,
    val estimatedSeconds: Int?,
)

internal data class LectureUploadProgress(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: LectureUploadStatus,
    val estimatedSeconds: Int?,
    val chapterName: String?,
    val progressPct: Int?,
    val parts: List<PartSummary>,
    val failCode: String?,
    val failMessage: String?,
    val failReason: String?,
)

internal data class PartSplitPlan(
    val partNumber: Int,
    val intendedName: String? = null,
)

internal enum class PartSplitMethod(
    val wireValue: String,
) {
    Auto("auto"),
    Manual("manual"),
}

internal enum class LectureUploadStatus(
    val wireValue: String,
) {
    Pending("pending"),
    Processing("processing"),
    Completed("completed"),
    Failed("failed"),
    Unknown("unknown"),
}

internal enum class LectureFileUploadType(
    val wireValue: String,
) {
    Pdf("pdf"),
    Image("image"),
}

@Serializable
private data class SubjectPageResponse(
    val content: List<SubjectSummaryResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
)

@Serializable
private data class SubjectSummaryResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
) {
    fun toDomain(): SubjectListItem = SubjectListItem(
        id = id,
        name = name,
        purpose = purpose,
        chapterCount = chapterCount,
        partCount = partCount,
    )
}

@Serializable
private data class CertificateResponse(
    val id: Long,
    val name: String,
    val featured: Boolean = false,
    val displayOrder: Int = Int.MAX_VALUE,
) {
    fun toDomain(): Certificate = Certificate(
        id = id,
        name = name,
        featured = featured,
        displayOrder = displayOrder,
    )
}

@Serializable
private data class SubjectResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val createdAt: String,
) {
    fun toDomain(): CreatedSubject = CreatedSubject(
        id = id,
        name = name,
        purpose = purpose,
        createdAt = createdAt,
    )
}

@Serializable
private data class SubjectCreateRequest(
    val name: String,
    val purpose: String,
    val examDetail: SubjectExamDetailRequest? = null,
    val reviewDetail: SubjectReviewDetailRequest? = null,
    val otherDetail: SubjectOtherDetailRequest? = null,
)

private fun SubjectCreateInput.toRequest(): SubjectCreateRequest = SubjectCreateRequest(
    name = name.ifBlank { "새 과목" },
    purpose = purpose.wireValue,
    examDetail = examDetail?.toRequest(),
    reviewDetail = reviewDetail?.toRequest(),
    otherDetail = otherDetail?.toRequest(),
)

private fun SubjectExamDetailInput.toRequest(): SubjectExamDetailRequest = SubjectExamDetailRequest(
    examType = examType,
    univMajorField = univMajorField,
    univMajorName = univMajorName,
    univCourseType = univCourseType,
    mhGrade = mhGrade,
    mhSubjectType = mhSubjectType,
    certificateId = certificateId,
    certificateName = certificateName,
    civilRank = civilRank,
    civilSeries = civilSeries,
    langType = langType,
    langExamName = langExamName,
    otherExamName = otherExamName,
)

private fun SubjectReviewDetailInput.toRequest(): SubjectReviewDetailRequest = SubjectReviewDetailRequest(
    field = field,
    studyLevel = studyLevel,
)

private fun SubjectOtherDetailInput.toRequest(): SubjectOtherDetailRequest = SubjectOtherDetailRequest(
    usagePurpose = usagePurpose,
    description = description,
)

@Serializable
private data class SubjectNameUpdateRequest(
    val name: String,
)

@Serializable
private data class SubjectExamScheduleUpsertRequest(
    val examName: String? = null,
    val examDate: String,
)

@Serializable
private data class ChapterNameUpdateRequest(
    val name: String,
)

@Serializable
private data class SubjectExamDetailRequest(
    val examType: String,
    val univMajorField: String? = null,
    val univMajorName: String? = null,
    val univCourseType: String? = null,
    val mhGrade: String? = null,
    val mhSubjectType: String? = null,
    val certificateId: String? = null,
    val certificateName: String? = null,
    val civilRank: String? = null,
    val civilSeries: String? = null,
    val langType: String? = null,
    val langExamName: String? = null,
    val otherExamName: String? = null,
)

@Serializable
private data class SubjectReviewDetailRequest(
    val field: String,
    val studyLevel: String,
)

@Serializable
private data class SubjectOtherDetailRequest(
    val usagePurpose: String,
    val description: String? = null,
)

@Serializable
private data class SubjectDetailResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val detail: JsonElement? = null,
    val examDetail: JsonElement? = null,
    val reviewDetail: JsonElement? = null,
    val otherDetail: JsonElement? = null,
    val createdAt: String,
    val examSchedule: SubjectExamScheduleResponse? = null,
    val chapters: List<ChapterWithPartsResponse> = emptyList(),
) {
    fun toDomain(json: Json): SubjectDetail {
        val purposeLower = purpose.lowercase()
        val detailWrapper = detail?.takeIf { it != JsonNull } as? JsonObject
        val examDetailJson = examDetail?.takeIf { it != JsonNull }
            ?: detailWrapper?.get("examDetail")?.takeIf { it != JsonNull }
            ?: detail?.takeIf { it != JsonNull }
        val reviewDetailJson = reviewDetail?.takeIf { it != JsonNull }
            ?: detailWrapper?.get("reviewDetail")?.takeIf { it != JsonNull }
            ?: detail?.takeIf { it != JsonNull }
        val otherDetailJson = otherDetail?.takeIf { it != JsonNull }
            ?: detailWrapper?.get("otherDetail")?.takeIf { it != JsonNull }
            ?: detail?.takeIf { it != JsonNull }
        val mappedExamDetail = if (purposeLower == "exam") {
            examDetailJson?.decodeOrNull<SubjectExamDetailResponse>(json)?.toDomain()
        } else {
            null
        }
        val mappedReviewDetail = if (purposeLower == "review" || purposeLower == "self_study") {
            reviewDetailJson?.decodeOrNull<SubjectReviewDetailResponse>(json)?.toDomain()
        } else {
            null
        }
        val mappedOtherDetail = if (purposeLower == "other") {
            otherDetailJson?.decodeOrNull<SubjectOtherDetailResponse>(json)?.toDomain()
        } else {
            null
        }

        return SubjectDetail(
            id = id,
            name = name,
            purpose = purpose,
            detailLabel = when (purposeLower) {
                "exam" -> mappedExamDetail?.displayLabel()
                "review",
                "self_study",
                -> mappedReviewDetail?.displayLabel()
                "other" -> mappedOtherDetail?.displayLabel()
                else -> null
            },
            examDetail = mappedExamDetail,
            reviewDetail = mappedReviewDetail,
            otherDetail = mappedOtherDetail,
            createdAt = createdAt,
            examSchedule = examSchedule?.toDomain(),
            chapters = chapters.map { it.toDomain() },
        )
    }
}

@Serializable
private data class SubjectExamDetailResponse(
    val examType: String = "",
    val univMajorField: String? = null,
    val certificateName: String? = null,
    val certificateId: String? = null,
    val otherExamName: String? = null,
    val langExamName: String? = null,
    val langType: String? = null,
    val mhGrade: String? = null,
    val mhSubjectType: String? = null,
    val civilRank: String? = null,
    val civilSeries: String? = null,
    val univMajorName: String? = null,
    val univCourseType: String? = null,
) {
    fun toDomain(): SubjectExamDetail = SubjectExamDetail(
        examType = examType,
        univMajorField = univMajorField,
        univMajorName = univMajorName,
        univCourseType = univCourseType,
        mhGrade = mhGrade,
        mhSubjectType = mhSubjectType,
        certificateId = certificateId,
        certificateName = certificateName,
        civilRank = civilRank,
        civilSeries = civilSeries,
        langType = langType,
        langExamName = langExamName,
        otherExamName = otherExamName,
    )
}

@Serializable
private data class SubjectReviewDetailResponse(
    val field: String = "",
    val studyLevel: String = "",
) {
    fun toDomain(): SubjectReviewDetail = SubjectReviewDetail(
        field = field,
        studyLevel = studyLevel,
    )
}

@Serializable
private data class SubjectOtherDetailResponse(
    val usagePurpose: String = "",
    val description: String? = null,
) {
    fun toDomain(): SubjectOtherDetail = SubjectOtherDetail(
        usagePurpose = usagePurpose,
        description = description,
    )
}

@Serializable
private data class SubjectExamScheduleResponse(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int? = null,
) {
    fun toDomain(): SubjectExamSchedule = SubjectExamSchedule(
        id = id,
        subjectId = subjectId,
        examName = examName,
        examDate = examDate,
        dDay = dDay,
    )
}

@Serializable
private data class ChapterWithPartsResponse(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<PartSummaryResponse> = emptyList(),
) {
    fun toDomain(): ChapterWithParts = ChapterWithParts(
        id = id,
        subjectId = subjectId,
        name = name,
        displayOrder = displayOrder,
        parts = parts.map { it.toDomain() },
    )
}

@Serializable
private data class ChapterResponse(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
) {
    fun toDomain(): Chapter = Chapter(
        id = id,
        subjectId = subjectId,
        name = name,
        displayOrder = displayOrder,
    )
}

@Serializable
private data class PartSummaryResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
) {
    fun toDomain(): PartSummary = PartSummary(
        id = id,
        chapterId = chapterId,
        name = name,
        partNumber = partNumber,
        contentPreview = contentPreview,
    )
}

@Serializable
private data class PartResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
    val subjectId: String? = null,
    val lectureUploadId: String? = null,
    val content: String? = null,
) {
    fun toDomain(): PartDetail = PartDetail(
        id = id,
        chapterId = chapterId,
        name = name,
        partNumber = partNumber,
        contentPreview = contentPreview,
        subjectId = subjectId,
        lectureUploadId = lectureUploadId,
        content = content,
    )
}

@Serializable
private data class PartUpdateRequest(
    val name: String,
    val content: String,
)

@Serializable
private data class LectureTextUploadRequest(
    val subjectId: String,
    val chapterName: String? = null,
    val uploadType: String,
    val partSplitMethod: String,
    val text: String,
    val partSplitPlans: List<PartSplitPlanRequest>? = null,
)

@Serializable
private data class PartTextAddRequest(
    val partName: String,
    val uploadType: String,
    val text: String,
)

@Serializable
private data class PartSplitPlanRequest(
    val partNumber: Int,
    val intendedName: String? = null,
)

private fun PartSplitPlan.toRequest(): PartSplitPlanRequest = PartSplitPlanRequest(
    partNumber = partNumber,
    intendedName = intendedName,
)

private fun String.toMultipartFileName(): String =
    replace("\"", "")
        .replace("\r", "")
        .replace("\n", "")
        .ifBlank { "upload" }

private fun buildMultipartContent(
    textParts: List<Pair<String, String>>,
    files: List<PickedUploadFile>,
): ByteArrayContent {
    val boundary = "QuiketCmpBoundary${files.size}${files.sumOf { it.bytes.size }}${textParts.size}"
    val chunks = mutableListOf<ByteArray>()

    fun appendText(value: String) {
        chunks += value.encodeToByteArray()
    }

    textParts.forEach { (name, value) ->
        appendText("--$boundary\r\n")
        appendText("Content-Disposition: form-data; name=\"${name.toMultipartFieldName()}\"\r\n")
        appendText("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
        appendText(value)
        appendText("\r\n")
    }

    files.forEach { file ->
        appendText("--$boundary\r\n")
        appendText(
            "Content-Disposition: form-data; name=\"files\"; filename=\"${file.name.toMultipartFileName()}\"\r\n",
        )
        appendText("Content-Type: ${file.mimeType}\r\n\r\n")
        chunks += file.bytes
        appendText("\r\n")
    }

    appendText("--$boundary--\r\n")

    val totalSize = chunks.sumOf { it.size }
    val body = ByteArray(totalSize)
    var offset = 0
    chunks.forEach { chunk ->
        chunk.copyInto(body, destinationOffset = offset)
        offset += chunk.size
    }

    return ByteArrayContent(
        bytes = body,
        contentType = ContentType.MultiPart.FormData.withParameter("boundary", boundary),
    )
}

private fun String.toMultipartFieldName(): String =
    replace("\"", "")
        .replace("\r", "")
        .replace("\n", "")

@Serializable
private data class LectureUploadAcceptedResponse(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
) {
    fun toDomain(): LectureUploadAccepted = LectureUploadAccepted(
        lectureUploadId = lectureUploadId,
        subjectId = subjectId,
        chapterId = chapterId,
        status = status.toLectureUploadStatus(),
        estimatedSeconds = estimatedSeconds,
    )
}

@Serializable
private data class LectureUploadStatusResponse(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
    val chapterName: String? = null,
    val progressPct: Int? = null,
    val parts: List<PartSummaryResponse> = emptyList(),
    val failCode: String? = null,
    val failMessage: String? = null,
    val failReason: String? = null,
) {
    fun toDomain(): LectureUploadProgress = LectureUploadProgress(
        lectureUploadId = lectureUploadId,
        subjectId = subjectId,
        chapterId = chapterId,
        status = status.toLectureUploadStatus(),
        estimatedSeconds = estimatedSeconds,
        chapterName = chapterName,
        progressPct = progressPct,
        parts = parts.map { it.toDomain() },
        failCode = failCode,
        failMessage = failMessage,
        failReason = failReason,
    )
}

private inline fun <reified T> JsonElement.decodeOrNull(json: Json): T? =
    runCatching { json.decodeFromJsonElement<T>(this) }.getOrNull()

private fun String.toExamTypeLabel(): String? = when (lowercase()) {
    "certificate" -> "자격증"
    "university" -> "대학교"
    "middle_high" -> "중고등"
    "civil_service" -> "공무원"
    "language" -> "어학"
    "other",
    "other_exam",
    -> "기타 시험"
    else -> takeIf { it.isNotBlank() }
}

private fun String.toStudyFieldLabel(): String = when (lowercase()) {
    "humanities" -> "인문"
    "korean_history" -> "한국사"
    "world_history" -> "세계사"
    "society" -> "사회"
    "politics" -> "정치"
    "economics" -> "경제"
    "business" -> "경영"
    "science_tech" -> "과학기술"
    "it" -> "IT"
    "culture_art" -> "문화예술"
    "psychology" -> "심리학"
    "custom" -> "직접 입력"
    else -> this
}

private fun String.toStudyLevelLabel(): String = when (lowercase()) {
    "beginner" -> "입문자"
    "casual" -> "초급자"
    "regular" -> "중급자"
    "expert" -> "고급자"
    else -> this
}

private fun String.toUsagePurposeLabel(): String = when (lowercase()) {
    "work" -> "업무·실무 활용"
    "personal" -> "개인 기록·정리"
    "hobby" -> "취미·가벼운 학습"
    "memory" -> "기억·암기 보조"
    "other" -> "기타"
    else -> this
}

private fun SubjectExamDetail.displayLabel(): String? {
    val detail = when (examType.lowercase()) {
        "university" -> listOfNotNull(
            univMajorName?.takeIf { it.isNotBlank() },
            univCourseType?.toCourseTypeLabel(),
        ).joinNonBlank()
        "middle_high" -> listOfNotNull(
            mhGrade?.toMiddleHighCurriculumLabel(),
            mhSubjectType?.toMiddleHighSubjectTypeLabel(),
        ).joinNonBlank()
        "certificate" -> certificateName.orEmpty()
        "civil_service",
        "civil_servant",
        -> listOfNotNull(
            civilSeries?.toCivilSeriesLabel(),
            civilRank?.toCivilGradeLabel(),
        ).joinNonBlank()
        "language" -> listOfNotNull(
            langType?.toLanguageTypeLabel(),
            langExamName?.toLanguageExamLabel(),
        ).joinNonBlank()
        "other",
        "other_exam",
        -> otherExamName.orEmpty()
        else -> ""
    }

    return listOfNotNull(
        examType.toExamTypeLabel(),
        detail.takeIf { it.isNotBlank() },
    ).joinNonBlank().ifBlank { null }
}

private fun SubjectReviewDetail.displayLabel(): String? =
    listOf(
        field.toStudyFieldLabel(),
        studyLevel.toStudyLevelLabel(),
    ).joinNonBlank().ifBlank { null }

private fun SubjectOtherDetail.displayLabel(): String? =
    listOfNotNull(
        usagePurpose.toUsagePurposeLabel(),
        description?.takeIf { it.isNotBlank() },
    ).joinNonBlank().ifBlank { null }

private fun List<String>.joinNonBlank(): String =
    filter { it.isNotBlank() }.joinToString(" · ")

private fun String.toCourseTypeLabel(): String = when (lowercase()) {
    "major" -> "전공"
    "liberal",
    "liberal_arts",
    -> "교양"
    else -> this
}

private fun String.toMiddleHighCurriculumLabel(): String = when (lowercase()) {
    "elem" -> "초등"
    "middle" -> "중학"
    "high1" -> "고1"
    "high2" -> "고2"
    "high3" -> "고3"
    "csat" -> "수능"
    else -> this
}

private fun String.toMiddleHighSubjectTypeLabel(): String = when (lowercase()) {
    "korean" -> "국어"
    "math" -> "수학"
    "english" -> "영어"
    "science" -> "과학"
    "social" -> "사회"
    "history" -> "역사"
    "ethics" -> "윤리"
    "art" -> "예체능"
    "custom" -> "직접 입력"
    else -> this
}

private fun String.toLanguageTypeLabel(): String = when (lowercase()) {
    "english" -> "영어"
    "japanese" -> "일본어"
    "chinese" -> "중국어"
    else -> this
}

private fun String.toLanguageExamLabel(): String = when (lowercase()) {
    "toeic" -> "TOEIC"
    "toefl" -> "TOEFL"
    "ielts" -> "IELTS"
    "teps" -> "TEPS"
    "opic" -> "OPIc"
    "jlpt" -> "JLPT"
    "jpt" -> "JPT"
    "hsk" -> "HSK"
    "hskk" -> "HSKK"
    "custom" -> "직접 입력"
    else -> this
}

private fun String.toCivilGradeLabel(): String = when (lowercase()) {
    "grade9" -> "9급"
    "grade7" -> "7급"
    "grade5" -> "5급"
    "police" -> "경찰직"
    "fire" -> "소방직"
    "special" -> "기타 특수직"
    else -> this
}

private fun String.toCivilSeriesLabel(): String = when (lowercase()) {
    "admin" -> "행정"
    "tax" -> "세무"
    "custom_duty" -> "관세"
    "social_welfare" -> "사회복지"
    "education" -> "교육행정"
    "labor" -> "고용노동"
    "judiciary" -> "법원"
    "prosecution" -> "검찰"
    "police" -> "경찰"
    "fire" -> "소방"
    "military" -> "군무원"
    "other" -> "기타"
    else -> this
}

private fun String.toLectureUploadStatus(): LectureUploadStatus =
    LectureUploadStatus.entries.firstOrNull { it.wireValue == lowercase() }
        ?: LectureUploadStatus.Unknown

internal fun String.toSubjectPurposeLabel(): String = when (lowercase()) {
    "exam" -> "시험 준비"
    "review" -> "복습"
    "self_study" -> "자기주도 학습"
    "other" -> "기타"
    else -> this
}

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}
