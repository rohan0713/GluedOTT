package com.ott.gluedin.data.repository

import android.util.Log
import com.ott.gluedin.domain.entity.PlayerAnalyticsEvent
import com.ott.gluedin.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor() : AnalyticsRepository {

    companion object {
        private const val TAG = "OTT_ANALYTICS"
    }

    private val _eventsFlow = MutableSharedFlow<PlayerAnalyticsEvent>(extraBufferCapacity = 64)

    override fun trackPlayerEvent(event: PlayerAnalyticsEvent) {
        val logMessage = when (event) {
            is PlayerAnalyticsEvent.PlayerReady ->
                "PLAYER_READY [Duration: ${event.durationMs}ms]"
            is PlayerAnalyticsEvent.PlaybackStarted ->
                "PLAYER_STARTED [Position: ${event.positionMs}ms]"
            is PlayerAnalyticsEvent.PlaybackPaused ->
                "PLAYER_PAUSED [Position: ${event.positionMs}ms]"
            is PlayerAnalyticsEvent.SeekPerformed ->
                "PLAYER_SEEKED [From: ${event.fromPositionMs}ms -> To: ${event.toPositionMs}ms]"
            is PlayerAnalyticsEvent.BufferingStarted ->
                "BUFFERING_STARTED [Position: ${event.positionMs}ms]"
            is PlayerAnalyticsEvent.BufferingEnded ->
                "BUFFERING_ENDED [Position: ${event.positionMs}ms]"
            is PlayerAnalyticsEvent.PlaybackCompleted ->
                "PLAYER_COMPLETED [Total Duration: ${event.totalDurationMs}ms]"
            is PlayerAnalyticsEvent.PlaybackError ->
                "PLAYER_ERROR [Code: ${event.errorCode}, Message: ${event.errorMessage}]"
        }

        try {
            Log.i(TAG, "Telemetry Event Logged: $logMessage")
        } catch (_: Exception) {
            println("[$TAG] $logMessage")
        }

        _eventsFlow.tryEmit(event)
    }
}
