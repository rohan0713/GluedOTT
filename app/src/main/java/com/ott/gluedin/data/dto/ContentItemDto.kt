package com.ott.gluedin.data.dto

import com.google.gson.annotations.SerializedName

data class ContentItemDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("short_description")
    val shortDescription: String? = null,

    @SerializedName("full_description")
    val fullDescription: String? = null,

    @SerializedName("release_year")
    val releaseYear: Int? = null,

    @SerializedName("maturity_rating")
    val maturityRating: String? = null,

    @SerializedName("duration")
    val duration: String? = null,

    @SerializedName("genres")
    val genres: List<String>? = null,

    @SerializedName("resolution")
    val resolution: String? = null,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerializedName("banner_url")
    val bannerUrl: String? = null,

    @SerializedName("video_url")
    val videoUrl: String? = null,

    @SerializedName("stream_type")
    val streamType: String? = null
)
