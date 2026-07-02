package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.auth.AuthClient
import com.f1.quiket.composeapp.auth.data.remote.AuthRemoteDataSource
import com.f1.quiket.composeapp.auth.data.remote.AuthRemoteDataSourceImpl
import com.f1.quiket.composeapp.auth.data.repository.AuthRepositoryImpl
import com.f1.quiket.composeapp.auth.data.repository.SessionRepositoryImpl
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import com.f1.quiket.composeapp.auth.domain.usecase.CheckEmailAvailabilityUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ClearAuthUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.auth.domain.usecase.CompleteKakaoNicknameUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.CompleteLoginUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ConfirmEmailVerificationUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ConfirmPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.GetCurrentUserUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.KakaoLoginUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LinkKakaoAccountUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LoginUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LogoutUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ReadSessionUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ResendEmailVerificationUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveHomeGuideCompletedUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.RequestPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveOnboardingCompletedUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SignupUseCase
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.history.HistoryClient
import com.f1.quiket.composeapp.history.data.remote.HistoryRemoteDataSource
import com.f1.quiket.composeapp.history.data.remote.HistoryRemoteDataSourceImpl
import com.f1.quiket.composeapp.history.data.repository.HistoryRepositoryImpl
import com.f1.quiket.composeapp.history.domain.repository.HistoryRepository
import com.f1.quiket.composeapp.history.domain.usecase.HistoryUseCases
import com.f1.quiket.composeapp.home.HomeClient
import com.f1.quiket.composeapp.home.data.remote.HomeRemoteDataSource
import com.f1.quiket.composeapp.home.data.remote.HomeRemoteDataSourceImpl
import com.f1.quiket.composeapp.home.data.repository.HomeRepositoryImpl
import com.f1.quiket.composeapp.home.domain.repository.HomeRepository
import com.f1.quiket.composeapp.home.domain.usecase.HomeUseCases
import com.f1.quiket.composeapp.home.presentation.ExamScheduleStateHolder
import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import com.f1.quiket.composeapp.mypage.MyPageClient
import com.f1.quiket.composeapp.mypage.data.remote.MyPageRemoteDataSource
import com.f1.quiket.composeapp.mypage.data.remote.MyPageRemoteDataSourceImpl
import com.f1.quiket.composeapp.mypage.data.repository.MyPageRepositoryImpl
import com.f1.quiket.composeapp.mypage.domain.repository.MyPageRepository
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.mypage.presentation.AccountSettingsStateHolder
import com.f1.quiket.composeapp.mypage.presentation.InquiryStateHolder
import com.f1.quiket.composeapp.mypage.presentation.NotificationSettingsStateHolder
import com.f1.quiket.composeapp.network.ApiConfig
import com.f1.quiket.composeapp.network.apiJson
import com.f1.quiket.composeapp.network.createHttpClient
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayRemoteDataSource
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayRemoteDataSourceImpl
import com.f1.quiket.composeapp.quiz.data.repository.QuizPlayRepositoryImpl
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository
import com.f1.quiket.composeapp.quiz.domain.usecase.BuildQuizCreateRequestUseCase
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.QuizPlayClient
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizPlayStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizStartStateHolder
import com.f1.quiket.composeapp.result.data.remote.QuizResultRemoteDataSource
import com.f1.quiket.composeapp.result.data.remote.QuizResultRemoteDataSourceImpl
import com.f1.quiket.composeapp.result.data.repository.QuizResultRepositoryImpl
import com.f1.quiket.composeapp.result.domain.repository.QuizResultRepository
import com.f1.quiket.composeapp.result.domain.usecase.QuizResultUseCases
import com.f1.quiket.composeapp.result.QuizResultClient
import com.f1.quiket.composeapp.result.presentation.QuizResultStateHolder
import com.f1.quiket.composeapp.review.ReviewClient
import com.f1.quiket.composeapp.subject.SubjectClient
import com.f1.quiket.composeapp.subject.data.remote.SubjectRemoteDataSource
import com.f1.quiket.composeapp.subject.data.remote.SubjectRemoteDataSourceImpl
import com.f1.quiket.composeapp.subject.data.repository.SubjectRepositoryImpl
import com.f1.quiket.composeapp.subject.domain.repository.SubjectRepository
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import com.f1.quiket.composeapp.subject.presentation.MaterialCheckStateHolder
import com.f1.quiket.composeapp.subject.presentation.PartDetailStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectCreateStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailStateHolder
import com.f1.quiket.composeapp.subject.presentation.SubjectDetailsEditStateHolder
import com.f1.quiket.composeapp.subject.presentation.TextLectureUploadStateHolder
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

private const val BaseUrlQualifier = "baseUrl"

fun initKoin(
    appDeclaration: KoinApplication.() -> Unit = {},
) {
    if (KoinPlatform.getKoinOrNull() != null) return

    startKoin {
        appDeclaration()
        modules(platformModule, commonModule)
    }
}

internal expect val platformModule: Module

