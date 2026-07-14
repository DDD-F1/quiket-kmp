package com.f1.quiket.composeapp.home.domain.usecase

import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.home.domain.model.HomeData
import com.f1.quiket.composeapp.home.domain.model.HomeException
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import com.f1.quiket.composeapp.home.domain.repository.HomeRepository

class HomeUseCases internal constructor(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val repository: HomeRepository,
) {
    suspend fun getHome(): HomeData =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getHome(session)
        }

    suspend fun getSubjects(page: Int = 0, size: Int = 50): List<SubjectSummary> =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getSubjects(session = session, page = page, size = size)
        }

    suspend fun upsertExamSchedule(
        subjectId: String,
        examName: String?,
        examDate: String,
    ) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.upsertExamSchedule(
                session = session,
                subjectId = subjectId,
                examName = examName,
                examDate = examDate,
            )
        }
    }

    suspend fun deleteExamSchedule(subjectId: String) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.deleteExamSchedule(session = session, subjectId = subjectId)
        }
    }

    private fun isUnauthorized(error: Throwable): Boolean =
        error is HomeException && error.isUnauthorized
}
