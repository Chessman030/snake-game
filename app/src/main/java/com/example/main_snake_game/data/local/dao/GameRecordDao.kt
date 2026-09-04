package com.example.main_snake_game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.main_snake_game.data.local.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Insert
    suspend fun insertGameRecord(record: GameRecordEntity)

    @Query("SELECT * FROM game_records WHERE playerId = :playerId ORDER BY timestamp DESC")
    fun getPlayerGameRecords(playerId: String): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records WHERE gameId = :gameId")
    fun getGameRecords(gameId: String): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentGames(limit: Int): Flow<List<GameRecordEntity>>
}
