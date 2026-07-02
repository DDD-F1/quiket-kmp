package com.f1.quiket.composeapp.home.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.home.data.remote.HomeClient
import com.f1.quiket.composeapp.home.domain.model.HomeData
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary

internal interface HomeRemoteDataSource {
    suspend fun getHome(session: SessionSnapshot): HomeData
    suspend fun getSubjects(session: SessionSnapshot, page: Int = 0, size: Int = 50): List<SubjectSummary>
    suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    )
    suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String)
}

internal class HomeRemoteDataSourceImpl(
    private val client: HomeClient,
) : HomeRemoteDataSource {
    override suspend fun getHome(session: SessionSnapshot): HomeData =
        client.getHome(session)

    override suspend fun getSubjects(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): List<SubjectSummary> =
        client.getSubjects(session = session, page = page, size = size)

    override suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ) {
        client.upsertExamSchedule(
            session = session,
            subjectId = subjectId,
            examName = examName,
            examDate = examDate,
        )
    }

    override suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String) {
        client.deleteExamSchedule(session = session, subjectId = subjectId)
    }
}
