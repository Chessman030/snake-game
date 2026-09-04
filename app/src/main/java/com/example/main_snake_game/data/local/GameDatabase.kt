package com.example.main_snake_game.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.main_snake_game.data.local.dao.PlayerDao
import com.example.main_snake_game.data.local.dao.GameRecordDao
import com.example.main_snake_game.data.local.entity.PlayerEntity
import com.example.main_snake_game.data.local.entity.GameRecordEntity

@Database(
    entities = [PlayerEntity::class, GameRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameRecordDao(): GameRecordDao
}
