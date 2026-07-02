package com.f1.quiket.feature.floating.data.di

import com.f1.quiket.feature.floating.data.remote.LectureUploadApi
import com.f1.quiket.feature.floating.data.remote.SubjectApi
import com.f1.quiket.feature.floating.data.repository.LectureUploadRepositoryImpl
import com.f1.quiket.feature.floating.data.repository.SubjectRepositoryImpl
import com.f1.quiket.feature.floating.domain.repository.LectureUploadRepository
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class FloatingRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSubjectRepository(
        repository: SubjectRepositoryImpl,
    ): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindLectureUploadRepository(
        repository: LectureUploadRepositoryImpl,
    ): LectureUploadRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FloatingNetworkModule {
    @Provides
    @Singleton
    fun provideSubjectApi(retrofit: Retrofit): SubjectApi =
        retrofit.create(SubjectApi::class.java)

    @Provides
    @Singleton
    fun provideLectureUploadApi(retrofit: Retrofit): LectureUploadApi =
        retrofit.create(LectureUploadApi::class.java)
}
