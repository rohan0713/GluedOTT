package com.ott.gluedin.domain.entity

data class CategoryRail(
    val railId: String,
    val title: String,
    val priority: Int,
    val items: List<ContentItem>
)
