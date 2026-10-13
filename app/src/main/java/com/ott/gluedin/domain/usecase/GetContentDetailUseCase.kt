package com.ott.gluedin.domain.usecase

import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.repository.ContentRepository

class GetContentDetailUseCase(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(contentId: String): Result<ContentItem> {
        return contentRepository.getContentDetail(contentId)
    }
}
