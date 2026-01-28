package com.kgapp.kccjUltra.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjUltra.data.model.ExamDetailUi
import com.kgapp.kccjUltra.data.repo.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ExamDetailUiState(
    val examId: String,
    val detail: ExamDetailUi = ExamDetailUi(),
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
                _uiState.update { it.copy(detail = response) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "加载失败") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun updateScore(
        studentName: String,
        studentNum: String,
        course: String,
        score: String
    ): Boolean {
        val examId = _uiState.value.examId
        if (examId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "考试ID为空") }
            return false
        }
        val success = repository.updateExamScore(
            examId = examId,
            studentName = studentName,
            studentNum = studentNum,
            course = course,
            score = score
        )
        if (success) {
            refresh()
        }
        return success
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
