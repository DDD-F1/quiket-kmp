package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.auth.AuthClient
import com.f1.quiket.composeapp.auth.data.remote.AuthRemoteDataSource
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import com.f1.quiket.composeapp.auth.domain.usecase.CheckEmailAvailabilityUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.auth.domain.usecase.CompleteKakaoNicknameUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ConfirmEmailVerificationUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ConfirmPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LinkKakaoAccountUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.RequestPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveHomeGuideCompletedUseCase
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.history.data.remote.HistoryClient
import com.f1.quiket.composeapp.history.data.remote.HistoryRemoteDataSource
import com.f1.quiket.composeapp.history.domain.repository.HistoryRepository
import com.f1.quiket.composeapp.history.domain.usecase.HistoryUseCases
import com.f1.quiket.composeapp.home.HomeClient
import com.f1.quiket.composeapp.home.data.remote.HomeRemoteDataSource
import com.f1.quiket.composeapp.home.domain.repository.HomeRepository
import com.f1.quiket.composeapp.home.domain.usecase.HomeUseCases
import com.f1.quiket.composeapp.home.presentation.ExamScheduleStateHolder
import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import com.f1.quiket.composeapp.mypage.MyPageClient
import com.f1.quiket.composeapp.mypage.data.remote.MyPageRemoteDataSource
import com.f1.quiket.composeapp.mypage.domain.repository.MyPageRepository
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.mypage.presentation.AccountSettingsStateHolder
import com.f1.quiket.composeapp.mypage.presentation.InquiryStateHolder
import com.f1.quiket.composeapp.mypage.presentation.NotificationSettingsStateHolder
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayClient
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayRemoteDataSource
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository
import com.f1.quiket.composeapp.quiz.domain.usecase.BuildQuizCreateRequestUseCase
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizPlayStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizStartStateHolder
import com.f1.quiket.composeapp.result.QuizResultClient
import com.f1.quiket.composeapp.result.data.remote.QuizResultRemoteDataSource
import com.f1.quiket.composeapp.result.domain.repository.QuizResultRepository
import com.f1.quiket.composeapp.result.domain.usecase.QuizResultUseCases
import com.f1.quiket.composeapp.result.presentation.QuizResultStateHolder
import com.f1.quiket.composeapp.review.ReviewClient
import com.f1.quiket.composeapp.subject.data.remote.SubjectClient
import com.f1.quiket.composeapp.subject.data.remote.SubjectRemoteDataSource
import com.f1.quiket.composeapp.subject.domain.repository.SubjectRepository
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import com.f1.quiket.composeapp.subject.presentation.MaterialCheckStateHolder
import com.f1.quiket.composeapp.subject.presentation.PartDetailStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectCreateStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailsEditStateHolder
import com.f1.quiket.composeapp.subject.presentation.TextLectureUploadStateHolder
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class KoinModuleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun commonModuleProvidesApiClients() {
        val app = startKoin {
            modules(commonModule)
        }

        with(app.koin) {
            assertNotNull(get<AuthClient>())
            assertNotNull(get<AuthRemoteDataSource>())
            assertNotNull(get<AuthRepository>())
            assertNotNull(get<SessionRepository>())
            assertNotNull(get<AuthenticatedCallRunner>())
            assertNotNull(get<CheckEmailAvailabilityUseCase>())
            assertNotNull(get<ConfirmEmailVerificationUseCase>())
            assertNotNull(get<CompleteKakaoNicknameUseCase>())
            assertNotNull(get<LinkKakaoAccountUseCase>())
            assertNotNull(get<RequestPasswordResetUseCase>())
            assertNotNull(get<ConfirmPasswordResetUseCase>())
            assertNotNull(get<SaveHomeGuideCompletedUseCase>())
            assertNotNull(get<AuthStateHolder>())
            assertNotNull(get<HomeClient>())
            assertNotNull(get<HomeRemoteDataSource>())
            assertNotNull(get<HomeRepository>())
            assertNotNull(get<HomeUseCases>())
            assertNotNull(get<ExamScheduleStateHolder>())
            assertNotNull(get<MainStateHolder>())
            assertNotNull(get<HistoryClient>())
            assertNotNull(get<HistoryRemoteDataSource>())
            assertNotNull(get<HistoryRepository>())
            assertNotNull(get<HistoryUseCases>())
            assertNotNull(get<MyPageClient>())
            assertNotNull(get<MyPageRemoteDataSource>())
            assertNotNull(get<MyPageRepository>())
            assertNotNull(get<MyPageUseCases>())
            assertNotNull(get<AccountSettingsStateHolder>())
            assertNotNull(get<InquiryStateHolder>())
            assertNotNull(get<NotificationSettingsStateHolder>())
            assertNotNull(get<QuizPlayClient>())
            assertNotNull(get<QuizPlayRemoteDataSource>())
            assertNotNull(get<QuizPlayRepository>())
            assertNotNull(get<BuildQuizCreateRequestUseCase>())
            assertNotNull(get<QuizPlayUseCases>())
            assertNotNull(get<QuizCreateStateHolder>())
            assertNotNull(get<QuizPlayStateHolder>())
            assertNotNull(get<QuizStartStateHolder>())
            assertNotNull(get<QuizResultClient>())
            assertNotNull(get<QuizResultRemoteDataSource>())
            assertNotNull(get<QuizResultRepository>())
            assertNotNull(get<QuizResultUseCases>())
            assertNotNull(get<QuizResultStateHolder>())
            assertNotNull(get<ReviewClient>())
            assertNotNull(get<SubjectClient>())
            assertNotNull(get<SubjectRemoteDataSource>())
            assertNotNull(get<SubjectRepository>())
            assertNotNull(get<SubjectUseCases>())
            assertNotNull(get<MaterialCheckStateHolder>())
            assertNotNull(get<PartDetailStateHolder>())
            assertNotNull(get<SubjectCreateStateHolder>())
            assertNotNull(get<SubjectDetailStateHolder>())
            assertNotNull(get<SubjectDetailsEditStateHolder>())
            assertNotNull(get<TextLectureUploadStateHolder>())
        }
    }
}
