package com.ott.gluedin.presentation.player

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ott.gluedin.databinding.ActivityPlayerBinding
import com.ott.gluedin.player.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val playerManager: PlayerManager by lazy {
        PlayerManager()
    }

    private val viewModel: PlayerViewModel by viewModels()

    private val videoUrl: String by lazy {
        intent.getStringExtra(EXTRA_VIDEO_URL).orEmpty()
    }

    private val streamType: String by lazy {
        intent.getStringExtra(EXTRA_STREAM_TYPE) ?: "MP4"
    }

    private val contentTitle: String by lazy {
        intent.getStringExtra(EXTRA_TITLE).orEmpty()
    }

    private var playbackPositionMs: Long = 0L
    private var playWhenReady: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableImmersiveFullscreen()
        setupViews()
        setupPlayerControls()
        restoreSavedState(savedInstanceState)
        observeViewModel()
    }

    private fun setupPlayerControls() {
        binding.playerView.setShowPreviousButton(false)
        binding.playerView.setShowNextButton(false)
        binding.playerView.setShowSubtitleButton(false)
        binding.playerView.setShowShuffleButton(false)
        binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_settings)?.visibility = View.GONE

        binding.playerView.setControllerVisibilityListener(androidx.media3.ui.PlayerView.ControllerVisibilityListener { visibility ->
            binding.clTopOverlay.animate().cancel()
            if (visibility == View.VISIBLE) {
                binding.clTopOverlay.visibility = View.VISIBLE
                binding.clTopOverlay.animate().alpha(1f).setDuration(200).start()
            } else {
                binding.clTopOverlay.animate().alpha(0f).setDuration(200).withEndAction {
                    binding.clTopOverlay.visibility = View.GONE
                }.start()
            }
        })

        // Tapping on empty area of top overlay toggles controller just like tapping video
        binding.clTopOverlay.setOnClickListener {
            if (binding.playerView.isControllerFullyVisible) {
                binding.playerView.hideController()
            } else {
                binding.playerView.showController()
            }
        }
    }

    private fun enableImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setupViews() {
        binding.tvPlayerTitle.text = contentTitle

        binding.ibPlayerBack.setOnClickListener {
            finish()
        }

        binding.btnPlayerRetry.setOnClickListener {
            binding.layoutPlayerError.isVisible = false
            initializePlayer()
        }
    }

    private fun restoreSavedState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            playbackPositionMs = it.getLong(KEY_PLAYBACK_POSITION, 0L)
            playWhenReady = it.getBoolean(KEY_PLAY_WHEN_READY, true)
        }
    }

    private fun initializePlayer() {
        if (videoUrl.isBlank()) {
            binding.layoutPlayerError.isVisible = true
            binding.tvPlayerErrorMessage.text = "Invalid or empty video stream URL"
            return
        }

        val exoPlayer = playerManager.initialize(
            context = this,
            videoUrl = videoUrl,
            streamType = streamType,
            startPositionMs = playbackPositionMs,
            autoPlay = playWhenReady
        ) { event ->
            // Flow: Player -> PlayerEventBridge -> PlayerViewModel -> TrackPlayerEventUseCase -> AnalyticsRepository
            viewModel.onPlayerEvent(event)
            if (event is com.ott.gluedin.domain.entity.PlayerAnalyticsEvent.PlaybackError) {
                runOnUiThread {
                    binding.layoutPlayerError.isVisible = true
                    binding.tvPlayerErrorMessage.text = "Playback Error: ${event.errorMessage}"
                }
            }
        }

        binding.playerView.player = exoPlayer
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lastEvent.collect { eventName ->
                    eventName?.let {
                        Log.d("PLAYER_ACTIVITY", "Event: $it")
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < 24 || binding.playerView.player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        playbackPositionMs = playerManager.getCurrentPosition()
        playWhenReady = playerManager.isPlaying()

        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24) {
            playbackPositionMs = playerManager.getCurrentPosition()
            playWhenReady = playerManager.isPlaying()
            releasePlayer()
        }
    }

    override fun finish() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        binding.playerView.player = null
        playerManager.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_PLAYBACK_POSITION, playerManager.getCurrentPosition())
        outState.putBoolean(KEY_PLAY_WHEN_READY, playerManager.isPlaying())
    }

    companion object {
        private const val EXTRA_VIDEO_URL = "extra_video_url"
        private const val EXTRA_STREAM_TYPE = "extra_stream_type"
        private const val EXTRA_TITLE = "extra_title"

        private const val KEY_PLAYBACK_POSITION = "key_playback_position"
        private const val KEY_PLAY_WHEN_READY = "key_play_when_ready"

        fun createIntent(
            context: Context,
            videoUrl: String,
            streamType: String,
            title: String
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, videoUrl)
                putExtra(EXTRA_STREAM_TYPE, streamType)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }
}
