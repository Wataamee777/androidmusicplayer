package com.example.androidmusicplayer.ui

import android.media.audiofx.Equalizer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.androidmusicplayer.playback.AudioSessionRepository

@Composable
fun EqualizerScreen() {
    val sessionId by AudioSessionRepository.audioSessionId.collectAsState()
    if (sessionId == 0) {
        Text("再生開始後にイコライザーを利用できます。")
        return
    }
    val levels = remember(sessionId) { mutableStateListOf<Short>() }
    val equalizer = remember(sessionId) { Equalizer(0, sessionId).apply { enabled = true } }
    DisposableEffect(equalizer) { onDispose { equalizer.release() } }
    val range = equalizer.bandLevelRange
    if (levels.isEmpty()) repeat(equalizer.numberOfBands.toInt()) { band -> levels += equalizer.getBandLevel(band.toShort()) }
    Column {
        Text("Equalizer / AudioSession: $sessionId")
        levels.forEachIndexed { index, level ->
            val band = index.toShort()
            val hz = equalizer.getCenterFreq(band) / 1000
            Row(Modifier.fillMaxWidth()) {
                Text("${hz}Hz")
                Slider(
                    value = level.toFloat(),
                    valueRange = range[0].toFloat()..range[1].toFloat(),
                    onValueChange = {
                        val shortLevel = it.toInt().toShort()
                        levels[index] = shortLevel
                        equalizer.setBandLevel(band, shortLevel)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
