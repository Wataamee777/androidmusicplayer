package me.wataame.player.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import me.wataame.player.data.AppDatabase
import me.wataame.player.data.HistoryEntity
import me.wataame.player.domain.ABRepeatState
import me.wataame.player.domain.PlaybackMode
import me.wataame.player.playback.PlaybackService
import me.wataame.player.util.FileScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val scanner = FileScanner(appContext)
    private val historyDao = AppDatabase.get(appContext).historyDao()
    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java)),
    ).buildAsync()

    private var controller: MediaController? = null
    private var progressJob: Job? = null
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    val history = historyDao.observeAll()

    init {
        viewModelScope.launch {
            controller = controllerFuture.await().also { mediaController ->
                mediaController.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) = syncPlayerState()
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        syncPlayerState()
                        addHistory(mediaItem)
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) = syncPlayerState()
                })
                syncPlayerState()
                startProgressLoop()
            }
        }
    }

    fun playFromFile(path: String) = viewModelScope.launch {
        val queue = scanner.buildQueueFromSelectedTrack(path)
        controller?.run {
            setMediaItems(queue, 0, 0L)
            prepare()
            play()
        }
        _uiState.update { it.copy(queue = queue, currentFolder = File(path).parent.orEmpty()) }
    }

    fun loadFolder(path: String) = viewModelScope.launch {
        val tracks = runCatching { scanner.scanFolder(path) }.getOrDefault(emptyList())
        _uiState.update { it.copy(browserPath = path, folderTracks = tracks) }
    }

    fun playPause() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun next() { controller?.seekToNextMediaItem() }
    fun seekBy(deltaMs: Long) { controller?.let { it.seekTo((it.currentPosition + deltaMs).coerceAtLeast(0L)) } }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun removeQueueItem(index: Int) { controller?.removeMediaItem(index); syncPlayerState() }
    fun moveQueueItem(from: Int, to: Int) { controller?.moveMediaItem(from, to); syncPlayerState() }

    fun cyclePlaybackMode() {
        val mode = _uiState.value.playbackMode.next()
        controller?.repeatMode = when (mode) {
            PlaybackMode.NORMAL, PlaybackMode.FOLDER_RANDOM, PlaybackMode.ALL_RANDOM -> Player.REPEAT_MODE_OFF
            PlaybackMode.ONE_LOOP -> Player.REPEAT_MODE_ONE
            PlaybackMode.FOLDER_LOOP -> Player.REPEAT_MODE_ALL
        }
        controller?.shuffleModeEnabled = mode == PlaybackMode.FOLDER_RANDOM || mode == PlaybackMode.ALL_RANDOM
        _uiState.update { it.copy(playbackMode = mode) }
    }

    fun setPointA() = _uiState.update { it.copy(abRepeat = it.abRepeat.copy(pointA = controller?.currentPosition)) }
    fun setPointB() = _uiState.update { it.copy(abRepeat = it.abRepeat.copy(pointB = controller?.currentPosition)) }
    fun clearABRepeat() = _uiState.update { it.copy(abRepeat = ABRepeatState()) }
    fun toggleDarkTheme() = _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                syncPlayerState()
                val c = controller
                val ab = _uiState.value.abRepeat
                if (c != null && ab.isEnabled && c.currentPosition >= (ab.pointB ?: Long.MAX_VALUE)) {
                    c.seekTo(ab.pointA ?: 0L)
                }
                delay(500)
            }
        }
    }

    private fun syncPlayerState() {
        val c = controller ?: return
        _uiState.update {
            it.copy(
                isPlaying = c.isPlaying,
                currentTitle = c.mediaMetadata.title?.toString().orEmpty(),
                currentArtist = c.mediaMetadata.artist?.toString().orEmpty(),
                currentArtwork = c.mediaMetadata.artworkData,
                positionMs = c.currentPosition.coerceAtLeast(0L),
                durationMs = c.duration.takeIf { duration -> duration > 0 } ?: 0L,
                queue = List(c.mediaItemCount) { index -> c.getMediaItemAt(index) },
            )
        }
    }

    private fun addHistory(mediaItem: MediaItem?) = viewModelScope.launch {
        if (mediaItem == null) return@launch
        historyDao.insert(
            HistoryEntity(
                uri = mediaItem.localConfiguration?.uri.toString(),
                title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
                artist = mediaItem.mediaMetadata.artist?.toString().orEmpty(),
                playedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override fun onCleared() {
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }
}
