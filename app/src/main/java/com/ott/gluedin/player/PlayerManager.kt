package com.ott.gluedin.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import com.ott.gluedin.domain.entity.PlayerAnalyticsEvent
import androidx.core.net.toUri

class PlayerManager {

    private var exoPlayer: ExoPlayer? = null
    private var eventBridge: PlayerEventBridge? = null

    var currentMediaUrl: String? = null
        private set

    var currentStreamType: String? = null
        private set

    fun initialize(
        context: Context,
        videoUrl: String,
        streamType: String = "MP4",
        startPositionMs: Long = 0L,
        autoPlay: Boolean = true,
        onEvent: (PlayerAnalyticsEvent) -> Unit
    ): ExoPlayer {
        Log.i(TAG, "initialize: url='$videoUrl', streamType='$streamType', startPos=${startPositionMs}ms, autoPlay=$autoPlay")
        release()

        val appContext = context.applicationContext

        val mediaCodecSelector = MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
            val decoders = MediaCodecUtil.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder
            )
            val nonGoldfish = decoders.filterNot { it.name.contains("goldfish", ignoreCase = true) }
            if (nonGoldfish.isNotEmpty()) nonGoldfish else decoders
        }

        val renderersFactory = DefaultRenderersFactory(appContext)
            .setMediaCodecSelector(mediaCodecSelector)
            .forceDisableMediaCodecAsynchronousQueueing()
            .setEnableDecoderFallback(true)

        val player = ExoPlayer.Builder(appContext, renderersFactory)
            .setSeekBackIncrementMs(3000L)
            .setSeekForwardIncrementMs(3000L)
            .build()
        val bridge = PlayerEventBridge { event ->
            val enrichedEvent = enrichEventWithPosition(event, player)
            onEvent(enrichedEvent)
        }
        player.addListener(bridge)

        val mediaItem = buildMediaItem(videoUrl, streamType)
        player.setMediaItem(mediaItem)
        player.prepare()

        if (startPositionMs > 0) {
            Log.d(TAG, "Seeking to initial position: ${startPositionMs}ms")
            player.seekTo(startPositionMs)
        }
        player.playWhenReady = autoPlay

        this.exoPlayer = player
        this.eventBridge = bridge
        this.currentMediaUrl = videoUrl
        this.currentStreamType = streamType

        Log.i(TAG, "Player successfully initialized and prepared")
        return player
    }

    private fun buildMediaItem(videoUrl: String, streamType: String): MediaItem {
        val uri = videoUrl.toUri()
        val builder = MediaItem.Builder().setUri(uri)

        when {
            streamType.equals("HLS", ignoreCase = true) || videoUrl.endsWith(".m3u8", ignoreCase = true) -> {
                Log.d(TAG, "Configured MIME type: APPLICATION_M3U8")
                builder.setMimeType(MimeTypes.APPLICATION_M3U8)
            }
            streamType.equals("DASH", ignoreCase = true) || videoUrl.endsWith(".mpd", ignoreCase = true) -> {
                Log.d(TAG, "Configured MIME type: APPLICATION_MPD")
                builder.setMimeType(MimeTypes.APPLICATION_MPD)
            }
            else -> {
                Log.d(TAG, "Configured MIME type: VIDEO_MP4")
                builder.setMimeType(MimeTypes.VIDEO_MP4)
            }
        }

        return builder.build()
    }

    private fun enrichEventWithPosition(
        event: PlayerAnalyticsEvent,
        player: ExoPlayer
    ): PlayerAnalyticsEvent {
        val currentPos = player.currentPosition.coerceAtLeast(0L)
        return when (event) {
            is PlayerAnalyticsEvent.PlaybackStarted ->
                PlayerAnalyticsEvent.PlaybackStarted(positionMs = currentPos)
            is PlayerAnalyticsEvent.PlaybackPaused ->
                PlayerAnalyticsEvent.PlaybackPaused(positionMs = currentPos)
            is PlayerAnalyticsEvent.BufferingStarted ->
                PlayerAnalyticsEvent.BufferingStarted(positionMs = currentPos)
            is PlayerAnalyticsEvent.BufferingEnded ->
                PlayerAnalyticsEvent.BufferingEnded(positionMs = currentPos)
            is PlayerAnalyticsEvent.PlaybackCompleted ->
                PlayerAnalyticsEvent.PlaybackCompleted(totalDurationMs = player.duration.coerceAtLeast(0L))
            else -> event
        }
    }

    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L

    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true

    fun release() {
        if (exoPlayer != null) {
            Log.i(TAG, "release: tearing down player and event listener")
            eventBridge?.let { bridge ->
                exoPlayer?.removeListener(bridge)
                bridge.reset()
            }
            eventBridge = null
            exoPlayer?.release()
            exoPlayer = null
            currentMediaUrl = null
            currentStreamType = null
            Log.d(TAG, "release: player successfully released")
        }
    }

    fun seekForward(offsetMs: Long = DEFAULT_SEEK_DELTA_MS) {
        exoPlayer?.let {
            val newPos = (it.currentPosition + offsetMs).coerceAtMost(it.duration.coerceAtLeast(0L))
            it.seekTo(newPos)
        }
    }

    fun seekBack(offsetMs: Long = DEFAULT_SEEK_DELTA_MS) {
        exoPlayer?.let {
            val newPos = (it.currentPosition - offsetMs).coerceAtLeast(0L)
            it.seekTo(newPos)
        }
    }

    companion object {
        private const val TAG = "PlayerManager"
        private const val DEFAULT_SEEK_DELTA_MS = 3_000L
    }
}