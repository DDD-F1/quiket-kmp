package com.f1.quiket.feature.home.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.home.data.mapper.toDomain
import com.f1.quiket.feature.home.data.remote.HomeApi
import com.f1.quiket.feature.home.domain.model.HomeData
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.RecentActivity
import com.f1.quiket.feature.home.domain.repository.HomeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : HomeRepository {
    override suspend fun getHome(): NetworkResult<HomeData> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getHome() },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun getRecentActivities(
        page: Int,
        size: Int,
    ): NetworkResult<List<RecentActivity>> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getRecentActivities(page = page, size = size) },
            mapper = { response -> response.content.map { activity -> activity.toDomain() } },
        )
    }

    override suspend fun getSubjects(
        page: Int,
        size: Int,
    ): NetworkResult<List<QuizSubjectSummary>> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getSubjects(page = page, size = size) },
            mapper = { response -> response.content.map { subject -> subject.toDomain() } },
        )
    }
}
