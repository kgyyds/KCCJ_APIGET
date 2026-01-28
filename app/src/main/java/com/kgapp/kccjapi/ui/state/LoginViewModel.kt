package com.kgapp.kccjapi.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.repo.ScoreRepository
import com.kgapp.kccjapi.data.store.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginEvent {
    data class Success(val username: String) : LoginEvent()
}

class LoginViewModel(
    private val repository: ScoreRepository,
    private val preferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            preferences.usernameFlow.collect { savedUsername ->
                _uiState.update { it.copy(username = savedUsername) }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun login() {
        val username = _uiState.value.username.trim()
        if (username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入手机号") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.loadExams(username)
            }.onSuccess {
                preferences.saveUsername(username)
                _events.emit(LoginEvent.Success(username))
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "登录失败") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
