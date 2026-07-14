package com.f1.quiket.composeapp.mypage.presentation

import com.f1.quiket.composeapp.util.runSuspendCatching

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.login.EmailFormatErrorMessage
import com.f1.quiket.composeapp.login.isValidEmail
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCategory
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCreate
import com.f1.quiket.composeapp.mypage.domain.model.MyPageException
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.network.toUserFacingMessage

internal class InquiryStateHolder(
    private val myPageUseCases: MyPageUseCases,
) {
    var state by mutableStateOf(InquiryUiState())
        private set

    fun selectCategory(category: FeedbackCategory) {
        state = state.copy(category = category, message = null)
    }

    fun updateBody(body: String) {
        state = state.copy(body = body.take(1000), message = null)
    }

    fun updateReplyEmail(email: String) {
        state = state.copy(replyEmail = email, message = null)
    }

    suspend fun submit(onSessionExpired: () -> Unit) {
        val currentState = state
        val trimmedReplyEmail = currentState.replyEmail.trim()
        when {
            currentState.body.isBlank() -> {
                state = currentState.copy(message = "문의 내용을 입력해주세요.")
                return
            }

            trimmedReplyEmail.isNotBlank() && !isValidEmail(trimmedReplyEmail) -> {
                state = currentState.copy(message = EmailFormatErrorMessage)
                return
            }

            currentState.isSubmitting -> return
        }

        state = currentState.copy(isSubmitting = true, message = null)
        runSuspendCatching {
            myPageUseCases.createFeedback(
                feedback = FeedbackCreate(
                    category = currentState.category,
                    body = currentState.body.trim(),
                    replyEmail = trimmedReplyEmail.takeIf { it.isNotBlank() },
                ),
            )
        }.onSuccess {
            state = InquiryUiState(message = "문의가 접수되었습니다.")
        }.onFailure { error ->
            if (error is MyPageException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = state.copy(
                    isSubmitting = false,
                    message = error.toUserFacingMessage("문의를 제출하지 못했습니다."),
                )
            }
        }
    }
}
