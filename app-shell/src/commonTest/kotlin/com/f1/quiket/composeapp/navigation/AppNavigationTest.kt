package com.f1.quiket.composeapp.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigationTest {
    @Test
    fun allDestinationsRoundTripThroughSavedState() {
        val destinations = listOf(
            AppDestination.Loading,
            AppDestination.Onboarding,
            AppDestination.Login,
            AppDestination.EmailLogin,
            AppDestination.PasswordResetEmailVerification,
            AppDestination.PasswordResetNewPassword,
            AppDestination.KakaoNickname,
            AppDestination.KakaoAccountLink,
            AppDestination.AppleNickname,
            AppDestination.AppleAccountLink,
            AppDestination.SignUpCredentials,
            AppDestination.SignUpNickname,
            AppDestination.SignUpTerms,
            AppDestination.SignUpCodeVerification,
            AppDestination.Main,
            MainDestination.Home,
            MainDestination.History,
            MainDestination.Review,
            MainDestination.MyPage,
            MainDestination.MyPageSettings,
            MainDestination.AccountSettings,
            MainDestination.NotificationSettings,
            MainDestination.Inquiry,
            MainDestination.Terms,
            MainDestination.PrivacyPolicy,
            MainDestination.ExamSchedule,
            MainDestination.QuizCreate,
            MainDestination.SubjectCreate,
            MainDestination.HomeUploadPicker,
            MainDestination.SubjectDetail(
                subjectId = "subject-1",
                subjectName = "운영체제",
                uploadRequestId = "upload-request-1",
            ),
            MainDestination.QuizStart(quizSessionId = "quiz-1"),
            MainDestination.QuizPlay(
                quizSessionId = "quiz-1",
                clientSessionId = "client-1",
                playSessionId = "play-1",
                playType = "retry_wrong",
                playMode = "one_by_one",
                timerEnabled = true,
                timerScope = "per_question",
                timerSeconds = 30,
            ),
            MainDestination.QuizResult(resultId = "result-1"),
        )
        val serializer = NavBackStackSerializer(PolymorphicSerializer(NavKey::class))
        val backStack = NavBackStack(*destinations.toTypedArray())

        val json = Json {
            serializersModule = AppNavigationSavedStateConfiguration.serializersModule
        }
        val encoded = json.encodeToString(serializer, backStack)
        val restored = json.decodeFromString(serializer, encoded)

        assertEquals(destinations, restored.toList())
    }

    @Test
    fun backStackOperationsKeepRequiredFallback() {
        val backStack = mutableListOf<NavKey>(AppDestination.Login)

        backStack.popOrReplaceWith(AppDestination.Onboarding)
        assertEquals(listOf<NavKey>(AppDestination.Onboarding), backStack)

        backStack += AppDestination.Login
        backStack += AppDestination.EmailLogin
        backStack.popToOrReplaceWith(AppDestination.Login)
        assertEquals(listOf<NavKey>(AppDestination.Onboarding, AppDestination.Login), backStack)

        backStack.replaceTopWith(AppDestination.Main)
        assertEquals(listOf<NavKey>(AppDestination.Onboarding, AppDestination.Main), backStack)

        backStack.replaceAllWith(AppDestination.Login)
        assertEquals(listOf<NavKey>(AppDestination.Login), backStack)
    }
}
