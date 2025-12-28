package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
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

class FuzzyQueryViewModel : ViewModel() {
    private val repo = ScoreRepository(Net.api)  // 添加Repository

    private val _state = MutableStateFlow(FuzzyQueryState())
    val state: StateFlow<FuzzyQueryState> = _state.asStateFlow()

    fun search(name: String, numRange: String) {
        if (name.isBlank()) {
            _state.value = FuzzyQueryState(error = "请输入学生姓名")
            return
        }

        // 解析学号范围
        val range = parseNumRange(numRange)
        if (range == null && numRange.isNotBlank()) {
            _state.value = FuzzyQueryState(error = "学号范围格式错误，请使用如 42000-42999 的格式")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = FuzzyQueryState(
                    loading = true,
                    error = null,
                    data = emptyList(),
                    progress = null
                )
                
                val allResults = mutableListOf<ScoreEntry>()
                
                if (range != null) {
                    // 遍历学号范围
                    val (start, end) = range
                    val total = end - start + 1
                    
                    for ((index, num) in (start..end).withIndex()) {
                        // 更新进度
                        _state.update { it.copy(progress = Pair(index + 1, total)) }
                        
                        // 实际API调用
                        val result = repo.exactQuery(name, num.toString())
                        result.onSuccess { list ->
                            // 只添加有结果的记录
                            if (list.isNotEmpty()) {
                                allResults.addAll(list)
                            }
                        }.onFailure { e ->
                            // 单个查询失败不中断整体查询，只是跳过
                            // 可以记录日志
                        }
                        
                        // 添加延迟避免请求过快
                        kotlinx.coroutines.delay(50)
                    }
                } else {
                    // 如果没有学号范围，只按姓名查询
                    val result = repo.exactQuery(name, "")
                    result.onSuccess { list ->
                        allResults.addAll(list)
                    }.onFailure { e ->
                        throw e // 单个查询失败时抛出异常
                    }
                }
                
                // 去重：学号-考试名-课程名
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
        if (start <= end) (start to end) else null
    } catch (_: NumberFormatException) {
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