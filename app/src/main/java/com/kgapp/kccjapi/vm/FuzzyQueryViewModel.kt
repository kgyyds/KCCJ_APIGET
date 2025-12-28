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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

enum class WorkerStatus { IDLE, RUNNING, SUCCESS, FAIL, STOPPED }

data class WorkerState(
    val id: Int,
    val status: WorkerStatus = WorkerStatus.IDLE,
    val currentNum: Long? = null,
    val lastMessage: String = ""
)

data class LogLine(
    val ts: String,
    val workerId: Int,
    val level: String, // "INFO" | "OK" | "ERR"
    val message: String
)

data class FuzzyQueryState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList(),
    val progress: Pair<Int, Int>? = null,
    val threadCount: Int = 4,
    val foundCount: Int = 0,
    val workers: List<WorkerState> = emptyList(),
    val logs: List<LogLine> = emptyList()
)

class FuzzyQueryViewModel : ViewModel() {
    private val repo = ScoreRepository(Net.api)

    private val _state = MutableStateFlow(FuzzyQueryState(threadCount = 4))
    val state: StateFlow<FuzzyQueryState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private val foundStudent = AtomicBoolean(false)

    private var threadPoolExecutor = Executors.newFixedThreadPool(_state.value.threadCount)
    private var customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    // 日志最多保留多少行（避免内存炸）
    private val maxLogs = 200

    private fun nowTs(): String = timeFmt.format(Date())

    private fun pushLog(workerId: Int, level: String, msg: String) {
        _state.update { st ->
            val newList = (st.logs + LogLine(nowTs(), workerId, level, msg))
            st.copy(logs = if (newList.size > maxLogs) newList.takeLast(maxLogs) else newList)
        }
    }

    private fun initWorkers(count: Int) {
        _state.update { st ->
            st.copy(
                workers = List(count) { idx -> WorkerState(id = idx, status = WorkerStatus.IDLE) }
            )
        }
    }

    private fun setWorker(
        workerId: Int,
        status: WorkerStatus? = null,
        currentNum: Long? = null,
        lastMessage: String? = null
    ) {
        _state.update { st ->
            if (workerId !in st.workers.indices) return@update st
            val mutable = st.workers.toMutableList()
            val old = mutable[workerId]
            mutable[workerId] = old.copy(
                status = status ?: old.status,
                currentNum = currentNum ?: old.currentNum,
                lastMessage = lastMessage ?: old.lastMessage
            )
            st.copy(workers = mutable)
        }
    }

