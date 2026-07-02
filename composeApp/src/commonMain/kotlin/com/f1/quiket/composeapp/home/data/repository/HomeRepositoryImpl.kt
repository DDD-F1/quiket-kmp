package com.f1.quiket.composeapp.home.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.home.HomeData
import com.f1.quiket.composeapp.home.SubjectSummary
import com.f1.quiket.composeapp.home.data.remote.HomeRemoteDataSource
import com.f1.quiket.composeapp.home.domain.repository.HomeRepository

internal class HomeRepositoryImpl(
    private val remoteDataSource: HomeRemoteDataSource,
) : HomeRepository {
    override suspend fun getHome(session: SessionSnapshot): HomeData =
        remoteDataSource.getHome(session)

    override suspend fun getSubjects(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): List<SubjectSummary> =
        remoteDataSource.getSubjects(session = session, page = page, size = size)

    override suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ) {
        remoteDataSource.upsertExamSchedule(
            session = session,
            subjectId = subjectId,
            examName = examName,
            examDate = examDate,
        )
    }

    override suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String) {
        remoteDataSource.deleteExamSchedule(session = session, subjectId = subjectId)
    }
}
