package com.example.androidmusicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.androidmusicplayer.viewmodel.PlayerUiState
import com.example.androidmusicplayer.viewmodel.PlayerViewModel
import java.io.File

@Composable
fun FolderBrowserScreen(state: PlayerUiState, viewModel: PlayerViewModel) {
    var path by remember(state.browserPath) { mutableStateOf(state.browserPath) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(state.browserPath) { path = state.browserPath }
    val filtered = state.folderTracks.filter {
        it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
    }

    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = {
                val parent = File(path).parent
                if (parent != null) viewModel.loadFolder(parent)
            }) { Text("戻る") }
            Spacer(Modifier.padding(4.dp))
            Button(onClick = { viewModel.loadFolder(path) }) { Text("更新") }
        }
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text("絶対パス") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { viewModel.loadFolder(path) }),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("曲名・アーティスト検索") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn {
            items(filtered, key = { it.absolutePath }) { track ->
                ListItem(
                    headlineContent = { Text(track.title) },
                    supportingContent = { Text("${track.artist}\n${track.absolutePath}") },
                    modifier = Modifier.clickable { viewModel.playFromFile(track.absolutePath) },
                )
                HorizontalDivider()
            }
        }
    }
}
