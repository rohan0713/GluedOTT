package com.ott.gluedin.player

import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.ott.gluedin.domain.entity.PlayerAnalyticsEvent

class PlayerEventBridge(
    private val onEventDispatched: (PlayerAnalyticsEvent) -> Unit
) : Player.Listener {

    private var isBuffering = false
    private var hasDispatchedReady = false

    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateName = when (playbackState) {
            Player.STATE_IDLE -> "STATE_IDLE"
            Player.STATE_BUFFERING -> "STATE_BUFFERING"
            Player.STATE_READY -> "STATE_READY"
            Player.STATE_ENDED -> "STATE_ENDED"
            else -> "UNKNOWN($playbackState)"
        }
        Log.d(TAG, "onPlaybackStateChanged: state=$stateName, isBuffering=$isBuffering")

        when (playbackState) {
            Player.STATE_READY -> {
                if (isBuffering) {
                    isBuffering = false
                    Log.i(TAG, "Buffering finished -> dispatching BufferingEnded")
                    onEventDispatched(PlayerAnalyticsEvent.BufferingEnded(0L))
                }
            }
            Player.STATE_BUFFERING -> {
                if (!isBuffering) {
                    isBuffering = true
                    Log.i(TAG, "Buffering started -> dispatching BufferingStarted")
                    onEventDispatched(PlayerAnalyticsEvent.BufferingStarted(0L))
                }
            }
            Player.STATE_ENDED -> {
                Log.i(TAG, "Playback reached end -> dispatching PlaybackCompleted")
                onEventDispatched(PlayerAnalyticsEvent.PlaybackCompleted(0L))
            }
            Player.STATE_IDLE -> {
                Log.d(TAG, "Player is idle")
            }
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
            if (player.playbackState == Player.STATE_READY && !hasDispatchedReady) {
                hasDispatchedReady = true
                val duration = if (player.duration > 0) player.duration else 0L
                Log.i(TAG, "onEvents: Player ready with duration=${duration}ms -> dispatching PlayerReady")
                onEventDispatched(PlayerAnalyticsEvent.PlayerReady(duration))
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "onIsPlayingChanged: isPlaying=$isPlaying")
        if (isPlaying) {
            Log.i(TAG, "Dispatching PlaybackStarted")
            onEventDispatched(PlayerAnalyticsEvent.PlaybackStarted(0L))
        } else {
            Log.i(TAG, "Dispatching PlaybackPaused")
            onEventDispatched(PlayerAnalyticsEvent.PlaybackPaused(0L))
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        val reasonName = when (reason) {
            Player.DISCONTINUITY_REASON_SEEK -> "SEEK"
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> "AUTO_TRANSITION"
            Player.DISCONTINUITY_REASON_REMOVE -> "REMOVE"
            Player.DISCONTINUITY_REASON_INTERNAL -> "INTERNAL"
            else -> "OTHER($reason)"
        }
        Log.d(TAG, "onPositionDiscontinuity: reason=$reasonName, from=${oldPosition.positionMs}ms to=${newPosition.positionMs}ms")

        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            Log.i(TAG, "Seek detected: ${oldPosition.positionMs}ms -> ${newPosition.positionMs}ms -> dispatching SeekPerformed")
            onEventDispatched(
                PlayerAnalyticsEvent.SeekPerformed(
                    fromPositionMs = oldPosition.positionMs,
                    toPositionMs = newPosition.positionMs
                )
            )
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(
            TAG,
            "onPlayerError: errorCode=${error.errorCodeName} (${error.errorCode}), message=${error.message}",
            error
        )
        onEventDispatched(
            PlayerAnalyticsEvent.PlaybackError(
                errorCode = error.errorCodeName,
                errorMessage = error.message ?: "Unknown playback error"
            )
        )
    }

    fun reset() {
        Log.d(TAG, "reset: clearing buffering and ready states")
        isBuffering = false
        hasDispatchedReady = false
    }

    companion object {
        private const val TAG = "PlayerEventBridge"
    }
}
