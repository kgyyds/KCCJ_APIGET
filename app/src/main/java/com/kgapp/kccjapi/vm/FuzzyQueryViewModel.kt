package com.kgapp.kccjapi.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
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
import java.util.concurrent.atomic.AtomicBoolean

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
    val level: String,
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

    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val maxLogs = 200

    // ✅ 每个 worker 的 UI 更新节流时间（越大越快）
    private val workerUiIntervalMs = 80L

    // ✅ 进度更新步长（越大越快）
    private val progressStep = 50

    private fun nowTs(): String = timeFmt.format(Date())

    private fun pushLog(workerId: Int, level: String, msg: String) {
        _state.update { st ->
            val newList = (st.logs + LogLine(nowTs(), workerId, level, msg))
            st.copy(logs = if (newList.size > maxLogs) newList.takeLast(maxLogs) else newList)
        }
    }

    private fun initWorkers(count: Int) {
        _state.update { st ->
            st.copy(workers = List(count) { idx -> WorkerState(id = idx) })
        }
    }

    private fun setWorkerFast(
        workerId: Int,
        status: WorkerStatus? = null,
        currentNum: Long? = null,
        lastMessage: String? = null
    ) {
        _state.update { st ->
            if (workerId !in st.workers.indices) return@update st
            val list = st.workers.toMutableList()
            val old = list[workerId]
            list[workerId] = old.copy(
                status = status ?: old.status,
                currentNum = currentNum ?: old.currentNum,
                lastMessage = lastMessage ?: old.lastMessage
            )
            st.copy(workers = list)
        }
    }

    fun updateThreadCount(count: Int) {
        if (count < 1 || count > 256) return
        cancelSearch()
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

        // ✅ IO 并发：比自建线程池更适合网络任务
        val io = Dispatchers.IO.limitedParallelism(tc)

        searchJob = viewModelScope.launch {
            val allResults = mutableListOf<ScoreEntry>()

            // 🚀 容量拉大：吞吐更高
            val numberChannel = Channel<Long>(capacity = tc * 64)

            try {
                supervisorScope {

                    val workers = List(tc) { workerId ->
                        launch(io) {
                            pushLog(workerId, "INFO", "worker online")

                            var lastUiUpdate = 0L

                            while (isActive) {
                                val num = numberChannel.receiveCatching().getOrNull() ?: break
                                if (foundStudent.get()) break

                                // ✅ worker UI 节流
                                val now = System.currentTimeMillis()
                                if (now - lastUiUpdate >= workerUiIntervalMs) {
                                    lastUiUpdate = now
                                    setWorkerFast(workerId, WorkerStatus.RUNNING, num, "running")
                                }

                                val list: List<ScoreEntry>? = try {
                                    repo.exactQuery(name, num.toString()).getOrNull()
                                } catch (t: Throwable) {
                                    // 失败也不要狂刷 UI/日志（只记一条）
                                    setWorkerFast(workerId, WorkerStatus.FAIL, num, "fail")
                                    pushLog(workerId, "ERR", "num=$num ${t.message ?: t.javaClass.simpleName}")
                                    null
                                }

                                if (!list.isNullOrEmpty()) {
                                    if (foundStudent.compareAndSet(false, true)) {
                                        setWorkerFast(workerId, WorkerStatus.SUCCESS, num, "HIT(${list.size})")
                                        pushLog(workerId, "OK", "num=$num 命中 ${list.size} 条 ✅")

                                        allResults.addAll(list)

                                        val distinct = allResults.distinctBy { e ->
                                            "${e.studentNum}-${e.examName}-${e.course}-${e.score}"
                                        }

                                        // ✅ 立即展示结果
                                        _state.update { st ->
                                            st.copy(
                                                loading = false,
                                                error = null,
                                                data = distinct,
                                                foundCount = distinct.size
                                            )
                                        }

                                        // ✅ 立刻停机
                                        numberChannel.close()
                                        this@supervisorScope.cancel(CancellationException("FOUND_RESULT"))
                                    }
                                }
                            }

                            setWorkerFast(workerId, WorkerStatus.STOPPED, lastMessage = if (foundStudent.get()) "stopped" else "done")
                        }
                    }

                    // producer：全速，不 delay
                    launch {
                        var current = 0
                        for (num in start..end) {
                            if (!isActive) break
                            if (foundStudent.get()) break

                            current++

                            // ✅ 进度节流：每 50 个更新一次
                            if (current % progressStep == 0 || current == total) {
                                _state.update { it.copy(progress = current to total) }
                            }

                            numberChannel.send(num)
                        }
                        _state.update { it.copy(progress = total to total) }
                        numberChannel.close()
                    }

                    workers.forEach { it.join() }
                }

                if (!foundStudent.get()) {
                    pushLog(-1, "INFO", "扫描结束：未命中")
                    _state.update { it.copy(loading = false, error = "未找到匹配结果") }
                } else {
                    pushLog(-1, "OK", "已命中，全部线程已停止")
                }

            } catch (e: CancellationException) {
                // ✅ 不当失败
                if (foundStudent.get()) {
                    _state.update { it.copy(loading = false, error = null) }
                } else {
                    _state.update { it.copy(loading = false) }
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
        pushLog(-1, "INFO", "用户点击 STOP")
    }

    override fun onCleared() {
        cancelSearch()
        super.onCleared()
    }
}