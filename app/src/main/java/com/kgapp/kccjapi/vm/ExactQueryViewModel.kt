package com.kgapp.kccjapi.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.Net
import com.kgapp.kccjapi.repo.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ExactUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<ScoreEntry> = emptyList()
)

data class ExactHistoryItem(
    val name: String,
    val num: String,
    val lastSuccessAt: Long
)

class ExactQueryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ScoreRepository(Net.api)

    private val _state = MutableStateFlow(ExactUiState())
    val state: StateFlow<ExactUiState> = _state

    private val _history = MutableStateFlow<List<ExactHistoryItem>>(emptyList())
    val history: StateFlow<List<ExactHistoryItem>> = _history

    private val prefs = app.getSharedPreferences("exact_query_history", Context.MODE_PRIVATE)

    init {
        _history.value = loadHistory()
    }

    fun search(name: String, num: String) {
        val n = name.trim()
        val s = num.trim()

        if (n.isBlank() || s.isBlank()) {
            _state.value = ExactUiState(error = "名字和学号都要填噢～")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, data = emptyList())

            val result = repo.exactQuery(n, s)
            _state.value = result.fold(
                onSuccess = { list ->
                    // ✅ 只有“查到数据”才记录历史
                    if (list.isNotEmpty()) {
                        addHistory(n, s)
                    }
                    ExactUiState(
                        loading = false,
                        error = if (list.isEmpty()) "没查到数据（可能名字/学号不匹配）" else null,
                        data = list
                    )
                },
                onFailure = { e ->
                    ExactUiState(
                        loading = false,
                        error = "请求失败：${e.message ?: e.javaClass.simpleName}",
                        data = emptyList()
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistory(_history.value)
    }

    fun removeHistory(item: ExactHistoryItem) {
        _history.value = _history.value.filterNot { it.name == item.name && it.num == item.num }
        saveHistory(_history.value)
    }

    // ====== history persistence ======

    private fun addHistory(name: String, num: String) {
        val now = System.currentTimeMillis()

        // 去重：同 name+num 更新 lastSuccessAt，并置顶
        val filtered = _history.value.filterNot { it.name == name && it.num == num }
        val updated = listOf(ExactHistoryItem(name, num, now)) + filtered

        // 只保留最近 20 条（你可以改）
        val limited = updated.take(20)

        _history.value = limited
        saveHistory(limited)
    }

    private fun saveHistory(list: List<ExactHistoryItem>) {
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
            o.put("name", it.name)
            o.put("num", it.num)
            o.put("t", it.lastSuccessAt)
            arr.put(o)
        }
        prefs.edit().putString("history_json", arr.toString()).apply()
    }

    private fun loadHistory(): List<ExactHistoryItem> {
        val json = prefs.getString("history_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val name = o.optString("name", "")
                    val num = o.optString("num", "")
                    val t = o.optLong("t", 0L)
                    if (name.isNotBlank() && num.isNotBlank()) {
                        add(ExactHistoryItem(name, num, t))
                    }
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}