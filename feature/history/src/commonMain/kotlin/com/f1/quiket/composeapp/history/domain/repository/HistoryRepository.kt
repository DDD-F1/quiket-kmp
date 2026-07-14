package com.f1.quiket.composeapp.history.domain.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.history.domain.model.RecentActivityPage

internal interface HistoryRepository {
    suspend fun getRecentActivities(
        session: SessionSnapshot,
        page: Int = 0,
        size: Int = 20,
    ): RecentActivityPage
}
