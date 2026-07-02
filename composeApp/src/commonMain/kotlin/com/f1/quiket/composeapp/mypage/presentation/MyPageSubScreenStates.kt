package com.f1.quiket.composeapp.mypage.presentation

import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCategory
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings

internal data class AccountSettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val profile: MyProfile? = null,
    val message: String? = null,
) {
    val isLocalAccount: Boolean
        get() = profile?.providers.orEmpty().any { it.equals("local", ignoreCase = true) }
}

internal sealed interface AccountDialog {
    data class Nickname(val initialNickname: String) : AccountDialog
    data object EmailRequest : AccountDialog
    data class EmailConfirm(val email: String) : AccountDialog
    data object Password : AccountDialog
    data object DeleteAccount : AccountDialog
}

internal data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val settings: NotificationSettings? = null,
    val message: String? = null,
)

internal data class InquiryUiState(
    val isSubmitting: Boolean = false,
    val category: FeedbackCategory = FeedbackCategory.Inquiry,
    val body: String = "",
    val replyEmail: String = "",
    val message: String? = null,
)
