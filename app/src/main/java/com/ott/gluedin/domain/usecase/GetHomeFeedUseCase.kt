package com.ott.gluedin.domain.usecase

import com.ott.gluedin.domain.entity.HomeFeed
import com.ott.gluedin.domain.repository.ContentRepository
import javax.inject.Inject

class GetHomeFeedUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(): Result<HomeFeed> {
        return contentRepository.getHomeFeed()
    }
}
