package com.f1.quiket.composeapp.subject

internal const val MaxImageUploadCount = 15
internal const val MaxPdfUploadBytes = 50L * 1024L * 1024L
internal const val MaxImageUploadBytes = 15L * 1024L * 1024L
internal const val MaxTextUploadLength = 30_000

internal fun Long.toUploadSizeLabel(): String = when {
    this >= 1_048_576L -> "${((this * 10) / 1_048_576L) / 10f}MB"
    this >= 1_024L -> "${this / 1_024L}KB"
    else -> "${this}B"
}

internal fun Int.toUploadCountLabel(): String =
    toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

internal fun String.toUploadMimeType(): String {
    val extension = substringAfterLast('.', missingDelimiterValue = "").lowercase()
    return when (extension) {
        "pdf" -> "application/pdf"
        "jpg",
        "jpeg",
        -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "heic" -> "image/heic"
        else -> "application/octet-stream"
    }
}
