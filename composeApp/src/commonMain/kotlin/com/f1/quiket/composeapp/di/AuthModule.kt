package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.auth.AuthClient
import com.f1.quiket.composeapp.auth.data.remote.AuthRemoteDataSource
import com.f1.quiket.composeapp.auth.data.remote.AuthRemoteDataSourceImpl
import com.f1.quiket.composeapp.auth.data.repository.AuthRepositoryImpl
import com.f1.quiket.composeapp.auth.data.repository.SessionRepositoryImpl
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.auth.domain.usecase.CheckEmailAvailabilityUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ClearAuthUseCase
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
import com.f1.quiket.composeapp.auth.domain.usecase.RequestPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ResendEmailVerificationUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveHomeGuideCompletedUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveOnboardingCompletedUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SignupUseCase
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import org.koin.dsl.module

internal val authModule = module {
    single {
        AuthClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
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
}
