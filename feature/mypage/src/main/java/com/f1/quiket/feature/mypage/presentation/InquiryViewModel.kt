package com.f1.quiket.feature.mypage.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.mypage.domain.model.FeedbackCreate
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InquiryViewModel @Inject constructor(
    private val repository: MyPageRepository,
) : MviViewModel<InquiryState, InquiryIntent, InquiryEffect>(
    initialState = InquiryState(),
) {
    override fun handleIntent(intent: InquiryIntent) {
        when (intent) {
            is InquiryIntent.SelectCategory -> updateState { copy(selectedCategory = intent.category) }
            is InquiryIntent.UpdateBody -> updateState { copy(body = intent.body) }
            is InquiryIntent.UpdateReplyEmail -> updateState { copy(replyEmail = intent.email) }
            is InquiryIntent.Submit -> submit()
            is InquiryIntent.NavigateBack -> launch { sendEffect(InquiryEffect.GoBack) }
        }
    }

    private fun submit() {
        if (!currentState.isSubmitEnabled) return
        launch {
            updateState { copy(isLoading = true) }
            val request = FeedbackCreate(
                category = currentState.selectedCategory,
                body = currentState.body,
                replyEmail = currentState.replyEmail.takeIf { it.isNotBlank() },
            )
            when (val result = repository.createFeedback(request)) {
                is NetworkResult.Success -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(InquiryEffect.SubmitSuccess)
                }
                is NetworkResult.Failure -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(InquiryEffect.ShowMessage(result.message))
                }
            }
        }
    }
}