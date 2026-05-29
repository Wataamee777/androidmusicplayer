package me.wataame.player.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val title: String,
    val artist: String,
    val playedAtEpochMillis: Long,
)
