package com.f1.quiket.feature.mypage.presentation

import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.mypage.data.model.CharacterLevel
import com.f1.quiket.feature.mypage.data.model.UserProfile
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import com.f1.quiket.feature.mypage.presentation.MyPageIntent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repository: MyPageRepository,
) : MviViewModel<MyPageState, MyPageIntent, MyPageEffect>(initialState = MyPageState()) {

    init {
        onIntent(LoadProfile)
    }

    override fun handleIntent(intent: MyPageIntent) {
        when (intent) {
            is LoadProfile -> loadProfile()
            is NavigateToSettings -> viewModelScope.launch { sendEffect(MyPageEffect.GoToSettings) }
            is NavigateToAcornShop -> viewModelScope.launch { sendEffect(MyPageEffect.GoToAcornShop) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            val profileResult = repository.getMyProfile()
            val gamificationResult = repository.getMyGamification()

            if (profileResult is NetworkResult.Success && gamificationResult is NetworkResult.Success) {
                val profile = profileResult.data
                val gamification = gamificationResult.data
                val totalXp = gamification.xpTotal
                val level = CharacterLevel.from(totalXp)
                val unlockedItems = resolveUnlockedItems(level)

                updateState {
                    copy(
                        isLoading = false,
                        profile = UserProfile(nickname = profile.nickname),
                        characterLevel = level,
                        totalXp = totalXp,
                        acornCount = gamification.dotoriBalance,
                        unlockedRoomItems = unlockedItems,
                    )
                }
            } else {
                updateState { copy(isLoading = false) }
                viewModelScope.launch {
                    sendEffect(MyPageEffect.ShowSnackBar("프로필을 불러오지 못했어요."))
                }
            }
        }
    }

    private fun resolveUnlockedItems(level: CharacterLevel): Set<RoomItem> = buildSet {
        add(RoomItem.BASIC_ROOM)
        if (level.level >= 2) add(RoomItem.PLANT)
        if (level.level >= 3) add(RoomItem.RUG)
        if (level.level >= 4) add(RoomItem.SOFA)
        if (level.level >= 5) add(RoomItem.CLOCK)
    }
}