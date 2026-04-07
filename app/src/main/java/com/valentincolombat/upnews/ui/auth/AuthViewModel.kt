package com.valentincolombat.upnews.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository.shared

    // MARK: - Exposed State

    val isLoading: StateFlow<Boolean> = repository.isLoading
    val errorMessage: StateFlow<String?> = repository.errorMessage

    // MARK: - Actions

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            repository.signIn(email, password)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            repository.signUp(email, password)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            repository.signInWithGoogle(idToken)
        }
    }

    fun clearError() {
        repository.clearError()
    }

    fun setGoogleError(msg: String) {
        repository.setError(msg)
    }
}
