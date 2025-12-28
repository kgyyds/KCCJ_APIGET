package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.vm.FuzzyQueryViewModel
import com.kgapp.kccjapi.vm.WorkerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuzzyQueryScreen(
    onBack: () -> Unit,
    viewModel: FuzzyQueryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Hacker-ish palette
    val bg = Color(0xFF070A0F)
    val panel = Color(0xFF0B1220)
    val border = Color(0xFF1B2A41)
    val glow = Color(0xFF00FF88)
    val textPrimary = Color(0xFFE6EEF8)
    val textMuted = Color(0xFF8CA0B3)
    val warning = Color(0xFFFFB74D)

    var name by rememberSaveable { mutableStateOf("") }
    var numRange by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }
    var threadCount by rememberSaveable { mutableStateOf(state.threadCount) }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { Button(onClick = { viewModel.clearError() }) { Text("确定") } },
            title = { Text("提示") },
            text = { Text(state.error ?: "") }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Scaffold(
            
                
            containerColor = bg
        ) { padding ->

            // ✅ 整页可滚动
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ===== 线程数设置 =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = panel),
                        border = BorderStroke(1.dp, border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("并发线程数", color = glow, fontFamily = FontFamily.Monospace)
                                Text(
                                    "${threadCount}线程",
                                    color = textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Slider(
                                value = threadCount.toFloat(),
                                onValueChange = {
                                    threadCount = it.toInt()
                                    viewModel.updateThreadCount(threadCount)
                                },
                                valueRange = 1f..32f,
                                steps = 31,
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = glow,
                                    activeTrackColor = glow,
                                    inactiveTrackColor = border
                                )
                            )

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("1", color = textMuted, fontFamily = FontFamily.Monospace)
                                Text("推荐: 4-8", color = warning, fontFamily = FontFamily.Monospace)
                                Text("32", color = textMuted, fontFamily = FontFamily.Monospace)
                            }

                            Text(
                                "提示：更多线程 = 更快查询，但会增加服务器压力",
                                color = textMuted,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // ===== 输入区 =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = panel),
                        border = BorderStroke(1.dp, border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "> 并发查询模式 - 找到结果即停止",
                                color = glow,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "range 示例：4112440401-4112440410",
                                color = textMuted,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )

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
                                value = numRange,
                                onValueChange = { numRange = it },
                                label = { Text("student_num_range", fontFamily = FontFamily.Monospace) },
                                placeholder = { Text("例如：4112440401-4112440410", color = textMuted) },
                                singleLine = true,
                                isError = touched && numRange.isBlank(),
                                supportingText = {
                                    if (numRange.isNotBlank() && numRange.split("-").size != 2) {
                                        Text("format: start-end", color = textMuted, fontFamily = FontFamily.Monospace)
                                    }
                                },
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
                                        viewModel.search(name, numRange)
                                    },
                                    enabled = !state.loading && name.isNotBlank() && numRange.isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = glow,
                                        contentColor = Color(0xFF04110A)
                                    )
                                ) {
                                    if (state.loading) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF04110A)
                                            )
                                            Spacer(Modifier.padding(horizontal = 6.dp))
                                            Text("RUNNING", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("RUN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (state.loading) {
                                    Button(
                                        onClick = { viewModel.cancelSearch() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF6B6B),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("STOP", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            name = ""
                                            numRange = ""
                                            touched = false
                                            viewModel.clearData()
                                        },
                                        enabled = !state.loading,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF111A2B),
                                            contentColor = textPrimary
                                        )
                                    ) {
                                        Text("CLEAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ===== 进度条 =====
                item {
                    state.progress?.let { (current, total) ->
                        val p = if (total > 0) current.toFloat() / total.toFloat() else 0f

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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = glow,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.padding(horizontal = 8.dp))
                                        Column {
                                            Text("并发扫描中…", color = textPrimary, fontFamily = FontFamily.Monospace)
                                            Text("使用 ${state.threadCount} 线程", color = textMuted, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                    Text(
                                        text = "${state.foundCount} hits",
                                        color = glow,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "进度: $current / $total (${(p * 100).toInt()}%)",
                                    color = textMuted,
                                    fontFamily = FontFamily.Monospace
                                )

                                LinearProgressIndicator(
                                    progress = { p },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (state.foundCount > 0) warning else glow
                                )

                                Text(
                                    text = "说明：找到结果后会自动停止所有查询线程",
                                    color = textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // ===== 线程状态面板 =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = panel),
                        border = BorderStroke(1.dp, border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "THREAD STATUS",
                                color = glow,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.workers.isEmpty()) {
                                Text("暂无线程", color = textMuted, fontFamily = FontFamily.Monospace)
                            } else {
                                state.workers.forEach { w ->
                                    val c = when (w.status) {
                                        WorkerStatus.RUNNING -> glow
                                        WorkerStatus.SUCCESS -> Color(0xFF7DD3FC)
                                        WorkerStatus.FAIL -> Color(0xFFFF6B6B)
                                        WorkerStatus.STOPPED -> textMuted
                                        WorkerStatus.IDLE -> textMuted
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "T#${w.id}",
                                            color = textPrimary,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = w.status.name,
                                            color = c,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "num=${w.currentNum ?: "-"} | ${w.lastMessage}",
                                        color = textMuted,
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }

                // ===== 实时日志面板 =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = panel),
                        border = BorderStroke(1.dp, border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "LIVE LOG (last ${state.logs.size})",
                                color = glow,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            if (state.logs.isEmpty()) {
                                Text("暂无日志", color = textMuted, fontFamily = FontFamily.Monospace)
                            } else {
                                // 小建议：日志太多时，这里仍然 OK，因为我们限制了 maxLogs=200
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    state.logs.takeLast(200).forEach { line ->
                                        val lc = when (line.level) {
                                            "OK" -> glow
                                            "ERR" -> Color(0xFFFF6B6B)
                                            else -> textMuted
                                        }
                                        val who = if (line.workerId >= 0) "T#${line.workerId}" else "SYS"
                                        Text(
                                            text = "[${line.ts}] [$who] ${line.level}: ${line.message}",
                                            color = lc,
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ===== 空提示 =====
                if (state.data.isEmpty() && !state.loading && touched) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = panel),
                            border = BorderStroke(1.dp, border),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⌁", color = glow, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Monospace)
                                Text("No data found.", color = textMuted, fontFamily = FontFamily.Monospace)
                                Text("请检查姓名和学号范围", color = textMuted, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // ===== 结果区 =====
                if (state.data.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = panel),
                            border = BorderStroke(1.dp, glow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "查询结果",
                                        color = glow,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "共 ${state.data.size} 条记录",
                                        color = textPrimary,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "扫描完成 ✅（已自动停止所有查询）",
                                    color = textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    val groupedByStudent = state.data.groupBy {
                        "${it.studentName ?: "未知"}-${it.studentNum ?: "未知"}"
                    }

                    items(
                        items = groupedByStudent.entries.toList(),
                        key = { it.key }
                    ) { (_, entries) ->
                        StudentScoreCardHacker(
                            entries = entries,
                            panel = panel,
                            border = border,
                            glow = glow,
                            textPrimary = textPrimary,
                            textMuted = textMuted
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ===== 原有结果卡片保持不变 =====

@Composable
private fun StudentScoreCardHacker(
    entries: List<ScoreEntry>,
    panel: Color,
    border: Color,
    glow: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val first = entries.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👤", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = first?.studentName ?: "未知姓名",
                        color = textPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!first?.studentNum.isNullOrBlank()) {
                        Text(
                            text = "num=${first?.studentNum}",
                            color = textMuted,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = "HIT",
                    color = glow,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            val groupedByExam = entries.groupBy { it.examName ?: "未知考试" }
            groupedByExam.forEach { (examName, examEntries) ->
                Text(
                    text = "📌 $examName",
                    color = glow,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                examEntries.forEach { entry ->
                    ScoreItemRowHacker(
                        entry = entry,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        glow = glow
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            first?.searchTime?.let { searchTime ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(border)
                        .height(1.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "t=$searchTime",
                    color = textMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ScoreItemRowHacker(
    entry: ScoreEntry,
    textPrimary: Color,
    textMuted: Color,
    glow: Color
) {
    val course = entry.course.orEmpty()
    val v = entry.score?.toFloatOrNull()
    val isRankLike = course.contains("排") || course.contains("排名")

    val scoreColor = when {
        v == null -> textMuted
        isRankLike -> textPrimary
        v >= 90f -> glow
        v >= 60f -> Color(0xFF7DD3FC)
        else -> Color(0xFFFF6B6B)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (course.isBlank()) "科目" else course,
            color = textPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = entry.score ?: "-",
            color = scoreColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}