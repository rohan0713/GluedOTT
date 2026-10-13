package com.ott.gluedin.domain.repository

import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.entity.HomeFeed

interface ContentRepository {
    suspend fun getHomeFeed(): Result<HomeFeed>
    suspend fun getContentDetail(contentId: String): Result<ContentItem>
}