internal val commonModule: Module = module {
    single<Json> { apiJson }
    single(named(BaseUrlQualifier)) { ApiConfig.ApiBaseUrl }
    single<HttpClient> { createHttpClient(json = get()) }

    single {
        AuthClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(client = get()) }
    single<AuthRepository> { AuthRepositoryImpl(remoteDataSource = get()) }
    single<SessionRepository> { SessionRepositoryImpl() }
    single { AuthenticatedCallRunner(sessionRepository = get(), authRepository = get()) }
    single { ReadSessionUseCase(sessionRepository = get()) }
    single { SaveOnboardingCompletedUseCase(sessionRepository = get()) }
    single { SaveHomeGuideCompletedUseCase(sessionRepository = get()) }
    single { ClearAuthUseCase(sessionRepository = get()) }
    single { CompleteLoginUseCase(sessionRepository = get()) }
    single { CheckEmailAvailabilityUseCase(authRepository = get()) }
    single { LoginUseCase(authRepository = get()) }
    single { KakaoLoginUseCase(authRepository = get()) }
    single { SignupUseCase(authRepository = get()) }
    single { ResendEmailVerificationUseCase(authRepository = get()) }
    single { ConfirmEmailVerificationUseCase(authRepository = get()) }
    single { CompleteKakaoNicknameUseCase(authRepository = get()) }
    single { LinkKakaoAccountUseCase(authRepository = get()) }
    single { RequestPasswordResetUseCase(authRepository = get()) }
    single { ConfirmPasswordResetUseCase(authRepository = get()) }
    single { GetCurrentUserUseCase(authenticatedCallRunner = get(), authRepository = get()) }
    single { LogoutUseCase(sessionRepository = get(), authRepository = get()) }
    single {
        AuthStateHolder(
            readSessionUseCase = get(),
            saveOnboardingCompletedUseCase = get(),
            clearAuthUseCase = get(),
            completeLoginUseCase = get(),
            checkEmailAvailabilityUseCase = get(),
            loginUseCase = get(),
            kakaoLoginUseCase = get(),
            signupUseCase = get(),
            resendEmailVerificationUseCase = get(),
            confirmEmailVerificationUseCase = get(),
            completeKakaoNicknameUseCase = get(),
            linkKakaoAccountUseCase = get(),
            requestPasswordResetUseCase = get(),
            confirmPasswordResetUseCase = get(),
            getCurrentUserUseCase = get(),
            logoutUseCase = get(),
        )
    }
    single {
        HomeClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<HomeRemoteDataSource> { HomeRemoteDataSourceImpl(client = get()) }
    single<HomeRepository> { HomeRepositoryImpl(remoteDataSource = get()) }
    single { HomeUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory { ExamScheduleStateHolder(homeUseCases = get()) }
    factory {
        MainStateHolder(
            homeUseCases = get(),
            historyUseCases = get(),
            myPageUseCases = get(),
            readSessionUseCase = get(),
            saveHomeGuideCompletedUseCase = get(),
        )
    }
    single {
        HistoryClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<HistoryRemoteDataSource> { HistoryRemoteDataSourceImpl(client = get()) }
    single<HistoryRepository> { HistoryRepositoryImpl(remoteDataSource = get()) }
    single { HistoryUseCases(authenticatedCallRunner = get(), repository = get()) }
    single {
        MyPageClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<MyPageRemoteDataSource> { MyPageRemoteDataSourceImpl(client = get()) }
    single<MyPageRepository> { MyPageRepositoryImpl(remoteDataSource = get()) }
    single { MyPageUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory { AccountSettingsStateHolder(myPageUseCases = get()) }
    factory { InquiryStateHolder(myPageUseCases = get()) }
    factory { NotificationSettingsStateHolder(myPageUseCases = get()) }
    single {
        QuizPlayClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<QuizPlayRemoteDataSource> { QuizPlayRemoteDataSourceImpl(client = get()) }
    single<QuizPlayRepository> { QuizPlayRepositoryImpl(remoteDataSource = get()) }
    single { BuildQuizCreateRequestUseCase() }
    single { QuizPlayUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory {
        QuizCreateStateHolder(
            subjectUseCases = get(),
            quizPlayUseCases = get(),
            buildQuizCreateRequest = get(),
        )
    }
    factory { QuizPlayStateHolder(quizPlayUseCases = get()) }
    factory { QuizStartStateHolder(quizPlayUseCases = get()) }
    single {
        QuizResultClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<QuizResultRemoteDataSource> { QuizResultRemoteDataSourceImpl(client = get()) }
    single<QuizResultRepository> { QuizResultRepositoryImpl(remoteDataSource = get()) }
    single { QuizResultUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory {
        QuizResultStateHolder(
            quizResultUseCases = get(),
            quizPlayUseCases = get(),
        )
    }
    single {
        ReviewClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single {
        SubjectClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(named(BaseUrlQualifier)),
        )
    }
    single<SubjectRemoteDataSource> { SubjectRemoteDataSourceImpl(client = get()) }
    single<SubjectRepository> { SubjectRepositoryImpl(remoteDataSource = get()) }
    single { SubjectUseCases(authenticatedCallRunner = get(), repository = get()) }
    factory { MaterialCheckStateHolder(subjectUseCases = get()) }
    factory { PartDetailStateHolder(subjectUseCases = get()) }
    factory { SubjectCreateStateHolder(subjectUseCases = get()) }
    factory { SubjectDetailStateHolder(subjectUseCases = get()) }
    factory { SubjectDetailsEditStateHolder(subjectUseCases = get()) }
    factory { TextLectureUploadStateHolder(subjectUseCases = get()) }
}
