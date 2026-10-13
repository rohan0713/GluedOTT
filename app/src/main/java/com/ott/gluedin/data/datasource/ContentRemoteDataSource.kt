package com.ott.gluedin.data.datasource

import com.ott.gluedin.data.dto.ContentItemDto
import com.ott.gluedin.data.dto.HomeFeedResponseDto

interface ContentRemoteDataSource {
    suspend fun getHomeFeed(): HomeFeedResponseDto
    suspend fun getContentDetail(contentId: String): ContentItemDto
}
