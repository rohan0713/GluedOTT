package com.ott.gluedin.domain.entity

data class ContentItem(
    val id: String,
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val releaseYear: Int,
    val maturityRating: String,
    val duration: String,
    val genres: List<String>,
    val resolution: String,
    val thumbnailUrl: String,
    val bannerUrl: String,
    val videoUrl: String,
    val streamType: String
)
