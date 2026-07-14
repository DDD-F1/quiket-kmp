package com.f1.quiket.composeapp.subject.data.remote

import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteArray
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SubjectMultipartContentTest {
    @Test
    fun cachedFileCanBeReopenedAndReleaseDeletesIt() {
        val cached = cachedFile("reopenable-content")

        repeat(2) {
            val bytes = cached.file.openSource().use { source -> source.readByteArray() }
            assertTrue(bytes.contentEquals("reopenable-content".encodeToByteArray()))
        }

        cached.file.release()

        assertFalse(SystemFileSystem.exists(cached.path))
        assertFailsWith<IllegalStateException> { cached.file.openSource() }
    }

    @Test
    fun multipartContentStreamsCachedFileWithExpectedHeaders() = runTest {
        val cached = cachedFile("streamed-file-body", name = "lecture.pdf")
        try {
            val content = buildMultipartContent(
                textParts = listOf("subjectId" to "subject-1", "uploadType" to "pdf"),
                files = listOf(cached.file),
            )

            assertIs<OutgoingContent.WriteChannelContent>(content)
            val channel = ByteChannel(autoFlush = true)
            val bodyDeferred = async { channel.readRemaining().readByteArray() }
            content.writeTo(channel)
            val body = bodyDeferred.await().decodeToString()

            assertContains(body, "name=subjectId")
            assertContains(body, "subject-1")
            assertContains(body, "name=uploadType")
            assertContains(body, "filename=\"lecture.pdf\"")
            assertContains(body, "Content-Type: application/pdf")
            assertContains(body, "streamed-file-body")
        } finally {
            cached.file.release()
        }
    }
}

private data class CachedFile(
    val file: PickedUploadFile,
    val path: Path,
)

private fun cachedFile(content: String, name: String = "upload.bin"): CachedFile {
    val bytes = content.encodeToByteArray()
    val path = Path(SystemTemporaryDirectory, "quiket-multipart-${Random.nextLong()}")
    SystemFileSystem.sink(path).buffered().use { sink -> sink.write(bytes) }
    return CachedFile(
        file = PickedUploadFile(
            name = name,
            mimeType = "application/pdf",
            sizeBytes = bytes.size.toLong(),
            previewBytes = null,
            cachePath = path.toString(),
        ),
        path = path,
    )
}
