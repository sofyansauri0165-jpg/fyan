package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val surahNumber: Int? = null,
    val ayahNumber: Int? = null,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val progress: Float = 0f,
    val errorMessage: String? = null
)

class QuranAudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun playAudio(
        url: String,
        title: String,
        subtitle: String,
        surahNumber: Int? = null,
        ayahNumber: Int? = null
    ) {
        stop()
        _playbackState.value = AudioPlaybackState(
            isLoading = true,
            title = title,
            subtitle = subtitle,
            surahNumber = surahNumber,
            ayahNumber = ayahNumber
        )

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    val dur = mp.duration.coerceAtLeast(0)
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        isLoading = false,
                        durationMs = dur
                    )
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        progress = 1f,
                        currentPositionMs = _playbackState.value.durationMs
                    )
                    stopProgressTracker()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("QuranAudioPlayer", "Audio error: what=$what, extra=$extra")
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "Gagal memutar audio. Periksa koneksi internet."
                    )
                    stopProgressTracker()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Exception initializing player", e)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isLoading = false,
                errorMessage = "Terjadi kesalahan saat memuat audio: ${e.message}"
            )
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                stopProgressTracker()
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun seekTo(progressFraction: Float) {
        mediaPlayer?.let {
            val targetMs = (progressFraction * it.duration).toInt()
            it.seekTo(targetMs)
            _playbackState.value = _playbackState.value.copy(
                currentPositionMs = targetMs,
                progress = progressFraction
            )
        }
    }

    fun stop() {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
        _playbackState.value = AudioPlaybackState()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val current = mp.currentPosition
                        val total = mp.duration
                        val fraction = if (total > 0) current.toFloat() / total.toFloat() else 0f
                        _playbackState.value = _playbackState.value.copy(
                            currentPositionMs = current,
                            durationMs = total,
                            progress = fraction.coerceIn(0f, 1f)
                        )
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
