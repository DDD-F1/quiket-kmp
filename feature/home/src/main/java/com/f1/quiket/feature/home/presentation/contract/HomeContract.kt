package com.f1.quiket.feature.home.presentation.contract

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState
import com.f1.quiket.feature.home.domain.model.HomeData

data class HomeState(
    val isLoading: Boolean = true,
    val showOnboarding: Boolean = false,
    val hasSubjects: Boolean = false,
    val homeData: HomeData? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface HomeIntent : UiIntent {
    data object LoadHomeData : HomeIntent
    data object OnboardingDoneClick : HomeIntent
}

sealed interface HomeSideEffect : UiEffect {
    data class ShowToast(val message: String) : HomeSideEffect
}
