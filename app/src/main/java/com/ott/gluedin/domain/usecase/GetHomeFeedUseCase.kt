package com.ott.gluedin.domain.usecase

import com.ott.gluedin.domain.entity.HomeFeed
import com.ott.gluedin.domain.repository.ContentRepository

class GetHomeFeedUseCase(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(): Result<HomeFeed> {
        return contentRepository.getHomeFeed()
    }
}
