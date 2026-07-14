package com.f1.quiket.composeapp.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
internal sealed interface AppDestination : NavKey {
    @Serializable
    data object Loading : AppDestination

    @Serializable
    data object Onboarding : AppDestination

    @Serializable
    data object Login : AppDestination

    @Serializable
    data object EmailLogin : AppDestination

    @Serializable
    data object PasswordResetEmailVerification : AppDestination

    @Serializable
    data object PasswordResetNewPassword : AppDestination

    @Serializable
    data object KakaoNickname : AppDestination

    @Serializable
    data object KakaoAccountLink : AppDestination

    @Serializable
    data object AppleNickname : AppDestination

    @Serializable
    data object AppleAccountLink : AppDestination

    @Serializable
    data object SignUpCredentials : AppDestination

    @Serializable
    data object SignUpNickname : AppDestination

    @Serializable
    data object SignUpTerms : AppDestination

    @Serializable
    data object SignUpCodeVerification : AppDestination

    @Serializable
    data object Main : AppDestination
}

@Serializable
internal sealed interface MainDestination : NavKey {
    @Serializable
    data object Home : MainDestination

    @Serializable
    data object History : MainDestination

    @Serializable
    data object Review : MainDestination

    @Serializable
    data object MyPage : MainDestination

    @Serializable
    data object MyPageSettings : MainDestination

    @Serializable
    data object AccountSettings : MainDestination

    @Serializable
    data object NotificationSettings : MainDestination

    @Serializable
    data object Inquiry : MainDestination

    @Serializable
    data object Terms : MainDestination

    @Serializable
    data object PrivacyPolicy : MainDestination

    @Serializable
    data object ExamSchedule : MainDestination

    @Serializable
    data object QuizCreate : MainDestination

    @Serializable
    data object SubjectCreate : MainDestination

    @Serializable
    data object HomeUploadPicker : MainDestination

    @Serializable
    data class SubjectDetail(
        val subjectId: String,
        val subjectName: String,
        val uploadRequestId: String? = null,
    ) : MainDestination

    @Serializable
    data class QuizStart(
        val quizSessionId: String,
    ) : MainDestination

    @Serializable
    data class QuizPlay(
        val quizSessionId: String,
        val clientSessionId: String? = null,
        val playSessionId: String? = null,
        val playType: String = "first",
        val playMode: String = "all_at_once",
        val timerEnabled: Boolean = false,
        val timerScope: String? = null,
        val timerSeconds: Int? = null,
    ) : MainDestination

    @Serializable
    data class QuizResult(
        val resultId: String,
    ) : MainDestination
}

internal val AppNavigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppDestination.Loading::class, AppDestination.Loading.serializer())
            subclass(AppDestination.Onboarding::class, AppDestination.Onboarding.serializer())
            subclass(AppDestination.Login::class, AppDestination.Login.serializer())
            subclass(AppDestination.EmailLogin::class, AppDestination.EmailLogin.serializer())
            subclass(
                AppDestination.PasswordResetEmailVerification::class,
                AppDestination.PasswordResetEmailVerification.serializer(),
            )
            subclass(
                AppDestination.PasswordResetNewPassword::class,
                AppDestination.PasswordResetNewPassword.serializer(),
            )
            subclass(AppDestination.KakaoNickname::class, AppDestination.KakaoNickname.serializer())
            subclass(AppDestination.KakaoAccountLink::class, AppDestination.KakaoAccountLink.serializer())
            subclass(AppDestination.AppleNickname::class, AppDestination.AppleNickname.serializer())
            subclass(AppDestination.AppleAccountLink::class, AppDestination.AppleAccountLink.serializer())
            subclass(AppDestination.SignUpCredentials::class, AppDestination.SignUpCredentials.serializer())
            subclass(AppDestination.SignUpNickname::class, AppDestination.SignUpNickname.serializer())
            subclass(AppDestination.SignUpTerms::class, AppDestination.SignUpTerms.serializer())
            subclass(
                AppDestination.SignUpCodeVerification::class,
                AppDestination.SignUpCodeVerification.serializer(),
            )
            subclass(AppDestination.Main::class, AppDestination.Main.serializer())

            subclass(MainDestination.Home::class, MainDestination.Home.serializer())
            subclass(MainDestination.History::class, MainDestination.History.serializer())
            subclass(MainDestination.Review::class, MainDestination.Review.serializer())
            subclass(MainDestination.MyPage::class, MainDestination.MyPage.serializer())
            subclass(MainDestination.MyPageSettings::class, MainDestination.MyPageSettings.serializer())
            subclass(MainDestination.AccountSettings::class, MainDestination.AccountSettings.serializer())
            subclass(
                MainDestination.NotificationSettings::class,
                MainDestination.NotificationSettings.serializer(),
            )
            subclass(MainDestination.Inquiry::class, MainDestination.Inquiry.serializer())
            subclass(MainDestination.Terms::class, MainDestination.Terms.serializer())
            subclass(MainDestination.PrivacyPolicy::class, MainDestination.PrivacyPolicy.serializer())
            subclass(MainDestination.ExamSchedule::class, MainDestination.ExamSchedule.serializer())
            subclass(MainDestination.QuizCreate::class, MainDestination.QuizCreate.serializer())
            subclass(MainDestination.SubjectCreate::class, MainDestination.SubjectCreate.serializer())
            subclass(MainDestination.HomeUploadPicker::class, MainDestination.HomeUploadPicker.serializer())
            subclass(MainDestination.SubjectDetail::class, MainDestination.SubjectDetail.serializer())
            subclass(MainDestination.QuizStart::class, MainDestination.QuizStart.serializer())
            subclass(MainDestination.QuizPlay::class, MainDestination.QuizPlay.serializer())
            subclass(MainDestination.QuizResult::class, MainDestination.QuizResult.serializer())
        }
    }
}

internal fun MutableList<NavKey>.replaceAllWith(destination: NavKey) {
    clear()
    add(destination)
}

internal fun MutableList<NavKey>.replaceTopWith(destination: NavKey) {
    if (isEmpty()) {
        add(destination)
    } else {
        this[lastIndex] = destination
    }
}

internal fun MutableList<NavKey>.popOrReplaceWith(destination: NavKey) {
    if (size > 1) {
        removeAt(lastIndex)
    } else {
        replaceAllWith(destination)
    }
}

internal fun MutableList<NavKey>.popToOrReplaceWith(destination: NavKey) {
    val destinationIndex = indexOfLast { it == destination }
    if (destinationIndex < 0) {
        replaceAllWith(destination)
        return
    }
    while (lastIndex > destinationIndex) {
        removeAt(lastIndex)
    }
}
