package com.f1.quiket.feature.home.presentation.viewmodel

import com.f1.quiket.feature.home.presentation.contract.*

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.database.datastore.OnboardingUseCase
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val onboardingUseCase: OnboardingUseCase,
    private val homeRepository: HomeRepository,
) : MviViewModel<HomeState, HomeIntent, HomeSideEffect>(
    initialState = HomeState(),
) {
    init {
        observeOnboarding()
        loadHomeData()
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadHomeData -> loadHomeData()
            HomeIntent.OnboardingDoneClick -> completeOnboarding()
        }
    }

    private fun observeOnboarding() {
        launch {
            onboardingUseCase.getFirstLaunch()
                .collect { isFirstLaunch ->
                    updateState {
                        copy(
                            showOnboarding = isFirstLaunch,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun loadHomeData() {
        launch {
            updateState {
                copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            when (val result = homeRepository.getHome()) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            hasSubjects = result.data.subjects.isNotEmpty(),
                            homeData = result.data,
                        )
                    }
                }

                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun completeOnboarding() {
        launch {
            onboardingUseCase.setDone()
            updateState { copy(showOnboarding = false) }
            sendEffect(HomeSideEffect.ShowToast("Quiket에 오신 것을 환영합니다 !"))
        }
    }
}
