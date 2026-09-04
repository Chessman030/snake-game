package com.example.main_snake_game.di

import android.content.Context
import com.example.main_snake_game.data.remote.service.FirebaseService
import com.example.main_snake_game.data.remote.service.LocationService
import com.example.main_snake_game.data.remote.service.WebSocketService
import com.example.main_snake_game.game.logic.CollisionDetector
import com.example.main_snake_game.game.logic.GameEngine
import com.example.main_snake_game.game.logic.ScoringEngine
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseService(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): FirebaseService {
        return FirebaseService(auth, database)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ): LocationService {
        return LocationService(context, fusedLocationClient)
    }

    @Provides
    @Singleton
    fun provideWebSocketService(): WebSocketService {
        return WebSocketService()
    }

    @Provides
    @Singleton
    fun provideScoringEngine(): ScoringEngine {
        return ScoringEngine()
    }

    @Provides
    @Singleton
    fun provideCollisionDetector(): CollisionDetector {
        return CollisionDetector()
    }

    @Provides
    @Singleton
    fun provideGameEngine(
        scoringEngine: ScoringEngine,
        collisionDetector: CollisionDetector
    ): GameEngine {
        return GameEngine(
            gridWidth = 30,
            gridHeight = 30,
            scoringEngine = scoringEngine,
            collisionDetector = collisionDetector
        )
    }
}