    fun updateThreadCount(count: Int) {
        if (count < 1 || count > 32) return

        cancelSearch()

        threadPoolExecutor.shutdown()
        threadPoolExecutor = Executors.newFixedThreadPool(count)
        customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

        _state.update { it.copy(threadCount = count) }
        initWorkers(count)
        pushLog(-1, "INFO", "线程数已更新为 $count")
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

        foundStudent.set(false)
        searchJob?.cancel()

        val tc = _state.value.threadCount
        initWorkers(tc)

        _state.update {
            it.copy(
                loading = true,
                error = null,
                data = emptyList(),
                foundCount = 0,
                progress = 0 to total,
                logs = emptyList()
            )
        }
        pushLog(-1, "INFO", "开始并发查询 name=$name, range=$start-$end, threads=$tc")

        searchJob = viewModelScope.launch {
            val allResults = mutableListOf<ScoreEntry>()

            // ✅ 小容量：避免堆积
            val numberChannel = Channel<Long>(capacity = tc * 2)

            // 进度节流：减少 UI 重组
            var lastProgressEmit = 0L
            fun emitProgressThrottled(current: Int) {
                val now = System.currentTimeMillis()
                if (now - lastProgressEmit >= 60) { // 约 16fps 的进度刷新
                    lastProgressEmit = now
                    _state.update { it.copy(progress = current to total) }
                }
            }

            try {
                supervisorScope {
                    // workers
                    val workers = List(tc) { workerId ->
                        launch(customDispatcher) {
                            setWorker(workerId, status = WorkerStatus.IDLE, currentNum = null, lastMessage = "ready")
                            pushLog(workerId, "INFO", "worker#$workerId ready")

                            while (isActive) {
                                val num = numberChannel.receiveCatching().getOrNull() ?: break
                                if (foundStudent.get()) break

                                setWorker(workerId, status = WorkerStatus.RUNNING, currentNum = num, lastMessage = "querying")
                                // 如果你想更“爽快”看到每个号的日志：打开这行（会刷很多）
                                // pushLog(workerId, "INFO", "query $num")

                                val list: List<ScoreEntry>? = try {
                                    val r = repo.exactQuery(name, num.toString())
                                    r.getOrNull()
                                } catch (t: Throwable) {
                                    setWorker(workerId, status = WorkerStatus.FAIL, currentNum = num, lastMessage = (t.message ?: t.javaClass.simpleName))
                                    pushLog(workerId, "ERR", "num=$num 失败: ${t.message ?: t.javaClass.simpleName}")
                                    null
                                }

                                if (!list.isNullOrEmpty()) {
                                    // 命中：第一个命中的 worker 负责“占坑 + UI更新 + 停机”
                                    if (foundStudent.compareAndSet(false, true)) {
                                        setWorker(workerId, status = WorkerStatus.SUCCESS, currentNum = num, lastMessage = "HIT(${list.size})")
                                        pushLog(workerId, "OK", "num=$num 命中 ${list.size} 条 ✅")

                                        allResults.addAll(list)

                                        val distinct = allResults.distinctBy { e ->
                                            "${e.studentNum}-${e.examName}-${e.course}-${e.score}"
                                        }

                                        _state.update { st ->
                                            st.copy(
                                                loading = false,
                                                error = null,
                                                data = distinct,
                                                foundCount = distinct.size
                                            )
                                        }

                                        // 停机：关闭 channel + 取消 scope
                                        numberChannel.close()
                                        this@supervisorScope.cancel(CancellationException("FOUND_RESULT"))
                                    }
                                } else {
                                    // 没命中也给个轻量状态
                                    setWorker(workerId, status = WorkerStatus.IDLE, currentNum = num, lastMessage = "miss")
                                }
                            }

                            if (!foundStudent.get()) {
                                setWorker(workerId, status = WorkerStatus.STOPPED, lastMessage = "done")
                                pushLog(workerId, "INFO", "worker#$workerId done")
                            } else {
                                setWorker(workerId, status = WorkerStatus.STOPPED, lastMessage = "stopped")
                            }
                        }
                    }

                    // producer
                    launch {
                        var current = 0
                        for (num in start..end) {
                            if (!isActive) break
                            if (foundStudent.get()) break

                            current++
                            emitProgressThrottled(current)
                            numberChannel.send(num)

                            // 若觉得“太慢”：保持注释（不 delay）
                            // 若怕服务器压力：改成 delay(5) / delay(10)
                            // delay(1)
                        }
                        // 最终进度确保打满一次
                        _state.update { it.copy(progress = total to total) }
                        numberChannel.close()
                    }

                    workers.forEach { it.join() }
                }

                // 没找到
                if (!foundStudent.get()) {
                    pushLog(-1, "INFO", "扫描结束：未命中")
                    _state.update {
                        it.copy(
                            loading = false,
                            error = "未找到匹配结果",
                            data = emptyList(),
                            foundCount = 0
                        )
                    }
                } else {
                    pushLog(-1, "OK", "已命中，全部线程已停止")
                }

            } catch (e: CancellationException) {
                // ✅ FOUND_RESULT 或用户取消，都不当“失败”
                if (foundStudent.get()) {
                    // 找到结果导致的取消：UI 已更新过
                    pushLog(-1, "OK", "停止原因：命中结果，已终止所有任务")
                    _state.update { st -> st.copy(loading = false, error = null) }
                } else {
                    // 用户取消
                    pushLog(-1, "INFO", "停止原因：用户取消")
                    _state.update { st -> st.copy(loading = false) }
                }
            } catch (e: Exception) {
                pushLog(-1, "ERR", "查询失败: ${e.message ?: e.javaClass.simpleName}")
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
        _state.value = FuzzyQueryState(threadCount = _state.value.threadCount).copy(
            workers = List(_state.value.threadCount) { WorkerState(it) }
        )
    }

    fun cancelSearch() {
        searchJob?.cancel()
        foundStudent.set(true)
        _state.update { it.copy(loading = false) }
        pushLog(-1, "INFO", "用户点击 STOP")
    }

    override fun onCleared() {
        cancelSearch()
        threadPoolExecutor.shutdown()
        super.onCleared()
    }
}