package com.f1.quiket.composeapp.mypage.presentation

import com.f1.quiket.composeapp.mypage.domain.model.MyPageData
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile

sealed interface MyPageUiState {
    data object Idle : MyPageUiState
    data object Loading : MyPageUiState
    data class Success(val data: MyPageData) : MyPageUiState
    data class Error(val message: String) : MyPageUiState
}

fun MyPageUiState.withUpdatedProfile(profile: MyProfile): MyPageUiState =
    when (this) {
        is MyPageUiState.Success -> copy(data = data.copy(profile = profile))
        MyPageUiState.Idle,
        MyPageUiState.Loading,
        is MyPageUiState.Error,
        -> this
    }
