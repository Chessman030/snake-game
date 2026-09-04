package com.example.main_snake_game.data.repository

import com.example.main_snake_game.data.remote.service.FirebaseService

class AuthRepository(
    private val firebaseService: FirebaseService
) {

    suspend fun signInAnonymously(): String? {
        return firebaseService.signInAnonymously()
    }

    suspend fun signInWithEmail(email: String, password: String): String? {
        return firebaseService.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String): String? {
        return firebaseService.createUserWithEmail(email, password)
    }


    fun getCurrentUserId(): String? {
        return firebaseService.getCurrentUserId()
    }

    fun logout() {
        firebaseService.logout()
    }
}
