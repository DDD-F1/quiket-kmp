package com.f1.quiket.feature.history.presentation

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.history.domain.model.QuizPlaySession
import com.f1.quiket.feature.history.domain.model.QuizResult
import com.f1.quiket.feature.history.domain.model.QuizResultSubmit
import com.f1.quiket.feature.history.domain.model.QuizRetry
import com.f1.quiket.feature.history.domain.model.RecentActivity
import com.f1.quiket.feature.history.domain.model.RecentActivityPage
import com.f1.quiket.feature.history.domain.model.RecentActivityType
import com.f1.quiket.feature.history.domain.repository.HistoryRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsRecentActivities() = runTest {
        val repository = FakeHistoryRepository()
        repository.recentActivityResults += NetworkResult.Success(
            recentActivityPage(
                page = 0,
                hasNext = true,
                activities = listOf(recentActivity("activity-1")),
            ),
        )

        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(repository.requestedPages).containsExactly(0)
        assertThat(state.activities.map { activity -> activity.activityId }).containsExactly("activity-1")
        assertThat(state.hasNext).isTrue()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun loadMore_appendsNextPage() = runTest {
        val repository = FakeHistoryRepository()
        repository.recentActivityResults += NetworkResult.Success(
            recentActivityPage(
                page = 0,
                hasNext = true,
                activities = listOf(recentActivity("activity-1")),
            ),
        )
        repository.recentActivityResults += NetworkResult.Success(
            recentActivityPage(
                page = 1,
                hasNext = false,
                activities = listOf(recentActivity("activity-2")),
            ),
        )
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(HistoryIntent.LoadMore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(repository.requestedPages).containsExactly(0, 1).inOrder()
        assertThat(state.activities.map { activity -> activity.activityId })
            .containsExactly("activity-1", "activity-2")
            .inOrder()
        assertThat(state.hasNext).isFalse()
    }

    @Test
    fun refresh_replacesExistingActivities() = runTest {
        val repository = FakeHistoryRepository()
        repository.recentActivityResults += NetworkResult.Success(
            recentActivityPage(
                page = 0,
                hasNext = true,
                activities = listOf(recentActivity("activity-1")),
            ),
        )
        repository.recentActivityResults += NetworkResult.Success(
            recentActivityPage(
                page = 0,
                hasNext = false,
                activities = listOf(recentActivity("activity-2")),
            ),
        )
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(HistoryIntent.Refresh)
        advanceUntilIdle()

        assertThat(viewModel.state.value.activities.map { activity -> activity.activityId })
            .containsExactly("activity-2")
    }

    @Test
    fun loadFailure_keepsErrorMessage() = runTest {
        val repository = FakeHistoryRepository()
        repository.recentActivityResults += NetworkResult.Failure(
            code = "SERVER_ERROR",
            message = "기록 조회 실패",
        )

        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.errorMessage).isEqualTo("기록 조회 실패")
        assertThat(state.isLoading).isFalse()
    }

    private class FakeHistoryRepository : HistoryRepository {
        val recentActivityResults = ArrayDeque<NetworkResult<RecentActivityPage>>()
        val requestedPages = mutableListOf<Int>()

        override suspend fun getRecentActivities(
            page: Int,
            size: Int,
        ): NetworkResult<RecentActivityPage> {
            requestedPages += page
            assertThat(size).isEqualTo(20)
            return recentActivityResults.removeFirstOrNull()
                ?: NetworkResult.Failure(code = "TEST", message = "not configured")
        }

        override suspend fun submitQuizResult(request: QuizResultSubmit): NetworkResult<QuizResult> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun getQuizResult(resultId: String): NetworkResult<QuizResult> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun retryAllQuestions(
            resultId: String,
            request: QuizRetry,
        ): NetworkResult<QuizPlaySession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun retryWrongQuestions(
            resultId: String,
            request: QuizRetry,
        ): NetworkResult<QuizPlaySession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
    }
}

private fun recentActivityPage(
    page: Int,
    hasNext: Boolean,
    activities: List<RecentActivity>,
): RecentActivityPage = RecentActivityPage(
    activities = activities,
    page = page,
    size = 20,
    totalElements = activities.size.toLong(),
    totalPages = if (hasNext) page + 2 else page + 1,
    hasNext = hasNext,
)

private fun recentActivity(activityId: String): RecentActivity = RecentActivity(
    activityId = activityId,
    activityType = RecentActivityType.QuizCompleted,
    quizSessionId = "quiz-$activityId",
    playSessionId = "play-$activityId",
    resultId = "result-$activityId",
    title = "SQLD 객관식",
    subjectId = "subject-1",
    subjectName = "SQLD",
    status = "completed",
    progressPct = null,
    scoreText = "8/10",
    createdAt = "2026-05-28T20:00:00+09:00",
)
