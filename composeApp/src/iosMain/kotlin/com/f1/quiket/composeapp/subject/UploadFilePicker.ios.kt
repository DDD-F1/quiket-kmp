package com.f1.quiket.composeapp.subject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
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
    val delegate = remember { IosDocumentPickerDelegate() }
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

private class IosDocumentPickerDelegate : NSObject(),
    UIDocumentPickerDelegateProtocol,
    PHPickerViewControllerDelegateProtocol {
    var onFilesPicked: (List<PickedUploadFile>) -> Unit = {}
    var onError: (String) -> Unit = {}
    var maxItems: Int = MaxImageUploadCount

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val files = didPickDocumentsAtURLs
            .take(maxItems)
            .mapNotNull { it as? NSURL }
            .mapNotNull { url -> url.readPickedUploadFile() }
        if (files.isNotEmpty()) {
            onFilesPicked(files)
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

        val pickedFiles = MutableList<PickedUploadFile?>(providers.size) { null }
        var remaining = providers.size
        var failedCount = 0

        fun completeOne(index: Int, file: PickedUploadFile?) {
            dispatchOnMain {
                pickedFiles[index] = file
                if (file == null) failedCount += 1
                remaining -= 1
                if (remaining == 0) {
                    val files = pickedFiles.mapNotNull { it }
                    if (files.isNotEmpty()) {
                        if (failedCount > 0) {
                            onError("일부 이미지를 불러오지 못했어요.")
                        }
                        onFilesPicked(files)
                    } else {
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
            provider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data: NSData?, error: NSError? ->
                completeOne(
                    index = index,
                    file = data?.let {
                        PickedUploadFile(
                            name = provider.suggestedName?.takeIf { name -> name.isNotBlank() }
                                ?: "image_${index + 1}.${typeIdentifier.toImageExtension()}",
                            mimeType = typeIdentifier.toImageMimeType(),
                            bytes = it.toByteArray(),
                        )
                    },
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
    return if (presented != null) {
        presented.topMostViewController()
    } else {
        this
    }
}

private fun dispatchOnMain(block: () -> Unit) {
    dispatch_async(dispatch_get_main_queue(), block)
}

private fun NSURL.readPickedUploadFile(): PickedUploadFile? {
    val accessing = startAccessingSecurityScopedResource()
    return try {
        val data = NSData.dataWithContentsOfURL(this) ?: return null
        val name = lastPathComponent?.takeIf { it.isNotBlank() } ?: "upload"
        PickedUploadFile(
            name = name,
            mimeType = name.toUploadMimeType(),
            bytes = data.toByteArray(),
        )
    } finally {
        if (accessing) {
            stopAccessingSecurityScopedResource()
        }
    }
}

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

private const val PublicImageTypeIdentifier = "public.image"
