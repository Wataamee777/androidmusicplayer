package me.wataame.player.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.wataame.player.viewmodel.PlayerUiState
import me.wataame.player.viewmodel.PlayerViewModel
import kotlin.math.max

@Composable
fun PlayerScreen(state: PlayerUiState, viewModel: PlayerViewModel) {
    Column(
        Modifier.fillMaxSize().padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark mode")
            Switch(checked = state.isDarkTheme, onCheckedChange = { viewModel.toggleDarkTheme() })
        }
        AlbumArt(state.currentArtwork)
        Spacer(Modifier.height(22.dp))
        Text(state.currentTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(state.currentArtist, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Slider(
            value = state.positionMs.toFloat(),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..max(1L, state.durationMs).toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(state.positionMs))
            Text("-${formatTime((state.durationMs - state.positionMs).coerceAtLeast(0L))}")
        }
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::previous) { Icon(Icons.Default.SkipPrevious, contentDescription = "前へ") }
            IconButton(onClick = { viewModel.seekBy(-10_000L) }) { Icon(Icons.Default.FastRewind, contentDescription = "10秒戻る") }
            IconButton(onClick = viewModel::playPause, modifier = Modifier.size(72.dp)) {
                Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "再生/一時停止", modifier = Modifier.size(56.dp))
            }
            IconButton(onClick = { viewModel.seekBy(10_000L) }) { Icon(Icons.Default.FastForward, contentDescription = "10秒送り") }
            IconButton(onClick = viewModel::next) { Icon(Icons.Default.SkipNext, contentDescription = "次へ") }
        }
        Button(onClick = viewModel::cyclePlaybackMode) { Text("再生モード: ${state.playbackMode.label}") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::setPointA) { Text("A=${state.abRepeat.pointA?.let(::formatTime) ?: "未設定"}") }
            Button(onClick = viewModel::setPointB) { Text("B=${state.abRepeat.pointB?.let(::formatTime) ?: "未設定"}") }
            Button(onClick = viewModel::clearABRepeat) { Text("A-B Off") }
        }
    }
}

@Composable
private fun AlbumArt(bytes: ByteArray?) {
    val bitmap = remember(bytes) { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } }
    Box(
        Modifier.size(280.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = "Album artwork", modifier = Modifier.fillMaxSize())
        else Text("No Artwork", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
