package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
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
    val progress: Pair<Int, Int>? = null // (当前进度, 总数)
)

class FuzzyQueryViewModel : ViewModel() { // 移除构造函数参数

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
                
                // 这里先模拟查询，你可以后续替换为实际API调用
                if (range != null) {
                    // 遍历学号范围
                    val (start, end) = range
                    val total = end - start + 1
                    
                    for ((index, num) in (start..end).withIndex()) {
                        // 更新进度
                        _state.update { it.copy(progress = Pair(index + 1, total)) }
                        
                        // 模拟API调用延迟
                        delay(50)
                        
                        // 这里可以添加实际API调用
                        // val result = repository.exactQuery(name, num.toString())
                    }
                }
                
                // 模拟查询结果
                delay(1000)
                
                // 模拟一些测试数据
                val mockData = if (range != null && name.contains("测试")) {
                    listOf(
                        ScoreEntry(
                            studentName = name,
                            studentNum = "${range.first}",
                            examName = "期末考试",
                            course = "数学",
                            score = "85",
                            searchTime = "2024-01-15 10:30"
                        ),
                        ScoreEntry(
                            studentName = name,
                            studentNum = "${range.first}",
                            examName = "期末考试",
                            course = "语文",
                            score = "92",
                            searchTime = "2024-01-15 10:30"
                        )
                    )
                } else {
                    emptyList()
                }
                
                _state.update { 
                    it.copy(
                        loading = false,
                        data = mockData,
                        progress = null,
                        error = if (mockData.isEmpty()) "未找到匹配结果" else null
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
        // 移除不必要的大小限制，只检查start <= end
        if (start <= end) {
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