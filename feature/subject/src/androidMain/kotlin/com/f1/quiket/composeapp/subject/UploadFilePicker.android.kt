package com.f1.quiket.composeapp.subject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun rememberUploadFilePicker(
    onFilesPicked: (List<PickedUploadFile>) -> Unit,
    onError: (String) -> Unit,
): UploadFilePicker {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnFilesPicked by rememberUpdatedState(onFilesPicked)
    val currentOnError by rememberUpdatedState(onError)
    var imageLimit by remember { mutableIntStateOf(MaxImageUploadCount) }

    fun cacheFiles(uris: List<Uri>, maxBytes: Long, createPreview: Boolean) {
        coroutineScope.launch {
            val results = withContext(Dispatchers.IO) {
                uris.map { uri ->
                    context.cachePickedUploadFile(
                        uri = uri,
                        maxBytes = maxBytes,
                        createPreview = createPreview,
                    )
                }
            }
            val files = results.mapNotNull(PickerReadResult::file)
            results.firstNotNullOfOrNull(PickerReadResult::error)?.let(currentOnError)
            if (files.isNotEmpty()) currentOnFilesPicked(files)
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        cacheFiles(
            uris = uris.take(1),
            maxBytes = MaxPdfUploadBytes,
            createPreview = false,
        )
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        cacheFiles(
            uris = uris.take(imageLimit),
            maxBytes = MaxImageUploadBytes,
            createPreview = true,
        )
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

private fun Context.cachePickedUploadFile(
    uri: Uri,
    maxBytes: Long,
    createPreview: Boolean,
): PickerReadResult {
    val metadata = readUploadMetadata(uri)
    if (metadata.sizeBytes != null && metadata.sizeBytes > maxBytes) {
        return PickerReadResult(error = maxBytes.toUploadLimitMessage(createPreview))
    }

    val cacheDirectory = File(cacheDir, UploadCacheDirectory).apply { mkdirs() }
    val cacheFile = File(
        cacheDirectory,
        "${UUID.randomUUID()}.${metadata.name.toSafeCacheExtension()}",
    )

    return try {
        val copiedBytes = contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().buffered().use { output ->
                val buffer = ByteArray(UploadCopyBufferBytes)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > maxBytes) throw UploadFileTooLargeException
                    output.write(buffer, 0, read)
                }
                total
            }
        } ?: error("Unable to open selected file")

        PickerReadResult(
            file = PickedUploadFile(
                name = metadata.name,
                mimeType = metadata.mimeType,
                sizeBytes = copiedBytes,
                previewBytes = if (createPreview) cacheFile.createImagePreview() else null,
                cachePath = cacheFile.absolutePath,
            ),
        )
    } catch (_: UploadFileTooLargeException) {
        cacheFile.delete()
        PickerReadResult(error = maxBytes.toUploadLimitMessage(createPreview))
    } catch (_: Throwable) {
        cacheFile.delete()
        PickerReadResult(error = "선택한 파일을 불러오지 못했어요.")
    }
}

private fun Context.readUploadMetadata(uri: Uri): UploadMetadata {
    var displayName: String? = null
    var sizeBytes: Long? = null
    contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0 && !cursor.isNull(nameIndex)) displayName = cursor.getString(nameIndex)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
        }
    }
    val name = displayName?.takeIf(String::isNotBlank) ?: "upload"
    return UploadMetadata(
        name = name,
        mimeType = contentResolver.getType(uri) ?: name.toUploadMimeType(),
        sizeBytes = sizeBytes,
    )
}

private fun File.createImagePreview(): ByteArray? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > UploadPreviewMaxPixels) {
        sampleSize *= 2
    }
    val bitmap = BitmapFactory.decodeFile(
        absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        },
    ) ?: return@runCatching null

    try {
        ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, UploadPreviewJpegQuality, output)
            output.toByteArray()
        }
    } finally {
        bitmap.recycle()
    }
}.getOrNull()

private fun String.toSafeCacheExtension(): String =
    substringAfterLast('.', missingDelimiterValue = "bin")
        .filter(Char::isLetterOrDigit)
        .take(10)
        .ifBlank { "bin" }

private fun Long.toUploadLimitMessage(isImage: Boolean): String =
    if (isImage) {
        "이미지는 장당 최대 15MB까지 업로드할 수 있어요."
    } else {
        "PDF는 최대 50MB까지 업로드할 수 있어요."
    }

private data class PickerReadResult(
    val file: PickedUploadFile? = null,
    val error: String? = null,
)

private data class UploadMetadata(
    val name: String,
    val mimeType: String,
    val sizeBytes: Long?,
)

private object UploadFileTooLargeException : RuntimeException()

private const val UploadCacheDirectory = "quiket_uploads"
private const val UploadCopyBufferBytes = 64 * 1024
private const val UploadPreviewMaxPixels = 1_024
private const val UploadPreviewJpegQuality = 82
