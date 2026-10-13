package com.ott.gluedin.data.dto

import com.google.gson.annotations.SerializedName

data class HomeFeedResponseDto(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("data")
    val data: HomeFeedDataDto? = null
)

data class HomeFeedDataDto(
    @SerializedName("hero_banner")
    val heroBanner: ContentItemDto? = null,

    @SerializedName("rails")
    val rails: List<CategoryRailDto>? = null
)
