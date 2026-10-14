package com.ott.gluedin.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ott.gluedin.domain.entity.PlayerAnalyticsEvent
import com.ott.gluedin.domain.usecase.TrackPlayerEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val trackPlayerEventUseCase: TrackPlayerEventUseCase
) : ViewModel() {

    private val _lastEvent = MutableStateFlow<String?>("PLAYER_INITIALIZING")
    val lastEvent: StateFlow<String?> = _lastEvent.asStateFlow()

    fun onPlayerEvent(event: PlayerAnalyticsEvent) {
        // Business logic & telemetry delegation through clean use case
        trackPlayerEventUseCase(event)

        val formattedEventDisplay = when (event) {
            is PlayerAnalyticsEvent.PlayerReady -> "READY (${event.durationMs / 1000}s)"
            is PlayerAnalyticsEvent.PlaybackStarted -> "STARTED"
            is PlayerAnalyticsEvent.PlaybackPaused -> "PAUSED"
            is PlayerAnalyticsEvent.SeekPerformed -> "SEEKED (${event.toPositionMs / 1000}s)"
            is PlayerAnalyticsEvent.BufferingStarted -> "BUFFERING..."
            is PlayerAnalyticsEvent.BufferingEnded -> "BUFFERING DONE"
            is PlayerAnalyticsEvent.PlaybackCompleted -> "COMPLETED"
            is PlayerAnalyticsEvent.PlaybackError -> "ERROR: ${event.errorCode}"
        }
        _lastEvent.value = formattedEventDisplay
    }
}
