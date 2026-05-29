package me.wataame.player.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioSessionRepository {
    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId = _audioSessionId.asStateFlow()
    fun update(id: Int) { _audioSessionId.value = id }
}
