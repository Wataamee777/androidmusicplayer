package com.example.androidmusicplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidmusicplayer.viewmodel.PlayerUiState
import com.example.androidmusicplayer.viewmodel.PlayerViewModel

@Composable
fun QueueScreen(state: PlayerUiState, viewModel: PlayerViewModel) {
    Column(Modifier.padding(top = 8.dp)) {
        Text("次に再生する曲: ${state.queue.size}曲")
        LazyColumn {
            itemsIndexed(state.queue) { index, item ->
                ListItem(
                    headlineContent = { Text(item.mediaMetadata.title?.toString().orEmpty()) },
                    supportingContent = { Text(item.mediaMetadata.artist?.toString().orEmpty()) },
                    trailingContent = {
                        Row {
                            Button(enabled = index > 0, onClick = { viewModel.moveQueueItem(index, index - 1) }) { Text("↑") }
                            Button(enabled = index < state.queue.lastIndex, onClick = { viewModel.moveQueueItem(index, index + 1) }) { Text("↓") }
                            Button(onClick = { viewModel.removeQueueItem(index) }) { Text("削除") }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
