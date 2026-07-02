package com.f1.quiket.composeapp.subject

import androidx.compose.runtime.Composable

internal class PickedUploadFile(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    val sizeBytes: Long = bytes.size.toLong()
}

internal interface UploadFilePicker {
    fun pickPdf()
    fun pickImages(maxItems: Int)
}

@Composable
internal expect fun rememberUploadFilePicker(
    onFilesPicked: (List<PickedUploadFile>) -> Unit,
    onError: (String) -> Unit,
): UploadFilePicker
