package com.valentincolombat.upnews.ui.article

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.data.repository.InteractionRepository
import com.valentincolombat.upnews.data.repository.UserRepository
import com.valentincolombat.upnews.service.AudioPlaybackService
import com.valentincolombat.upnews.service.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ArticleDetailViewModel(
    application: Application,
    val article: Article
) : AndroidViewModel(application) {

    // MARK: - Audio

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var playerListener: Player.Listener? = null
    private var pendingPlay = false

    private val _isPlaying       = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeMs   = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _durationMs      = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed   = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLoadingAudio  = MutableStateFlow(false)
    val isLoadingAudio: StateFlow<Boolean> = _isLoadingAudio.asStateFlow()

    private val _audioLoadFailed = MutableStateFlow(false)
    val audioLoadFailed: StateFlow<Boolean> = _audioLoadFailed.asStateFlow()

    private val _audioLimitReached = MutableStateFlow(false)
    val audioLimitReached: StateFlow<Boolean> = _audioLimitReached.asStateFlow()

    // MARK: - Interactions

    private val _isLiked         = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _isFavorite      = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _hasClaimedXp   = MutableStateFlow(false)
    val hasClaimedXp: StateFlow<Boolean> = _hasClaimedXp.asStateFlow()

    private val _hasMarkedAsRead = MutableStateFlow(false)
    val hasMarkedAsRead: StateFlow<Boolean> = _hasMarkedAsRead.asStateFlow()

    private val _showPaywall     = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    enum class ClaimSource { BUTTON, AUDIO }

    private val _confettiEvent = MutableSharedFlow<ClaimSource>(extraBufferCapacity = 1)
    val confettiEvent: SharedFlow<ClaimSource> = _confettiEvent.asSharedFlow()

    // MARK: - Private

    private val userRepo        = UserRepository.shared
    private val interactionRepo = InteractionRepository.shared
    private val isPremium       get() = userRepo.isPremium
    val isPremiumFlow           get() = userRepo.subscriptionTier
    val freeLimit               = 15_000L
    private var progressJob: Job? = null

    // MARK: - Init

    init {
        connectToService()
        loadInteractions()
    }

    // MARK: - Service connection

    private fun connectToService() {
        val url = article.audioUrl ?: run { _audioLoadFailed.value = true; return }
        _isLoadingAudio.value = true

        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), AudioPlaybackService::class.java)
        )

        viewModelScope.launch {
            try {
                val future = MediaController.Builder(getApplication(), sessionToken).buildAsync()
                controllerFuture = future
                val controller = future.await()
                mediaController = controller
                setupPlayer(controller, url)
                if (pendingPlay) {
                    pendingPlay = false
                    controller.play()
                }
            } catch (e: Exception) {
                _audioLoadFailed.value = true
                _isLoadingAudio.value = false
            }
        }
    }

    private suspend fun setupPlayer(controller: MediaController, url: String) {
        val artwork = lockScreenArtwork(getApplication())
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(article.title)
                    .setArtist("UpNews")
                    .setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        _isLoadingAudio.value = false
                        _durationMs.value = controller.duration.coerceAtLeast(0L)
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        stopProgressPolling()
                        if (!_hasClaimedXp.value) claimXp(ClaimSource.AUDIO)
                    }
                    else -> {}
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) startProgressPolling() else stopProgressPolling()
            }

            override fun onPlayerError(error: PlaybackException) {
                _audioLoadFailed.value = true
                _isLoadingAudio.value = false
            }
        }
        playerListener = listener
        controller.addListener(listener)
    }

    // MARK: - Progress polling

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val controller = mediaController ?: break
                val posMs = controller.currentPosition
                _currentTimeMs.value = posMs

                if (_durationMs.value == 0L && controller.duration > 0)
                    _durationMs.value = controller.duration

                // Limitation free à 15s : stop() plutôt que pause() pour passer en STATE_IDLE
                // → MediaSessionService retire la notification lock screen automatiquement
                if (!isPremium && posMs >= freeLimit) {
                    _audioLimitReached.value = true
                    mediaController?.stop()
                    NotificationManager.postAudioLimitNotification(getApplication())
                    break
                }

                // XP automatique à 95%
                val dur = _durationMs.value
                if (dur > 0 && posMs.toDouble() / dur >= 0.95 && !_hasClaimedXp.value && !_hasMarkedAsRead.value)
                    claimXp(ClaimSource.AUDIO)

                delay(500)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    // MARK: - Playback controls

    fun play() {
        val controller = mediaController
        if (controller == null) { pendingPlay = true; return }
        controller.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentTimeMs.value = positionMs
    }

    fun seekBack()    { mediaController?.seekBack() }
    fun seekForward() { mediaController?.seekForward() }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaController?.setPlaybackSpeed(speed)
    }

    // MARK: - Paywall

    fun showPaywall()    { _showPaywall.value = true  }
    fun dismissPaywall() { _showPaywall.value = false }

    // MARK: - Interactions

    private fun loadInteractions() {
        viewModelScope.launch {
            runCatching {
                val interaction = interactionRepo.loadInteractions(article.id) ?: return@runCatching
                _isLiked.value         = interaction.isLiked
                _isFavorite.value      = interaction.isFavorite
                _hasClaimedXp.value    = interaction.hasClaimedXp
                _hasMarkedAsRead.value = interaction.isRead
            }
        }
    }

    fun toggleLike() {
        _isLiked.value = !_isLiked.value
        viewModelScope.launch {
            runCatching {
                interactionRepo.upsertInteraction(
                    article.id, _isLiked.value, _isFavorite.value, _hasMarkedAsRead.value, _hasClaimedXp.value
                )
            }
        }
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
        viewModelScope.launch {
            runCatching {
                interactionRepo.upsertInteraction(
                    article.id, _isLiked.value, _isFavorite.value, _hasMarkedAsRead.value, _hasClaimedXp.value
                )
            }
        }
    }

    fun markAsRead() {
        if (_hasMarkedAsRead.value) return
        _hasMarkedAsRead.value = true
        userRepo.incrementArticlesRead()
        viewModelScope.launch {
            runCatching {
                interactionRepo.upsertReadAt(article.id, _isLiked.value, _isFavorite.value, _hasClaimedXp.value)
            }
        }
    }

    fun claimXp(source: ClaimSource = ClaimSource.BUTTON) {
        if (_hasClaimedXp.value) return
        val xpAmount = if (isPremium) 40 else 20
        val companionUnlocked = userRepo.addXp(xpAmount)
        _hasClaimedXp.value = true
        if (!companionUnlocked) _confettiEvent.tryEmit(source)

        viewModelScope.launch {
            runCatching {
                userRepo.saveXpAndLevel()
                interactionRepo.upsertInteraction(
                    article.id, _isLiked.value, _isFavorite.value, _hasMarkedAsRead.value, _hasClaimedXp.value
                )
                interactionRepo.upsertReadAt(article.id, _isLiked.value, _isFavorite.value, _hasClaimedXp.value)
            }
        }
    }

    // MARK: - Cleanup

    override fun onCleared() {
        super.onCleared()
        stopProgressPolling()
        playerListener?.let { mediaController?.removeListener(it) }
        playerListener = null
        controllerFuture?.cancel(true)
        controllerFuture = null
        mediaController?.release()
        mediaController = null
    }

    // MARK: - Factory

    class Factory(
        private val application: Application,
        private val article: Article
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ArticleDetailViewModel(application, article) as T
    }

    // MARK: - Companion

    companion object {
        // Artwork calculé une seule fois en arrière-plan, partagé entre toutes les instances
        @Volatile private var cachedLockScreenArtwork: ByteArray? = null

        private suspend fun lockScreenArtwork(context: Context): ByteArray {
            cachedLockScreenArtwork?.let { return it }
            return withContext(Dispatchers.Default) {
                val size = 512
                val drawable = ContextCompat.getDrawable(context, R.drawable.fallback)
                    ?: return@withContext ByteArray(0)

                val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 } ?: size
                val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 } ?: size
                val scale = minOf(size.toFloat() / intrinsicW, size.toFloat() / intrinsicH)
                val drawW = (intrinsicW * scale).toInt()
                val drawH = (intrinsicH * scale).toInt()
                val left = (size - drawW) / 2
                val top  = (size - drawH) / 2

                val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)
                canvas.drawColor(android.graphics.Color.WHITE)
                drawable.setBounds(left, top, left + drawW, top + drawH)
                drawable.draw(canvas)

                val out = ByteArrayOutputStream()
                output.compress(Bitmap.CompressFormat.PNG, 90, out)
                output.recycle()
                out.toByteArray()
            }.also { cachedLockScreenArtwork = it }
        }
    }
}
