package com.example.androidmusicplayer.viewmodel

import androidx.media3.common.MediaItem
import com.example.androidmusicplayer.domain.ABRepeatState
import com.example.androidmusicplayer.domain.AudioTrack
import com.example.androidmusicplayer.domain.PlaybackMode

data class PlayerUiState(
    val isDarkTheme: Boolean = true,
    val isPlaying: Boolean = false,
    val currentTitle: String = "No track",
    val currentArtist: String = "",
    val currentArtwork: ByteArray? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackMode: PlaybackMode = PlaybackMode.NORMAL,
    val abRepeat: ABRepeatState = ABRepeatState(),
    val queue: List<MediaItem> = emptyList(),
    val browserPath: String = "/storage/emulated/0/Music",
    val currentFolder: String = "",
    val folderTracks: List<AudioTrack> = emptyList(),
)
