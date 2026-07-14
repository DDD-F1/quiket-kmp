package com.f1.quiket.composeapp.history.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.history.domain.model.RecentActivityPage
import com.f1.quiket.composeapp.history.data.remote.HistoryRemoteDataSource
import com.f1.quiket.composeapp.history.domain.repository.HistoryRepository

internal class HistoryRepositoryImpl(
    private val remoteDataSource: HistoryRemoteDataSource,
) : HistoryRepository {
    override suspend fun getRecentActivities(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): RecentActivityPage =
        remoteDataSource.getRecentActivities(
            session = session,
            page = page,
            size = size,
        )
}
