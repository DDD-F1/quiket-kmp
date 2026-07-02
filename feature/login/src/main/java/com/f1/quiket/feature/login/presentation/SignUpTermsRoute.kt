package com.f1.quiket.feature.login.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpTermsRoute(
    onBackClick: () -> Unit,
    onSignupSubmitted: () -> Unit,
    viewModel: SignupTermsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var detailType by remember { mutableStateOf<SignUpTermsDetailType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SignupTermsEffect.NavigateToEmailVerification -> onSignupSubmitted()
                is SignupTermsEffect.ShowMessage -> context.showAuthToast(effect.message)
            }
        }
    }

    BackHandler(enabled = detailType != null) {
        detailType = null
    }

    when (val currentDetailType = detailType) {
        null -> SignUpTermsScreen(
            termsState = state.termsState,
            onBackClick = onBackClick,
            onAllTermsClick = { viewModel.onIntent(SignupTermsIntent.ToggleAllTerms) },
            onServiceTermsClick = { viewModel.onIntent(SignupTermsIntent.ToggleServiceTerms) },
            onServiceTermsDetailClick = { detailType = SignUpTermsDetailType.Service },
            onPrivacyTermsClick = { viewModel.onIntent(SignupTermsIntent.TogglePrivacyTerms) },
            onPrivacyTermsDetailClick = { detailType = SignUpTermsDetailType.Privacy },
            onMarketingTermsClick = { viewModel.onIntent(SignupTermsIntent.ToggleMarketingTerms) },
            onSubmitClick = { viewModel.onIntent(SignupTermsIntent.Submit) },
        )

        else -> SignUpTermsDetailScreen(
            content = currentDetailType.toContent(),
            onBackClick = { detailType = null },
            onAgreeClick = {
                viewModel.onIntent(currentDetailType.toAgreeIntent())
                detailType = null
            },
        )
    }
}

data class SignUpTermsState(
    val serviceTermsAgreed: Boolean = false,
    val privacyTermsAgreed: Boolean = false,
    val marketingTermsAgreed: Boolean = false,
) {
    val allAgreed: Boolean
        get() = serviceTermsAgreed && privacyTermsAgreed && marketingTermsAgreed

    val requiredAgreed: Boolean
        get() = serviceTermsAgreed && privacyTermsAgreed
}

private fun SignUpTermsDetailType.toAgreeIntent(): SignupTermsIntent =
    when (this) {
        SignUpTermsDetailType.Service -> SignupTermsIntent.AgreeServiceTerms
        SignUpTermsDetailType.Privacy -> SignupTermsIntent.AgreePrivacyTerms
    }
