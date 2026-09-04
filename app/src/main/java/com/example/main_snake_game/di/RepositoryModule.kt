package com.example.main_snake_game.di

import com.example.main_snake_game.data.repository.AuthRepository
import com.example.main_snake_game.data.repository.GameRepository
import com.example.main_snake_game.data.repository.LocationRepository
import com.example.main_snake_game.data.repository.PlayerRepository
import com.example.main_snake_game.data.local.dao.GameRecordDao
import com.example.main_snake_game.data.local.dao.PlayerDao
import com.example.main_snake_game.data.remote.service.FirebaseService
import com.example.main_snake_game.data.remote.service.LocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseService: FirebaseService
    ): AuthRepository {
        return AuthRepository(firebaseService)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(
        playerDao: PlayerDao,
        firebaseService: FirebaseService
    ): PlayerRepository {
        return PlayerRepository(playerDao, firebaseService)
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        gameRecordDao: GameRecordDao
    ): GameRepository {
        return GameRepository(gameRecordDao)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        locationService: LocationService
    ): LocationRepository {
        return LocationRepository(locationService)
    }
}
