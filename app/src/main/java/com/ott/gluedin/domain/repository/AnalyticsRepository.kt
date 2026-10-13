package com.ott.gluedin.domain.repository

import com.ott.gluedin.domain.entity.PlayerAnalyticsEvent

interface AnalyticsRepository {
    fun trackPlayerEvent(event: PlayerAnalyticsEvent)
}
