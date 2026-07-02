package com.f1.quiket.feature.review.data.di

import com.f1.quiket.feature.review.data.remote.ReviewApi
import com.f1.quiket.feature.review.data.repository.ReviewRepositoryImpl
import com.f1.quiket.feature.review.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        repository: ReviewRepositoryImpl,
    ): ReviewRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ReviewNetworkModule {
    @Provides
    @Singleton
    fun provideReviewApi(retrofit: Retrofit): ReviewApi =
        retrofit.create(ReviewApi::class.java)
}
