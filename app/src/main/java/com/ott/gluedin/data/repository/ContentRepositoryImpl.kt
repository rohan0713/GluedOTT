package com.ott.gluedin.data.repository

import com.ott.gluedin.data.datasource.ContentRemoteDataSource
import com.ott.gluedin.data.mapper.ContentDtoMapper
import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.entity.HomeFeed
import com.ott.gluedin.domain.repository.ContentRepository

class ContentRepositoryImpl(
    private val remoteDataSource: ContentRemoteDataSource,
    private val mapper: ContentDtoMapper = ContentDtoMapper()
) : ContentRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        val dto = remoteDataSource.getHomeFeed()
        mapper.mapToDomain(dto)
    }

    override suspend fun getContentDetail(contentId: String): Result<ContentItem> = runCatching {
        val dto = remoteDataSource.getContentDetail(contentId)
        mapper.mapItemToDomain(dto)
    }
}
