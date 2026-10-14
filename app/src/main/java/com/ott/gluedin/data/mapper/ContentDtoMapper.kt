package com.ott.gluedin.data.mapper

import com.ott.gluedin.data.dto.CategoryRailDto
import com.ott.gluedin.data.dto.ContentItemDto
import com.ott.gluedin.data.dto.HomeFeedResponseDto
import com.ott.gluedin.domain.entity.CategoryRail
import com.ott.gluedin.domain.entity.ContentItem
import com.ott.gluedin.domain.entity.HomeFeed

import javax.inject.Inject

class ContentDtoMapper @Inject constructor() {

    fun mapToDomain(dto: HomeFeedResponseDto): HomeFeed {
        val data = dto.data ?: throw IllegalStateException("API response data payload is null")
        val heroBannerDto = data.heroBanner ?: throw IllegalStateException("Hero banner is missing in response")
        val heroBanner = mapItemToDomain(heroBannerDto)
        val rails = data.rails?.mapNotNull { railDto ->
            mapRailToDomain(railDto)
        }.orEmpty().sortedBy { it.priority }

        return HomeFeed(
            heroBanner = heroBanner,
            rails = rails
        )
    }

    fun mapRailToDomain(dto: CategoryRailDto): CategoryRail? {
        val railId = dto.railId ?: return null
        val title = dto.railTitle.orEmpty()
        val priority = dto.priority ?: 99
        val items = dto.items?.map { mapItemToDomain(it) }.orEmpty()
        return CategoryRail(
            railId = railId,
            title = title,
            priority = priority,
            items = items
        )
    }

    fun mapItemToDomain(dto: ContentItemDto): ContentItem {
        return ContentItem(
            id = dto.id.orEmpty(),
            title = dto.title.orEmpty(),
            shortDescription = dto.shortDescription.orEmpty(),
            fullDescription = dto.fullDescription.orEmpty(),
            releaseYear = dto.releaseYear ?: 0,
            maturityRating = dto.maturityRating.orEmpty(),
            duration = dto.duration.orEmpty(),
            genres = dto.genres.orEmpty(),
            resolution = dto.resolution ?: "HD",
            thumbnailUrl = dto.thumbnailUrl.orEmpty(),
            bannerUrl = dto.bannerUrl.orEmpty(),
            videoUrl = dto.videoUrl.orEmpty(),
            streamType = dto.streamType ?: "MP4"
        )
    }
}
