package me.wataame.player.viewmodel

import androidx.media3.common.MediaItem
import me.wataame.player.domain.ABRepeatState
import me.wataame.player.domain.AudioTrack
import me.wataame.player.domain.PlaybackMode

data class PlayerUiState(
    val isDarkTheme: Boolean = true,
    val isPlaying: Boolean = false,
    val currentTitle: String = "No track",
    val currentArtist: String = "",
    val currentArtwork: ByteArray? = null,
    val audioSessionId: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackMode: PlaybackMode = PlaybackMode.NORMAL,
    val abRepeat: ABRepeatState = ABRepeatState(),
    val queue: List<MediaItem> = emptyList(),
    val browserPath: String = "/storage/emulated/0/Music",
    val currentFolder: String = "",
    val folderTracks: List<AudioTrack> = emptyList(),
)
