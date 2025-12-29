package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.vm.ExactHistoryItem
import com.kgapp.kccjapi.vm.ExactQueryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactQueryScreen(
    onBack: () -> Unit,
    vm: ExactQueryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val history by vm.history.collectAsState()

    // Hacker-ish palette
    val bg = androidx.compose.ui.graphics.Color(0xFF070A0F)
    val panel = androidx.compose.ui.graphics.Color(0xFF0B1220)
    val border = androidx.compose.ui.graphics.Color(0xFF1B2A41)
    val glow = androidx.compose.ui.graphics.Color(0xFF00FF88)
    val textPrimary = androidx.compose.ui.graphics.Color(0xFFE6EEF8)
    val textMuted = androidx.compose.ui.graphics.Color(0xFF8CA0B3)
    val danger = androidx.compose.ui.graphics.Color(0xFFFF6B6B)

    var name by rememberSaveable { mutableStateOf("") }
    var num by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }
    var historyExpanded by rememberSaveable { mutableStateOf(false) }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            confirmButton = { Button(onClick = { vm.clearError() }) { Text("好") } },
            title = { Text("提示") },
            text = { Text(state.error ?: "") }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Scaffold(containerColor = bg) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Header panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = panel),
                    border = BorderStroke(1.dp, border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "> 输入姓名与学号进行精确检索",
                            color = glow,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "成功查询会自动写入 History，点一下就能回填 🧠",
                            color = textMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // History (collapsible)
                HistoryCard(
                    expanded = historyExpanded,
                    onToggle = { historyExpanded = !historyExpanded },
                    history = history,
                    panel = panel,
                    border = border,
                    glow = glow,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    danger = danger,
                    onPick = { item ->
                        name = item.name
                        num = item.num
                        touched = false
                    },
                    onClear = { vm.clearHistory() },
                    onRemove = { vm.removeHistory(it) }
                )

                // Input panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = panel),
                    border = BorderStroke(1.dp, border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("student_name", fontFamily = FontFamily.Monospace) },
                            placeholder = { Text("例如：张三", color = textMuted) },
                            singleLine = true,
                            isError = touched && name.isBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = glow,
                                unfocusedBorderColor = border,
                                focusedLabelColor = glow,
                                unfocusedLabelColor = textMuted,
                                cursorColor = glow,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            )
                        )

                        OutlinedTextField(
                            value = num,
                            onValueChange = { num = it },
                            label = { Text("student_num", fontFamily = FontFamily.Monospace) },
                            placeholder = { Text("例如：4112440406", color = textMuted) },
                            singleLine = true,
                            isError = touched && num.isBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = glow,
                                unfocusedBorderColor = border,
                                focusedLabelColor = glow,
                                unfocusedLabelColor = textMuted,
                                cursorColor = glow,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    touched = true
                                    vm.search(name, num)
                                },
                                enabled = !state.loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = glow,
                                    contentColor = androidx.compose.ui.graphics.Color(0xFF04110A)
                                )
                            ) {
                                if (state.loading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.height(16.dp),
                                            strokeWidth = 2.dp,
                                            color = androidx.compose.ui.graphics.Color(0xFF04110A)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "RUNNING",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Text("RUN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    name = ""
                                    num = ""
                                    touched = false
                                },
                                enabled = !state.loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF111A2B),
                                    contentColor = textPrimary
                                )
                            ) {
                                Text("CLEAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ===== Results area: NO return, only branches =====
                val grouped = state.data.groupBy { it.examName ?: "（未知考试）" }

                when {
                    state.loading && state.data.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = glow)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Fetching…", color = textMuted, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    !state.loading && state.data.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "⌁",
                                    color = glow,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    "No data. 输入参数后点击 RUN。",
                                    color = textMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            grouped.forEach { (exam, list) ->
                                item(key = "header:$exam") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = panel),
                                        border = BorderStroke(1.dp, border),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "📌 $exam",
                                                color = textPrimary,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val date = list.firstOrNull()?.pubDate
                                            if (!date.isNullOrBlank()) {
                                                Text(
                                                    text = "date: $date",
                                                    color = textMuted,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }

                                items(
                                    items = list,
                                    key = { e ->
                                        "${e.studentNum}-${e.examName}-${e.course}-${e.score}-${e.searchTime}"
                                    }
                                ) { entry ->
                                    ScoreRowHacker(
                                        e = entry,
                                        panel = panel,
                                        border = border,
                                        glow = glow,
                                        textPrimary = textPrimary,
                                        textMuted = textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    history: List<ExactHistoryItem>,
    panel: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    danger: androidx.compose.ui.graphics.Color,
    onPick: (ExactHistoryItem) -> Unit,
    onClear: () -> Unit,
    onRemove: (ExactHistoryItem) -> Unit
) {
    val mono = FontFamily.Monospace

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "▼ HISTORY" else "▶ HISTORY",
                    color = glow,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (history.isNotEmpty()) {
                    Text(
                        text = "CLEAR",
                        color = danger,
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onClear() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // ✅ 不使用 return，改为分支渲染
            if (!expanded) {
                Text(
                    text = if (history.isEmpty()) "暂无记录（成功查询后会自动出现）" else "最近 ${history.size} 条（点开可回填）",
                    color = textMuted,
                    fontFamily = mono,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                if (history.isEmpty()) {
                    Text(
                        text = "暂无记录。先 RUN 一次成功的查询再来～",
                        color = textMuted,
                        fontFamily = mono
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEach { item ->
                            HistoryRow(
                                item = item,
                                panel = panel,
                                border = border,
                                glow = glow,
                                textPrimary = textPrimary,
                                textMuted = textMuted,
                                danger = danger,
                                onPick = onPick,
                                onRemove = onRemove
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: ExactHistoryItem,
    panel: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    danger: androidx.compose.ui.graphics.Color,
    onPick: (ExactHistoryItem) -> Unit,
    onRemove: (ExactHistoryItem) -> Unit
) {
    val mono = FontFamily.Monospace
    val time = formatTime(item.lastSuccessAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPick(item) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.name}  #${item.num}",
                    color = textPrimary,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "DEL",
                    color = danger,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onRemove(item) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = "last_ok: $time",
                color = textMuted,
                fontFamily = mono,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        fmt.format(Date(ms))
    } catch (_: Throwable) {
        "-"
    }
}

@Composable
private fun ScoreRowHacker(
    e: ScoreEntry,
    panel: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color
) {
    val course = e.course.orEmpty()
    val v = e.score?.toFloatOrNull()
    val isRankLike = course.contains("排") || course.contains("排名")

    val scoreColor = when {
        v == null -> textMuted
        isRankLike -> textPrimary
        v >= 90f -> glow
        v >= 60f -> androidx.compose.ui.graphics.Color(0xFF7DD3FC)
        else -> androidx.compose.ui.graphics.Color(0xFFFF6B6B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = course.ifBlank { "科目" },
                    color = textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = e.score ?: "-",
                    color = scoreColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            val meta = buildString {
                if (!e.studentName.isNullOrBlank()) append("name=${e.studentName}  ")
                if (!e.studentNum.isNullOrBlank()) append("num=${e.studentNum}  ")
                if (!e.searchTime.isNullOrBlank()) append("t=${e.searchTime}")
            }

            if (meta.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(border)
                        .height(1.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta,
                    color = textMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}