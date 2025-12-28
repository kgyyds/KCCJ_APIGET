package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

data class FuzzyQueryState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList(),
    val progress: Pair<Int, Int>? = null,
    val threadCount: Int = 4, // 默认线程数
    val foundCount: Int = 0   // 已找到的数量
)

class FuzzyQueryViewModel : ViewModel() {
    private val repo = ScoreRepository(Net.api)

    private val _state = MutableStateFlow(FuzzyQueryState())
    val state: StateFlow<FuzzyQueryState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var foundStudent: Boolean = false // 标记是否已找到学生

    // 线程池相关
    private var threadPoolExecutor = Executors.newFixedThreadPool(4)
    private var customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

    fun updateThreadCount(count: Int) {
        if (count < 1 || count > 32) return
        
        // 关闭旧的线程池
        threadPoolExecutor.shutdown()
        
        // 创建新的线程池
        threadPoolExecutor = Executors.newFixedThreadPool(count)
        customDispatcher = threadPoolExecutor.asCoroutineDispatcher()
        
        _state.update { it.copy(threadCount = count) }
    }

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

        if (range == null) {
            _state.value = FuzzyQueryState(error = "请输入学号范围，例如 4112440401-4112440410")
            return
        }

        // 重置状态
        foundStudent = false
        
        // 取消之前的搜索
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            try {
                _state.value = FuzzyQueryState(
                    loading = true,
                    threadCount = _state.value.threadCount,
                    foundCount = 0
                )

                val allResults = mutableListOf<ScoreEntry>()
                val (start, end) = range
                val total = (end - start + 1).toInt()

                if (total <= 0) {
                    _state.value = FuzzyQueryState(error = "范围不合法")
                    return@launch
                }

                // 限制范围
                if (total > 10000) {
                    _state.value = FuzzyQueryState(
                        loading = false,
                        error = "范围太大（$total），请控制在 10000 以内"
                    )
                    return@launch
                }

                // 使用通道来协调工作
                val numberChannel = Channel<Long>(Channel.UNLIMITED)
                val resultChannel = Channel<List<ScoreEntry>>(Channel.UNLIMITED)

                // 启动工作协程（使用自定义的线程池）
                val workers = List(_state.value.threadCount) { workerId ->
                    launch(customDispatcher) {
                        for (num in numberChannel) {
                            // 如果已经找到学生，停止处理
                            if (foundStudent) break
                            
                            val result = repo.exactQuery(name, num.toString())
                            result.onSuccess { list ->
                                if (list.isNotEmpty()) {
                                    // 找到数据，发送到结果通道
                                    resultChannel.send(list)
                                }
                            }
                        }
                    }
                }

                // 启动结果收集协程
                val collector = launch {
                    for (results in resultChannel) {
                        allResults.addAll(results)
                        
                        // 更新找到的数量
                        _state.update { it.copy(foundCount = allResults.size) }
                        
                        // 如果找到任意结果，标记为已找到
                        if (results.isNotEmpty()) {
                            foundStudent = true
                            break
                        }
                    }
                }

                // 发送数字到通道
                var current = 0
                for (num in start..end) {
                    if (foundStudent) break // 如果已找到，停止发送
                    
                    current++
                    _state.update { it.copy(progress = Pair(current, total)) }
                    
                    numberChannel.send(num)
                    
                    // 小延迟避免过快发送
                    delay(1)
                }

                // 关闭通道
                numberChannel.close()
                
                // 等待所有worker完成
                workers.forEach { it.join() }
                
                // 关闭结果通道并等待收集器完成
                resultChannel.close()
                collector.join()

                // 去重
                val distinctResults = allResults.distinctBy { entry ->
                    "${entry.studentNum}-${entry.examName}-${entry.course}"
                }

                _state.value = FuzzyQueryState(
                    loading = false,
                    error = if (distinctResults.isEmpty()) "未找到匹配结果" else null,
                    data = distinctResults,
                    threadCount = _state.value.threadCount,
                    foundCount = distinctResults.size
                )

            } catch (e: Exception) {
                _state.value = FuzzyQueryState(
                    loading = false,
                    error = "查询失败: ${e.message ?: e.javaClass.simpleName}",
                    data = emptyList(),
                    threadCount = _state.value.threadCount,
                    foundCount = 0
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
        foundStudent = false
        _state.value = FuzzyQueryState()
    }

    fun cancelSearch() {
        searchJob?.cancel()
        foundStudent = true // 标记为已找到，停止所有线程
        _state.update { it.copy(loading = false) }
    }

    override fun onCleared() {
        searchJob?.cancel()
        threadPoolExecutor.shutdown()
        super.onCleared()
    }
}