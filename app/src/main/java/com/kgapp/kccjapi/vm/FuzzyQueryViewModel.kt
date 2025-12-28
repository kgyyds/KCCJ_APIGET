package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class FuzzyQueryState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList(),
    val progress: Pair<Int, Int>? = null,
    val threadCount: Int = 4,
    val foundCount: Int = 0
)

class FuzzyQueryViewModel : ViewModel() {
    private val repo = ScoreRepository(Net.api)

    private val _state = MutableStateFlow(FuzzyQueryState())
    val state: StateFlow<FuzzyQueryState> = _state.asStateFlow()

    private var searchJob: Job? = null

    // ✅ 原子标记：跨线程可见，避免“看起来不停止”
    private val foundStudent = AtomicBoolean(false)

    // 线程池相关
    private var threadPoolExecutor = Executors.newFixedThreadPool(_state.value.threadCount)
    private var customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

    fun updateThreadCount(count: Int) {
        if (count < 1 || count > 32) return

        // ✅ 先停掉正在跑的任务，避免 shutdown 旧线程池时还在跑导致异常/卡住
        cancelSearch()

        // 关闭旧线程池
        threadPoolExecutor.shutdown()

        // 创建新线程池
        threadPoolExecutor = Executors.newFixedThreadPool(count)
        customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

        _state.update { it.copy(threadCount = count) }
    }

    fun search(name: String, numRange: String) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "请输入学生姓名") }
            return
        }

        val range = parseNumRange(numRange)
        if (range == null) {
            _state.update { it.copy(error = "请输入学号范围，例如 4112440401-4112440410") }
            return
        }

        val (start, end) = range
        val total = (end - start + 1).toInt()
        if (total <= 0) {
            _state.update { it.copy(error = "范围不合法") }
            return
        }
        if (total > 10000) {
            _state.update { it.copy(error = "范围太大（$total），请控制在 10000 以内") }
            return
        }

        // 重置标记
        foundStudent.set(false)

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
                val tc = _state.value.threadCount

                coroutineScope {
                    // ✅ 小容量 channel：防堆积、防爆内存
                    val numberChannel = Channel<Long>(capacity = tc * 2)

                    // ✅ workers：并发请求
                    val workers = List(tc) {
                        launch(customDispatcher) {
                            while (isActive) {
                                val num = numberChannel.receiveCatching().getOrNull() ?: break
                                if (foundStudent.get()) break

                                val result = repo.exactQuery(name, num.toString())
                                result.onSuccess { list ->
                                    if (list.isNotEmpty()) {
                                        // ✅ 第一个命中的 worker 负责“占坑+停机”
                                        if (foundStudent.compareAndSet(false, true)) {
                                            allResults.addAll(list)
                                            _state.update { st ->
                                                st.copy(foundCount = allResults.size)
                                            }
                                            // ✅ 直接取消整个 scope：所有 worker + producer 立刻停
                                            this@coroutineScope.cancel("Found result, stop all")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ✅ producer：逐个送号，更新进度
                    launch {
                        var current = 0
                        for (num in start..end) {
                            if (!isActive) break
                            if (foundStudent.get()) break

                            current++
                            _state.update { it.copy(progress = current to total) }

                            numberChannel.send(num)

                            // ✅ 可选：轻微限速（防止服务器炸）
                            // 你可以改成 delay(5) / delay(10)
                            delay(1)
                        }
                        numberChannel.close()
                    }

                    workers.forEach { it.join() }
                }

                // 去重（更稳一点，避免重复）
                val distinctResults = allResults.distinctBy { entry ->
                    "${entry.studentNum}-${entry.examName}-${entry.course}-${entry.score}"
                }

                _state.update {
                    it.copy(
                        loading = false,
                        error = if (distinctResults.isEmpty()) "未找到匹配结果" else null,
                        data = distinctResults,
                        foundCount = distinctResults.size,
                        progress = if (distinctResults.isEmpty()) it.progress else it.progress
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = "查询失败: ${e.message ?: e.javaClass.simpleName}",
                        data = emptyList(),
                        foundCount = 0
                    )
                }
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
        cancelSearch()
        _state.value = FuzzyQueryState(threadCount = _state.value.threadCount)
    }

    fun cancelSearch() {
        searchJob?.cancel()
        foundStudent.set(true)
        _state.update { it.copy(loading = false) }
    }

    override fun onCleared() {
        cancelSearch()
        threadPoolExecutor.shutdown()
        super.onCleared()
    }
}