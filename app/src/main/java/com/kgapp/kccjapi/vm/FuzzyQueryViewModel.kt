package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
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

    // ✅ 跨线程安全停止标记
    private val foundStudent = AtomicBoolean(false)

    // 线程池相关
    private var threadPoolExecutor = Executors.newFixedThreadPool(_state.value.threadCount)
    private var customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

    fun updateThreadCount(count: Int) {
        if (count < 1 || count > 32) return

        // ✅ 改线程数前先停掉当前搜索，避免旧任务跑在旧线程池上
        cancelSearch()

        threadPoolExecutor.shutdown()
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

        // reset
        foundStudent.set(false)

        // cancel previous
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            // ✅ 结果容器
            val allResults = mutableListOf<ScoreEntry>()

            try {
                _state.value = FuzzyQueryState(
                    loading = true,
                    threadCount = _state.value.threadCount,
                    foundCount = 0,
                    progress = 0 to total
                )

                val tc = _state.value.threadCount

                // ✅ 小容量，避免塞爆（范围小也更快被 workers 吃完）
                val numberChannel = Channel<Long>(capacity = tc * 2)

                supervisorScope {
                    // workers
                    val workers = List(tc) {
                        launch(customDispatcher) {
                            while (isActive) {
                                val num = numberChannel.receiveCatching().getOrNull() ?: break
                                if (foundStudent.get()) break

                                // ✅ 单次请求失败不要炸全局
                                val list: List<ScoreEntry>? = try {
                                    val r = repo.exactQuery(name, num.toString())
                                    r.getOrNull()
                                } catch (_: Throwable) {
                                    null
                                }

                                if (!list.isNullOrEmpty()) {
                                    // ✅ 第一个命中的 worker 负责“展示 + 停机”
                                    if (foundStudent.compareAndSet(false, true)) {

                                        allResults.addAll(list)

                                        // 去重（避免重复）
                                        val distinctResults = allResults.distinctBy { entry ->
                                            "${entry.studentNum}-${entry.examName}-${entry.course}-${entry.score}"
                                        }

                                        // ✅ 立刻把结果推给 UI（不要等 join 完）
                                        _state.update { st ->
                                            st.copy(
                                                loading = false,
                                                error = null,
                                                data = distinctResults,
                                                foundCount = distinctResults.size
                                            )
                                        }

                                        // ✅ 立刻停止：关闭 channel + cancel 作用域
                                        numberChannel.close()
                                        this@supervisorScope.cancel(CancellationException("FOUND_RESULT"))
                                    }
                                }
                            }
                        }
                    }

                    // producer（在当前协程上跑即可）
                    launch {
                        var current = 0
                        for (num in start..end) {
                            if (!isActive) break
                            if (foundStudent.get()) break

                            current++
                            _state.update { it.copy(progress = current to total) }

                            numberChannel.send(num)

                            // ⚡范围小想更快：可以改成 0
                            // 若怕服务器压力：改成 delay(5) 或 delay(10)
                            // delay(1)
                        }
                        numberChannel.close()
                    }

                    workers.forEach { it.join() }
                }

                // 如果跑完没找到
                if (!foundStudent.get()) {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = "未找到匹配结果",
                            data = emptyList(),
                            foundCount = 0
                        )
                    }
                }

            } catch (e: CancellationException) {
                // ✅ FOUND_RESULT / 用户取消 都会走到这里：不要当失败
                if (!foundStudent.get()) {
                    // 用户取消时，foundStudent 会在 cancelSearch() 里置 true，
                    // 这里如果不是“找到结果”导致的取消，就只关 loading
                    _state.update { it.copy(loading = false) }
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
        foundStudent.set(true) // 让 workers 自己停
        _state.update { it.copy(loading = false) }
    }

    override fun onCleared() {
        cancelSearch()
        threadPoolExecutor.shutdown()
        super.onCleared()
    }
}