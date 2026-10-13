package com.ott.gluedin.domain.usecase

import com.ott.gluedin.domain.entity.PlayerAnalyticsEvent
import com.ott.gluedin.domain.repository.AnalyticsRepository

class TrackPlayerEventUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    operator fun invoke(event: PlayerAnalyticsEvent) {
        analyticsRepository.trackPlayerEvent(event)
    }
}
