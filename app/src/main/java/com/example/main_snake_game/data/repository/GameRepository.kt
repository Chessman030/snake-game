package com.example.main_snake_game.data.repository

import com.example.main_snake_game.data.local.dao.GameRecordDao
import com.example.main_snake_game.data.local.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameRecordDao: GameRecordDao
) {

    suspend fun saveGameRecord(record: GameRecordEntity) {
        gameRecordDao.insertGameRecord(record)
    }

    fun getPlayerGameRecords(playerId: String): Flow<List<GameRecordEntity>> {
        return gameRecordDao.getPlayerGameRecords(playerId)
    }

    fun getGameRecords(gameId: String): Flow<List<GameRecordEntity>> {
        return gameRecordDao.getGameRecords(gameId)
    }

    fun getRecentGames(limit: Int): Flow<List<GameRecordEntity>> {
        return gameRecordDao.getRecentGames(limit)
    }
}
