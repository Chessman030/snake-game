package com.example.main_snake_game.di

import android.content.Context
import androidx.room.Room
import com.example.main_snake_game.data.local.GameDatabase
import com.example.main_snake_game.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGameDatabase(
        @ApplicationContext context: Context
    ): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providePlayerDao(database: GameDatabase) = database.playerDao()

    @Provides
    @Singleton
    fun provideGameRecordDao(database: GameDatabase) = database.gameRecordDao()
}
