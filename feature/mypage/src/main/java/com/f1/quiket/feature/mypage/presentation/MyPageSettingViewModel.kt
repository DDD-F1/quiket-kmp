package com.f1.quiket.feature.mypage.presentation

import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageSettingViewModel @Inject constructor(
    private val repository: MyPageRepository,
) : MviViewModel<MyPageSettingState, MyPageSettingIntent, MyPageSettingEffect>(
    initialState = MyPageSettingState(),
) {
    init {
        loadProfile()
    }

    override fun handleIntent(intent: MyPageSettingIntent) {
        val effect = when (intent) {
            MyPageSettingIntent.NavigateBack -> MyPageSettingEffect.GoBack
            MyPageSettingIntent.NavigateToAccountSetting -> MyPageSettingEffect.GoToAccountSetting
            MyPageSettingIntent.NavigateToAlarmSetting -> MyPageSettingEffect.GoToAlarmSetting
            MyPageSettingIntent.NavigateToInquiry -> MyPageSettingEffect.GoToInquiry
            MyPageSettingIntent.NavigateToTerms -> MyPageSettingEffect.GoToTerms
            MyPageSettingIntent.NavigateToPrivacyPolicy -> MyPageSettingEffect.GoToPrivacyPolicy
            MyPageSettingIntent.NavigateToAppInfo -> MyPageSettingEffect.GoToAppInfo
            MyPageSettingIntent.Logout -> MyPageSettingEffect.GoToLogin
        }
        viewModelScope.launch { sendEffect(effect) }
    }

    private fun loadProfile() {
        launch {
            when (val result = repository.getMyProfile()) {
                is NetworkResult.Success -> updateState { copy(nickname = result.data.nickname) }
                is NetworkResult.Failure -> Unit
            }
        }
    }
}