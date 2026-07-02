package com.f1.quiket.composeapp.home.domain.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.home.HomeData
import com.f1.quiket.composeapp.home.SubjectSummary

internal interface HomeRepository {
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
