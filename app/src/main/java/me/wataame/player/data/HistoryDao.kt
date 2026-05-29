package me.wataame.player.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(entity: HistoryEntity)

    @Query("SELECT * FROM play_history ORDER BY playedAtEpochMillis DESC")
    fun observeAll(): Flow<List<HistoryEntity>>
}
