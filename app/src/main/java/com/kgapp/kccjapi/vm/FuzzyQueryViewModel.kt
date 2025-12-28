package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FuzzyQueryState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList(),
    val progress: Pair<Int, Int>? = null // (当前进度, 总数)
)

class FuzzyQueryViewModel(
    private val repository: ScoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FuzzyQueryState())
    val state: StateFlow<FuzzyQueryState> = _state.asStateFlow()

    fun search(name: String, numRange: String) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "请输入学生姓名") }
            return
        }

        // 解析学号范围
        val range = parseNumRange(numRange)
        if (range == null && numRange.isNotBlank()) {
            _state.update { it.copy(error = "学号范围格式错误，请使用如 42000-42999 的格式") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true, data = emptyList(), progress = null) }
                
                val allResults = mutableListOf<ScoreEntry>()
                
                if (range != null) {
                    // 遍历学号范围
                    val (start, end) = range
                    val total = end - start + 1
                    
                    for ((index, num) in (start..end).withIndex()) {
                        // 更新进度
                        _state.update { it.copy(progress = Pair(index + 1, total)) }
                        
                        // 查询当前学号
                        val result = repository.exactQuery(name, num.toString())
                        result.onSuccess { entries: List<ScoreEntry> ->
                            // 只添加有结果的记录
                            if (entries.isNotEmpty()) {
                                allResults.addAll(entries)
                            }
                        }.onFailure { e ->
                            // 单个查询失败不中断整体查询，只是跳过
                            println("查询学号 $num 失败: ${e.message}")
                        }
                        
                        // 添加延迟避免请求过快
                        kotlinx.coroutines.delay(100)
                    }
                } else {
                    // 如果没有学号范围，只按姓名查询
                    val result = repository.exactQuery(name, "")
                    result.onSuccess { entries: List<ScoreEntry> ->
                        allResults.addAll(entries)
                    }.onFailure { e ->
                        throw e // 单个查询失败时抛出异常
                    }
                }
                
                // 去重：学号-考试名-课程名
                val distinctResults = allResults.distinctBy { entry -> 
                    "${entry.studentNum}-${entry.examName}-${entry.course}"
                }
                
                _state.update { 
                    it.copy(
                        loading = false,
                        data = distinctResults,
                        progress = null,
                        error = if (distinctResults.isEmpty()) "未找到匹配结果" else null
                    )
                }
                
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = "查询失败: ${e.message}",
                        progress = null
                    )
                }
            }
        }
    }

    private fun parseNumRange(rangeStr: String): Pair<Int, Int>? {
        if (rangeStr.isBlank()) return null
        
        val parts = rangeStr.split("-")
        if (parts.size != 2) return null
        
        return try {
            val start = parts[0].trim().toInt()
            val end = parts[1].trim().toInt()
            if (start in 1..999999 && end in 1..999999 && start <= end) {
                Pair(start, end)
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearData() {
        _state.update { it.copy(data = emptyList(), progress = null) }
    }
}