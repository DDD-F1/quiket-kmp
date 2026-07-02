package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState
import com.f1.quiket.feature.mypage.domain.model.FeedbackCategory

data class InquiryState(
    val isLoading: Boolean = false,
    val selectedCategory: FeedbackCategory = FeedbackCategory.Inquiry,
    val body: String = "",
    val replyEmail: String = "",
) : UiState

val InquiryState.isSubmitEnabled: Boolean
    get() = body.isNotBlank() && !isLoading

sealed interface InquiryIntent : UiIntent {
    data class SelectCategory(val category: FeedbackCategory) : InquiryIntent
    data class UpdateBody(val body: String) : InquiryIntent
    data class UpdateReplyEmail(val email: String) : InquiryIntent
    data object Submit : InquiryIntent
    data object NavigateBack : InquiryIntent
}

sealed interface InquiryEffect : UiEffect {
    data object GoBack : InquiryEffect
    data class ShowMessage(val message: String) : InquiryEffect
    data object SubmitSuccess : InquiryEffect
}