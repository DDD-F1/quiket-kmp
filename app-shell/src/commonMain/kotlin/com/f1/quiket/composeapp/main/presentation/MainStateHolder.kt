package com.f1.quiket.composeapp.main.presentation

import com.f1.quiket.composeapp.util.runSuspendCatching

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.auth.domain.usecase.ReadSessionUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveHomeGuideCompletedUseCase
import com.f1.quiket.composeapp.history.domain.model.HistoryException
import com.f1.quiket.composeapp.history.domain.usecase.HistoryUseCases
import com.f1.quiket.composeapp.history.presentation.HistoryUiState
import com.f1.quiket.composeapp.home.domain.model.HomeException
import com.f1.quiket.composeapp.home.domain.usecase.HomeUseCases
import com.f1.quiket.composeapp.home.presentation.HomeUiState
import com.f1.quiket.composeapp.mypage.domain.model.MyPageException
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.mypage.presentation.MyPageUiState
import com.f1.quiket.composeapp.mypage.presentation.withUpdatedProfile
import com.f1.quiket.composeapp.network.toUserFacingMessage

internal class MainStateHolder(
    private val homeUseCases: HomeUseCases,
    private val historyUseCases: HistoryUseCases,
    private val myPageUseCases: MyPageUseCases,
    private val readSessionUseCase: ReadSessionUseCase,
    private val saveHomeGuideCompletedUseCase: SaveHomeGuideCompletedUseCase,
) {
    var homeState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    var historyState by mutableStateOf(HistoryUiState())
        private set

    var myPageState by mutableStateOf<MyPageUiState>(MyPageUiState.Idle)
        private set

    var starredSubjectIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var homeGuideCompleted by mutableStateOf(false)
        private set

    suspend fun loadHome(onSessionExpired: () -> Unit) {
        homeState = HomeUiState.Loading
        runSuspendCatching {
            homeUseCases.getHome()
        }.onSuccess { homeData ->
            homeState = HomeUiState.Success(homeData)
        }.onFailure { error ->
            if (error is HomeException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                homeState = HomeUiState.Error(error.toUserFacingMessage("홈 정보를 불러오지 못했습니다."))
            }
        }
    }

    suspend fun loadHistory(
        reset: Boolean,
        onSessionExpired: () -> Unit,
    ) {
        val currentState = historyState
        if (currentState.isLoading || currentState.isLoadingMore) return
        if (!reset && !currentState.hasNext) return

        val nextPage = if (reset) 0 else currentState.page + 1
        historyState = if (reset) {
            currentState.copy(
                isLoading = currentState.activities.isEmpty(),
                isLoadingMore = false,
                errorMessage = null,
            )
        } else {
            currentState.copy(
                isLoadingMore = true,
                errorMessage = null,
            )
        }

        runSuspendCatching {
            historyUseCases.getRecentActivities(
                page = nextPage,
                size = HistoryUiState.PageSize,
            )
        }.onSuccess { page ->
            historyState = historyState.copy(
                isLoading = false,
                isLoadingMore = false,
                hasLoaded = true,
                activities = if (reset) {
                    page.activities
                } else {
                    historyState.activities + page.activities
                },
                page = page.page,
                hasNext = page.hasNext,
                errorMessage = null,
            )
        }.onFailure { error ->
            if (error is HistoryException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                historyState = historyState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    hasLoaded = true,
                    errorMessage = error.toUserFacingMessage("기록을 불러오지 못했습니다."),
                )
            }
        }
    }

    suspend fun loadMyPage(onSessionExpired: () -> Unit) {
        if (myPageState is MyPageUiState.Loading) return

        myPageState = MyPageUiState.Loading
        runSuspendCatching {
            myPageUseCases.getMyPage()
        }.onSuccess { myPageData ->
            myPageState = MyPageUiState.Success(myPageData)
        }.onFailure { error ->
            if (error is MyPageException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                myPageState = MyPageUiState.Error(error.toUserFacingMessage("프로필을 불러오지 못했습니다."))
            }
        }
    }

    suspend fun loadHomeGuideCompleted() {
        homeGuideCompleted = runSuspendCatching {
            readSessionUseCase().homeGuideCompleted
        }.getOrDefault(false)
    }

    suspend fun dismissHomeGuide() {
        if (homeGuideCompleted) return
        homeGuideCompleted = true
        saveHomeGuideCompletedUseCase()
    }

    fun setInitialHomeGuideCompleted(completed: Boolean) {
        homeGuideCompleted = completed
    }

    fun toggleStarredSubject(subjectId: String) {
        starredSubjectIds = if (subjectId in starredSubjectIds) {
            starredSubjectIds - subjectId
        } else {
            starredSubjectIds + subjectId
        }
    }

    fun setSubjectStarred(subjectId: String, starred: Boolean) {
        starredSubjectIds = if (starred) {
            starredSubjectIds + subjectId
        } else {
            starredSubjectIds - subjectId
        }
    }

    fun removeStarredSubject(subjectId: String) {
        starredSubjectIds = starredSubjectIds - subjectId
    }

    fun updateMyPageProfile(profile: MyProfile) {
        myPageState = myPageState.withUpdatedProfile(profile)
    }
}
