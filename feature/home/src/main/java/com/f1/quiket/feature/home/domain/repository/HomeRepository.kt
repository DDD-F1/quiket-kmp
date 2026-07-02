package com.f1.quiket.feature.home.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.HomeData
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.RecentActivity

interface HomeRepository {
    suspend fun getHome(): NetworkResult<HomeData>

    suspend fun getRecentActivities(
        page: Int = 0,
        size: Int = 20,
    ): NetworkResult<List<RecentActivity>>

    suspend fun getSubjects(
        page: Int = 0,
        size: Int = 50,
    ): NetworkResult<List<QuizSubjectSummary>>
}
