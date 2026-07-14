package com.f1.quiket.composeapp.history.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.history.domain.model.RecentActivityPage

internal interface HistoryRemoteDataSource {
    suspend fun getRecentActivities(
        session: SessionSnapshot,
        page: Int = 0,
        size: Int = 20,
    ): RecentActivityPage
}

internal class HistoryRemoteDataSourceImpl(
    private val client: HistoryClient,
) : HistoryRemoteDataSource {
    override suspend fun getRecentActivities(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): RecentActivityPage =
        client.getRecentActivities(
            session = session,
            page = page,
            size = size,
        )
}
