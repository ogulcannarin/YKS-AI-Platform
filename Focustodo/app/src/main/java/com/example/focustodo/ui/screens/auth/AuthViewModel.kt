package com.example.focustodo.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.focustodo.data.remote.SupabaseUser
import com.example.focustodo.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
    val currentUser: StateFlow<SupabaseUser?> = _currentUser.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.login(email, passwordHash)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, fullName: String, passwordHash: String, targetRank: Int?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.register(email, fullName, passwordHash, targetRank)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
