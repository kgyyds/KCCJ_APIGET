package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExactUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList()
)

class ExactQueryViewModel : ViewModel() {
    private val repo = ScoreRepository(Net.api)

    private val _state = MutableStateFlow(ExactUiState())
    val state: StateFlow<ExactUiState> = _state

    fun search(name: String, num: String) {
        if (name.isBlank() || num.isBlank()) {
            _state.value = ExactUiState(error = "名字和学号都要填噢～")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, data = emptyList())

            val result = repo.exactQuery(name, num)
            _state.value = result.fold(
                onSuccess = { list ->
                    ExactUiState(
                        loading = false,
                        error = if (list.isEmpty()) "没查到数据（可能名字/学号不匹配）" else null,
                        data = list
                    )
                },
                onFailure = { e ->
                    ExactUiState(
                        loading = false,
                        error = "请求失败：${e.message ?: e.javaClass.simpleName}",
                        data = emptyList()
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}