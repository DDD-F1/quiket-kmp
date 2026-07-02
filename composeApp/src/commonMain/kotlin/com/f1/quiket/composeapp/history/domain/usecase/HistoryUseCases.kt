package com.f1.quiket.composeapp.history.domain.usecase

import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.history.HistoryException
import com.f1.quiket.composeapp.history.RecentActivityPage
import com.f1.quiket.composeapp.history.domain.repository.HistoryRepository

internal class HistoryUseCases(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val repository: HistoryRepository,
) {
    suspend fun getRecentActivities(
        page: Int = 0,
        size: Int = 20,
    ): RecentActivityPage =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getRecentActivities(
                session = session,
                page = page,
                size = size,
            )
        }

    private fun isUnauthorized(error: Throwable): Boolean =
        error is HistoryException && error.isUnauthorized
}
