package com.ott.gluedin.data.dto

import com.google.gson.annotations.SerializedName

data class CategoryRailDto(
    @SerializedName("rail_id")
    val railId: String? = null,

    @SerializedName("rail_title")
    val railTitle: String? = null,

    @SerializedName("priority")
    val priority: Int? = null,

    @SerializedName("items")
    val items: List<ContentItemDto>? = null
)
