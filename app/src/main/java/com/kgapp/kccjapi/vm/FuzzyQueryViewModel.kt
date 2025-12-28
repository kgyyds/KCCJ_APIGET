package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FuzzyQueryState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList(),
    val progress: Pair<Int, Int>? = null
)

class FuzzyQueryViewModel : ViewModel() {

    private val repo = ScoreRepository(Net.api)

    private val _state = MutableStateFlow(FuzzyQueryState())
    val state: StateFlow<FuzzyQueryState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun search(name: String, numRange: String) {
        if (name.isBlank()) {
            _state.value = FuzzyQueryState(error = "请输入学生姓名")
            return
        }

        val range = parseNumRange(numRange)
        if (range == null && numRange.isNotBlank()) {
            _state.value = FuzzyQueryState(error = "学号范围格式错误，请使用如 4112440401-4112440410 的格式")
            return
        }

        // ✅ 接口形态需要学号，这里直接要求输入范围
        if (range == null) {
            _state.value = FuzzyQueryState(error = "请输入学号范围，例如 4112440401-4112440410")
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _state.value = FuzzyQueryState(loading = true)

                val allResults = mutableListOf<ScoreEntry>()

                val (start, end) = range
                val totalLong = end - start + 1

                if (totalLong <= 0L) {
                    _state.value = FuzzyQueryState(error = "范围不合法")
                    return@launch
                }

                // ✅ 限制范围，防止跑死
                if (totalLong > 2000L) {
                    _state.value = FuzzyQueryState(
                        loading = false,
                        error = "范围太大（$totalLong），请控制在 2000 以内"
                    )
                    return@launch
                }

                val total = totalLong.toInt()

                var current = 0
                var num = start
                while (num <= end) {
                    current += 1
                    _state.update { it.copy(progress = current to total) }

                    val result = repo.exactQuery(name, num.toString())
                    result.onSuccess { list ->
                        if (list.isNotEmpty()) allResults.addAll(list)
                    }

                    delay(50)
                    num += 1
                }

                val distinctResults = allResults.distinctBy { entry ->
                    "${entry.studentNum}-${entry.examName}-${entry.course}"
                }

                _state.value = FuzzyQueryState(
                    loading = false,
                    error = if (distinctResults.isEmpty()) "未找到匹配结果" else null,
                    data = distinctResults,
                    progress = null
                )
            } catch (e: Exception) {
                _state.value = FuzzyQueryState(
                    loading = false,
                    error = "查询失败: ${e.message ?: e.javaClass.simpleName}",
                    data = emptyList(),
                    progress = null
                )
            }
        }
    }

    private fun parseNumRange(rangeStr: String): Pair<Long, Long>? {
        if (rangeStr.isBlank()) return null
        val parts = rangeStr.split("-")
        if (parts.size != 2) return null
        return try {
            val start = parts[0].trim().toLong()
            val end = parts[1].trim().toLong()
            if (start <= end) start to end else null
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearData() {
        searchJob?.cancel()
        _state.value = FuzzyQueryState()
    }

    override fun onCleared() {
        searchJob?.cancel()
        super.onCleared()
    }
}