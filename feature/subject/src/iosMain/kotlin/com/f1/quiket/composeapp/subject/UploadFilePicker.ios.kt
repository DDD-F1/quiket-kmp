package com.f1.quiket.composeapp.subject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFURLRef
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithURL
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
internal actual fun rememberUploadFilePicker(
    onFilesPicked: (List<PickedUploadFile>) -> Unit,
    onError: (String) -> Unit,
): UploadFilePicker {
    val coroutineScope = rememberCoroutineScope()
    val delegate = remember(coroutineScope) { IosDocumentPickerDelegate(coroutineScope) }
    delegate.onFilesPicked = onFilesPicked
    delegate.onError = onError

    return remember(delegate) {
        object : UploadFilePicker {
            override fun pickPdf() {
                presentDocumentPicker(
                    documentTypes = listOf("com.adobe.pdf"),
                    allowsMultipleSelection = false,
                    delegate = delegate,
                    onError = delegate.onError,
                )
            }

            override fun pickImages(maxItems: Int) {
                delegate.maxItems = maxItems.coerceAtLeast(1)
                presentImagePicker(
                    maxItems = delegate.maxItems,
                    delegate = delegate,
                    onError = delegate.onError,
                )
            }
        }
    }
}

private class IosDocumentPickerDelegate(
    private val coroutineScope: CoroutineScope,
) : NSObject(), UIDocumentPickerDelegateProtocol, PHPickerViewControllerDelegateProtocol {
    var onFilesPicked: (List<PickedUploadFile>) -> Unit = {}
    var onError: (String) -> Unit = {}
    var maxItems: Int = MaxImageUploadCount

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val urls = didPickDocumentsAtURLs.take(1).mapNotNull { it as? NSURL }
        coroutineScope.launch {
            val results = withContext(Dispatchers.Default) {
                urls.map { url ->
                    url.cachePickedUploadFile(
                        name = url.lastPathComponent?.takeIf(String::isNotBlank) ?: "upload.pdf",
                        mimeType = "application/pdf",
                        maxBytes = MaxPdfUploadBytes,
                        createPreview = false,
                    )
                }
            }
            val files = results.mapNotNull(IosPickerReadResult::file)
            results.firstNotNullOfOrNull(IosPickerReadResult::error)?.let(onError)
            if (files.isNotEmpty()) onFilesPicked(files)
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = Unit

    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val providers = didFinishPicking
            .take(maxItems)
            .mapNotNull { it as? PHPickerResult }
            .map { result -> result.itemProvider }

        if (providers.isEmpty()) return

        val pickedResults = MutableList<IosPickerReadResult?>(providers.size) { null }
        var remaining = providers.size

        fun completeOne(index: Int, result: IosPickerReadResult) {
            dispatchOnMain {
                pickedResults[index] = result
                remaining -= 1
                if (remaining == 0) {
                    val results = pickedResults.mapNotNull { it }
                    val files = results.mapNotNull(IosPickerReadResult::file)
                    results.firstNotNullOfOrNull(IosPickerReadResult::error)?.let(onError)
                    if (files.isNotEmpty()) {
                        onFilesPicked(files)
                    } else if (results.none { it.error != null }) {
                        onError("선택한 이미지를 불러오지 못했어요.")
                    }
                }
            }
        }

        providers.forEachIndexed { index, provider ->
            val typeIdentifier = provider.registeredTypeIdentifiers
                .filterIsInstance<String>()
                .firstOrNull { it.startsWith("public.") || it.startsWith("com.") }
                ?: PublicImageTypeIdentifier
            val suggestedName = provider.suggestedName
                ?.takeIf(String::isNotBlank)
                ?.withExtensionIfMissing(typeIdentifier.toImageExtension())
                ?: "image_${index + 1}.${typeIdentifier.toImageExtension()}"

            provider.loadFileRepresentationForTypeIdentifier(typeIdentifier) { url: NSURL?, _: NSError? ->
                completeOne(
                    index = index,
                    result = url?.cachePickedUploadFile(
                        name = suggestedName,
                        mimeType = typeIdentifier.toImageMimeType(),
                        maxBytes = MaxImageUploadBytes,
                        createPreview = true,
                    ) ?: IosPickerReadResult(error = "선택한 이미지를 불러오지 못했어요."),
                )
            }
        }
    }
}

