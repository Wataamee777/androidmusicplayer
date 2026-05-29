package me.wataame.player.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.wataame.player.viewmodel.PlayerViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(viewModel: PlayerViewModel) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    val grouped = history.groupBy { DateFormat.getDateInstance().format(Date(it.playedAtEpochMillis)) }
    LazyColumn {
        grouped.forEach { (date, rows) ->
            item { Text(date) }
            items(rows, key = { it.id }) { row ->
                ListItem(
                    headlineContent = { Text(row.title) },
                    supportingContent = { Text(row.artist) },
                )
                HorizontalDivider()
            }
        }
    }
}
