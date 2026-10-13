package com.ott.gluedin.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.ott.gluedin.R
import com.ott.gluedin.data.dto.ContentItemDto
import com.ott.gluedin.data.dto.HomeFeedResponseDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MockJsonContentDataSource(
    private val context: Context,
    private val gson: Gson = Gson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRemoteDataSource {

    private val feedResponse: HomeFeedResponseDto by lazy {
        context.resources.openRawResource(R.raw.ott_content).bufferedReader().use { reader ->
            gson.fromJson(reader, HomeFeedResponseDto::class.java)
        }
    }

    override suspend fun getHomeFeed(): HomeFeedResponseDto = withContext(ioDispatcher) {
        feedResponse
    }

    override suspend fun getContentDetail(contentId: String): ContentItemDto = withContext(ioDispatcher) {
        val data = feedResponse.data
        if (data?.heroBanner?.id == contentId) {
            return@withContext data.heroBanner
        }

        data?.rails.orEmpty()
            .flatMap { it.items.orEmpty() }
            .firstOrNull { it.id == contentId }
            ?: throw NoSuchElementException("Content with id $contentId not found")
    }
}
