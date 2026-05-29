package me.wataame.player

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.wataame.player.ui.EqualizerScreen
import me.wataame.player.ui.FolderBrowserScreen
import me.wataame.player.ui.HistoryScreen
import me.wataame.player.ui.MusicPlayerTheme
import me.wataame.player.ui.PlayerScreen
import me.wataame.player.ui.QueueScreen
import me.wataame.player.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(viewModel) }
    }
}

@Composable
private fun App(viewModel: PlayerViewModel) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(AppTab.Player) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    LaunchedEffect(Unit) {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        if (permissions.isNotEmpty()) permissionLauncher.launch(permissions)
        viewModel.loadFolder(state.browserPath)
    }

    MusicPlayerTheme(darkTheme = state.isDarkTheme) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(12.dp)) {
                Row {
                    AppTab.entries.forEach { item ->
                        Button(onClick = { tab = item }, modifier = Modifier.padding(end = 6.dp)) { Text(item.label) }
                    }
                }
                when (tab) {
                    AppTab.Player -> PlayerScreen(state, viewModel)
                    AppTab.Folders -> FolderBrowserScreen(state, viewModel)
                    AppTab.Queue -> QueueScreen(state, viewModel)
                    AppTab.History -> HistoryScreen(viewModel)
                    AppTab.Equalizer -> EqualizerScreen(state.audioSessionId)
                }
            }
        }
    }
}

private enum class AppTab(val label: String) {
    Player("Player"), Folders("Folders"), Queue("Queue"), History("History"), Equalizer("EQ")
}
