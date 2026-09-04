package com.example.main_snake_game

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SnakeGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
