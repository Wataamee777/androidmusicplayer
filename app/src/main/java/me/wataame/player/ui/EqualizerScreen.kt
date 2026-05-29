package me.wataame.player.ui

import android.media.audiofx.Equalizer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerScreen(audioSessionId: Int) {
    if (audioSessionId <= 0) {
        Text("再生を開始するとイコライザーを利用できます。")
        return
    }

    val levels = remember(audioSessionId) { mutableStateListOf<Short>() }
    val equalizer = remember(audioSessionId) { Equalizer(0, audioSessionId) }
    var enabled by remember(audioSessionId) { mutableStateOf(true) }

    LaunchedEffect(equalizer, enabled) {
        equalizer.enabled = enabled
    }
    DisposableEffect(equalizer) {
        onDispose { equalizer.release() }
    }

    val range = equalizer.bandLevelRange
    if (levels.isEmpty()) {
        repeat(equalizer.numberOfBands.toInt()) { band ->
            levels += equalizer.getBandLevel(band.toShort())
        }
    }

    Column(Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("イコライザー", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(if (enabled) "ON" else "OFF")
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
        Text("AudioSession: $audioSessionId", color = MaterialTheme.colorScheme.onSurfaceVariant)
        levels.forEachIndexed { index, level ->
            val band = index.toShort()
            val hz = equalizer.getCenterFreq(band) / 1000
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Band ${index + 1}: ${hz}Hz / ${level / 100}dB")
                Slider(
                    value = level.toFloat(),
                    valueRange = range[0].toFloat()..range[1].toFloat(),
                    enabled = enabled,
                    onValueChange = {
                        val shortLevel = it.toInt().toShort()
                        levels[index] = shortLevel
                        equalizer.setBandLevel(band, shortLevel)
                    },
                )
            }
        }
    }
}
