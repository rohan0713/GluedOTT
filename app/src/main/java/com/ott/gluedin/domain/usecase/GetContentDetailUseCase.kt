package com.ott.gluedin.domain.usecase

import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.repository.ContentRepository
import javax.inject.Inject

class GetContentDetailUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(contentId: String): Result<ContentItem> {
        return contentRepository.getContentDetail(contentId)
    }
}
