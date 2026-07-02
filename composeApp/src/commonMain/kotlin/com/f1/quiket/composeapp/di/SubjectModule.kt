package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.subject.data.remote.SubjectClient
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
import org.koin.dsl.module

internal val subjectModule = module {
    single {
        SubjectClient(
            httpClient = get(),
            json = get(),
            baseUrl = get(baseUrlQualifier),
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
