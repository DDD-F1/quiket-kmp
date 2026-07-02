package com.f1.quiket.composeapp.subject

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile

@Composable
internal actual fun rememberUploadFilePicker(
    onFilesPicked: (List<PickedUploadFile>) -> Unit,
    onError: (String) -> Unit,
): UploadFilePicker {
    val context = LocalContext.current
    val currentOnFilesPicked by rememberUpdatedState(onFilesPicked)
    val currentOnError by rememberUpdatedState(onError)
    var imageLimit by remember { mutableIntStateOf(MaxImageUploadCount) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        val files = uris.take(1).mapNotNull { uri -> context.readPickedUploadFile(uri) }
        if (files.isNotEmpty()) {
            currentOnFilesPicked(files)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        val files = uris.take(imageLimit).mapNotNull { uri -> context.readPickedUploadFile(uri) }
        if (files.isNotEmpty()) {
            currentOnFilesPicked(files)
        }
    }

    return remember(pdfPicker, imagePicker) {
        object : UploadFilePicker {
            override fun pickPdf() {
                pdfPicker.launch("application/pdf")
            }

            override fun pickImages(maxItems: Int) {
                imageLimit = maxItems.coerceAtLeast(1)
                imagePicker.launch("image/*")
            }
        }
    }
}

private fun Context.readPickedUploadFile(uri: Uri): PickedUploadFile? =
    runCatching {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@runCatching null
        val name = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
            ?.takeIf { it.isNotBlank() }
            ?: "upload"
        val mimeType = contentResolver.getType(uri) ?: name.toUploadMimeType()
        PickedUploadFile(
            name = name,
            mimeType = mimeType,
            bytes = bytes,
        )
    }.getOrNull()
