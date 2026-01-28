package com.kgapp.kccjapi.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.model.HeaderItem
import com.kgapp.kccjapi.data.store.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class HeaderSettingsUiState(
    val headers: List<HeaderItem> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class HeaderSettingsViewModel(
    private val preferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(HeaderSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.headersFlow.collect { headers ->
                _uiState.update { it.copy(headers = headers) }
            }
        }
    }

    fun addHeader() {
        _uiState.update {
            it.copy(headers = it.headers + HeaderItem(key = "", value = ""))
        }
    }

    fun updateHeader(index: Int, key: String? = null, value: String? = null) {
        _uiState.update { state ->
            val updated = state.headers.mapIndexed { i, item ->
                if (i == index) {
                    item.copy(
                        key = key ?: item.key,
                        value = value ?: item.value
                    )
                } else {
                    item
                }
            }
            state.copy(headers = updated)
        }
    }

    fun deleteHeader(index: Int) {
        _uiState.update { state ->
            val updated = state.headers.toMutableList().also { list ->
                if (index in list.indices) {
                    list.removeAt(index)
                }
            }
            state.copy(headers = updated)
        }
    }

    fun saveHeaders() {
        viewModelScope.launch {
            val headers = _uiState.value.headers.filter { it.key.isNotBlank() }
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                preferences.saveHeaders(headers)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "保存失败") }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
