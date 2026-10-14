package com.ott.gluedin.di

import com.ott.gluedin.data.datasource.ContentRemoteDataSource
import com.ott.gluedin.data.datasource.MockJsonContentDataSource
import com.ott.gluedin.data.repository.AnalyticsRepositoryImpl
import com.ott.gluedin.data.repository.ContentRepositoryImpl
import com.ott.gluedin.domain.repository.AnalyticsRepository
import com.ott.gluedin.domain.repository.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRemoteDataSource(
        mockJsonContentDataSource: MockJsonContentDataSource
    ): ContentRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepositoryImpl: AnalyticsRepositoryImpl
    ): AnalyticsRepository
}
