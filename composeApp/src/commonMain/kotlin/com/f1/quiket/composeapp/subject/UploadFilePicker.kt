package com.f1.quiket.composeapp.subject

import androidx.compose.runtime.Composable
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile

internal interface UploadFilePicker {
    fun pickPdf()
    fun pickImages(maxItems: Int)
}

@Composable
internal expect fun rememberUploadFilePicker(
    onFilesPicked: (List<PickedUploadFile>) -> Unit,
    onError: (String) -> Unit,
): UploadFilePicker