private fun presentDocumentPicker(
    documentTypes: List<String>,
    allowsMultipleSelection: Boolean,
    delegate: UIDocumentPickerDelegateProtocol,
    onError: (String) -> Unit,
) {
    val presenter = UIApplication.sharedApplication.activePresenter()
    if (presenter == null) {
        onError("파일 선택 화면을 열지 못했어요.")
        return
    }
    val picker = UIDocumentPickerViewController(
        documentTypes = documentTypes,
        inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
    )
    picker.allowsMultipleSelection = allowsMultipleSelection
    picker.delegate = delegate
    presenter.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun presentImagePicker(
    maxItems: Int,
    delegate: PHPickerViewControllerDelegateProtocol,
    onError: (String) -> Unit,
) {
    val presenter = UIApplication.sharedApplication.activePresenter()
    if (presenter == null) {
        onError("이미지 선택 화면을 열지 못했어요.")
        return
    }
    val configuration = PHPickerConfiguration()
    configuration.filter = PHPickerFilter.imagesFilter()
    configuration.selectionLimit = maxItems.convert()
    val picker = PHPickerViewController(configuration = configuration)
    picker.delegate = delegate
    presenter.presentViewController(picker, animated = true, completion = null)
}

private fun UIApplication.activePresenter(): UIViewController? =
    (
        keyWindow?.rootViewController
            ?: windows
                .filterIsInstance<UIWindow>()
                .firstOrNull { window -> window.rootViewController != null }
                ?.rootViewController
    )?.topMostViewController()

private fun UIViewController.topMostViewController(): UIViewController {
    val presented = presentedViewController
    return if (presented != null) presented.topMostViewController() else this
}

private fun dispatchOnMain(block: () -> Unit) {
    dispatch_async(dispatch_get_main_queue(), block)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.cachePickedUploadFile(
    name: String,
    mimeType: String,
    maxBytes: Long,
    createPreview: Boolean,
): IosPickerReadResult {
    val manager = NSFileManager.defaultManager
    val cacheDirectory = "${NSTemporaryDirectory().trimEnd('/')}/$UploadCacheDirectory"
    manager.createDirectoryAtPath(
        path = cacheDirectory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    val cachePath = "$cacheDirectory/${NSUUID().UUIDString}.${name.toSafeCacheExtension()}"
    val cacheUrl = NSURL.fileURLWithPath(cachePath)
    val accessing = startAccessingSecurityScopedResource()

    return try {
        if (!manager.copyItemAtURL(this, toURL = cacheUrl, error = null)) {
            return IosPickerReadResult(error = "선택한 파일을 불러오지 못했어요.")
        }
        val sizeBytes = SystemFileSystem.metadataOrNull(Path(cachePath))?.size
            ?: error("Unable to read cached file size")
        if (sizeBytes > maxBytes) {
            SystemFileSystem.delete(Path(cachePath), mustExist = false)
            return IosPickerReadResult(error = maxBytes.toUploadLimitMessage(createPreview))
        }

        IosPickerReadResult(
            file = PickedUploadFile(
                name = name,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                previewBytes = if (createPreview) cacheUrl.createImagePreview() else null,
                cachePath = cachePath,
            ),
        )
    } catch (_: Throwable) {
        runCatching { SystemFileSystem.delete(Path(cachePath), mustExist = false) }
        IosPickerReadResult(error = "선택한 파일을 불러오지 못했어요.")
    } finally {
        if (accessing) stopAccessingSecurityScopedResource()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("UNCHECKED_CAST")
private fun NSURL.createImagePreview(): ByteArray? = runCatching {
    val options = mapOf<Any?, Any?>(
        kCGImageSourceCreateThumbnailFromImageAlways to true,
        kCGImageSourceCreateThumbnailWithTransform to true,
        kCGImageSourceThumbnailMaxPixelSize to UploadPreviewMaxPixels,
    )
    val retainedUrl = CFBridgingRetain(this) as CFURLRef
    val retainedOptions = CFBridgingRetain(options) as CFDictionaryRef
    try {
        val source = CGImageSourceCreateWithURL(retainedUrl, null) ?: return@runCatching null
        try {
            val thumbnail = CGImageSourceCreateThumbnailAtIndex(source, 0u, retainedOptions)
                ?: return@runCatching null
            try {
                val image = UIImage.imageWithCGImage(thumbnail)
                UIImageJPEGRepresentation(image, UploadPreviewJpegQuality)?.toByteArray()
            } finally {
                CFRelease(thumbnail)
            }
        } finally {
            CFRelease(source)
        }
    } finally {
        CFRelease(retainedOptions)
        CFRelease(retainedUrl)
    }
}.getOrNull()

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    if (length == 0) return ByteArray(0)
    val result = ByteArray(length)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length.convert())
    }
    return result
}

private fun String.withExtensionIfMissing(extension: String): String =
    if (substringAfterLast('.', missingDelimiterValue = "").isBlank()) "$this.$extension" else this

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

private fun String.toImageMimeType(): String = when {
    contains("png", ignoreCase = true) -> "image/png"
    contains("heic", ignoreCase = true) -> "image/heic"
    contains("heif", ignoreCase = true) -> "image/heif"
    contains("webp", ignoreCase = true) -> "image/webp"
    else -> "image/jpeg"
}

private fun String.toImageExtension(): String = when {
    contains("png", ignoreCase = true) -> "png"
    contains("heic", ignoreCase = true) -> "heic"
    contains("heif", ignoreCase = true) -> "heif"
    contains("webp", ignoreCase = true) -> "webp"
    else -> "jpg"
}

private data class IosPickerReadResult(
    val file: PickedUploadFile? = null,
    val error: String? = null,
)

private const val PublicImageTypeIdentifier = "public.image"
private const val UploadCacheDirectory = "quiket_uploads"
private const val UploadPreviewMaxPixels = 1_024
private const val UploadPreviewJpegQuality = 0.82
