package com.example.main_snake_game.data.remote.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.example.main_snake_game.game.model.Player
import kotlinx.coroutines.tasks.await

class FirebaseService(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    suspend fun signInAnonymously(): String? {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithEmail(email: String, password: String): String? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserWithEmail(email: String, password: String): String? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sign in with Google using an ID token (from Google Sign-In SDK)
     */
    suspend fun signInWithGoogle(idToken: String): String? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get current user's email if available
     */
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    /**
     * Get current user's display name if available
     */
    fun getCurrentUserName(): String? = auth.currentUser?.displayName

    suspend fun updatePlayerOnlineStatus(playerId: String, isOnline: Boolean) {
        try {
            database.reference.child("players").child(playerId).child("isOnline").setValue(isOnline).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updatePlayerLocation(playerId: String, latitude: Double, longitude: Double, geohash: String) {
        try {
            database.reference.child("players").child(playerId).updateChildren(
                mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "geohash" to geohash,
                    "lastUpdated" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create or update user profile in Firebase Realtime Database
     */
    suspend fun createOrUpdateUserProfile(
        userId: String,
        name: String,
        email: String? = null,
        highScore: Int = 0
    ) {
        try {
            val userProfile = mapOf(
                "userId" to userId,
                "name" to name,
                "email" to (email ?: ""),
                "highScore" to highScore,
                "createdAt" to System.currentTimeMillis(),
                "lastUpdated" to System.currentTimeMillis()
            )
            database.reference.child("users").child(userId).setValue(userProfile).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get user's current high score from Firebase
     */
    suspend fun getUserHighScore(userId: String): Int {
        return try {
            val snapshot = database.reference.child("users").child(userId).child("highScore").get().await()
            snapshot.getValue(Int::class.java) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Update user's high score if new score is higher
     */
    suspend fun updateUserHighScore(userId: String, newScore: Int): Boolean {
        return try {
            val currentHighScore = getUserHighScore(userId)
            if (newScore > currentHighScore) {
                database.reference.child("users").child(userId).child("highScore").setValue(newScore).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
    }
}
