package com.kgapp.kccjapi.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.model.Exam
import com.kgapp.kccjapi.data.repo.ScoreRepository
import com.kgapp.kccjapi.data.store.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ExamListUiState(
    val username: String,
    val exams: List<Exam> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExamListViewModel(
    private val repository: ScoreRepository,
    private val preferences: UserPreferences,
    username: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExamListUiState(username = username))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val username = _uiState.value.username
        if (username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "用户名为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.loadExams(username)
            }.onSuccess { response ->
                _uiState.update { it.copy(exams = response.exams) }
                preferences.saveUsername(username)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "加载失败") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
