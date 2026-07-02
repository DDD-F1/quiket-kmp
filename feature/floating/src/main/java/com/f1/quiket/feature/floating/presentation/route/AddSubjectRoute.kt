package com.f1.quiket.feature.floating.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.f1.quiket.feature.floating.presentation.contract.AddSubjectContract
import com.f1.quiket.feature.floating.presentation.screen.addsubject.AddSubjectScreen
import com.f1.quiket.feature.floating.presentation.viewmodel.AddSubjectViewModel

@Composable
fun AddSubjectRoute(
    onFinish: () -> Unit = {},
    onDismiss: () -> Unit = {},
    viewModel: AddSubjectViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddSubjectContract.Effect.NavigateBack -> onDismiss()
                AddSubjectContract.Effect.NavigateToSuccess -> onFinish()
            }
        }
    }

    AddSubjectScreen(
        onFinish = { viewModel.handleIntent(AddSubjectContract.Intent.Finish) },
        onDismiss = { viewModel.handleIntent(AddSubjectContract.Intent.Skip) },
        viewModel = viewModel,
    )
}
