package com.f1.quiket.feature.floating.presentation.navigation

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FloatingDestinationTest {

    @Test
    fun lectureUploadFileDestination_createRoute_encodesPathArguments() {
        val route = LectureUploadFileDestination.createRoute(
            lectureId = "subject/1",
            lectureTitle = "운영체제/입문?x=1#top",
            chapterCount = 7,
            category = "CS & OS",
        )

        val segments = route.split("/")

        assertThat(segments).hasSize(5)
        assertThat(Uri.decode(segments[1])).isEqualTo("subject/1")
        assertThat(Uri.decode(segments[2])).isEqualTo("운영체제/입문?x=1#top")
        assertThat(segments[3]).isEqualTo("7")
        assertThat(Uri.decode(segments[4])).isEqualTo("CS & OS")
    }

    @Test
    fun lectureViewDestination_createRoute_encodesPathArguments() {
        val route = LectureViewDestination.createRoute(
            subjectId = "subject/1",
            chapterId = "chapter?1",
            chapterNumber = 2,
            chapterName = "네트워크/보안#1",
            partCount = 3,
        )

        val segments = route.split("/")

        assertThat(segments).hasSize(6)
        assertThat(Uri.decode(segments[1])).isEqualTo("subject/1")
        assertThat(Uri.decode(segments[2])).isEqualTo("chapter?1")
        assertThat(segments[3]).isEqualTo("2")
        assertThat(Uri.decode(segments[4])).isEqualTo("네트워크/보안#1")
        assertThat(segments[5]).isEqualTo("3")
    }

    @Test
    fun materialCheckDestination_createRoute_encodesUploadId() {
        val route = MaterialCheckDestination.createRoute(
            lectureUploadId = "upload/1?draft",
            chapterNumber = 4,
        )

        val segments = route.split("/")

        assertThat(segments).hasSize(3)
        assertThat(Uri.decode(segments[1])).isEqualTo("upload/1?draft")
        assertThat(segments[2]).isEqualTo("4")
    }
}
