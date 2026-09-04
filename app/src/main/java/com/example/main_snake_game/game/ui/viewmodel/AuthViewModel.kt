package com.example.main_snake_game.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.main_snake_game.data.repository.AuthRepository
import com.example.main_snake_game.data.repository.PlayerRepository
import com.example.main_snake_game.data.local.entity.PlayerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        _currentUserId.value = authRepository.getCurrentUserId()
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authRepository.signInAnonymously()
                if (userId != null) {
                    _currentUserId.value = userId
                    val playerName = "Player_${userId.takeLast(6)}"
                    val playerEntity = PlayerEntity(
                        id = userId,
                        name = playerName
                    )
                    playerRepository.savePlayer(playerEntity)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to sign in as guest"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authRepository.signInWithEmail(email, password)
                if (userId != null) {
                    _currentUserId.value = userId
                    val playerName = email.substringBefore("@")
                    val playerEntity = PlayerEntity(
                        id = userId,
                        name = playerName
                    )
                    playerRepository.savePlayer(playerEntity)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to sign in"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authRepository.signUpWithEmail(email, password)
                if (userId != null) {
                    _currentUserId.value = userId
                    val playerName = email.substringBefore("@")
                    val playerEntity = PlayerEntity(
                        id = userId,
                        name = playerName
                    )
                    playerRepository.savePlayer(playerEntity)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to create account"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUserId.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
