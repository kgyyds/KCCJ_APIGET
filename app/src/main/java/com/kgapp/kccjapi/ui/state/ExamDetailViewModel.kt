package com.kgapp.kccjapi.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.kgapp.kccjapi.data.model.Course
import com.kgapp.kccjapi.data.repo.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ExamDetailUiState(
    val examId: String,
    val courses: List<Course> = emptyList(),
    val students: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExamDetailViewModel(
    private val repository: ScoreRepository,
    examId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExamDetailUiState(examId = examId))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val examId = _uiState.value.examId
        if (examId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "考试ID为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.loadExamDetail(examId)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        courses = response.coursesList,
                        students = response.studentScoreList
                    )
                }
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
