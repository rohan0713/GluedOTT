package com.ott.gluedin.domain.entity

sealed interface PlayerAnalyticsEvent {
    val eventName: String

    data class PlayerReady(val durationMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "PLAYER_READY"
    }

    data class PlaybackStarted(val positionMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "PLAYER_STARTED"
    }

    data class PlaybackPaused(val positionMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "PLAYER_PAUSED"
    }

    data class SeekPerformed(val fromPositionMs: Long, val toPositionMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "PLAYER_SEEKED"
    }

    data class BufferingStarted(val positionMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "BUFFERING_STARTED"
    }

    data class BufferingEnded(val positionMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "BUFFERING_ENDED"
    }

    data class PlaybackCompleted(val totalDurationMs: Long) : PlayerAnalyticsEvent {
        override val eventName: String = "PLAYER_COMPLETED"
    }

    data class PlaybackError(val errorCode: String, val errorMessage: String) : PlayerAnalyticsEvent {
        override val eventName: String = "PLAYER_ERROR"
    }
}
