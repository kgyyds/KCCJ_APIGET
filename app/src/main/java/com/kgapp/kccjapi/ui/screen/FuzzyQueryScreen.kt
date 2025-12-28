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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.vm.FuzzyQueryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuzzyQueryScreen(
    onBack: () -> Unit,
    viewModel: FuzzyQueryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Hacker-ish palette (same style as ExactQueryScreen)
    val bg = androidx.compose.ui.graphics.Color(0xFF070A0F)
    val panel = androidx.compose.ui.graphics.Color(0xFF0B1220)
    val border = androidx.compose.ui.graphics.Color(0xFF1B2A41)
    val glow = androidx.compose.ui.graphics.Color(0xFF00FF88)
    val textPrimary = androidx.compose.ui.graphics.Color(0xFFE6EEF8)
    val textMuted = androidx.compose.ui.graphics.Color(0xFF8CA0B3)

    var name by rememberSaveable { mutableStateOf("") }
    var numRange by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }

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
            containerColor = bg,
            
        ) { padding ->

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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "> 输入姓名 + 学号范围进行匹配",
                            color = glow,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "range 示例：4112440401-4112440410",
                            color = textMuted,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

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
                                enabled = !state.loading,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = glow,
                                    contentColor = androidx.compose.ui.graphics.Color(0xFF04110A)
                                )
                            ) {
                                if (state.loading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = androidx.compose.ui.graphics.Color(0xFF04110A)
                                        )
                                        Spacer(Modifier.padding(horizontal = 6.dp))
                                        Text("RUNNING", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("RUN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }

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
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF111A2B),
                                    contentColor = textPrimary
                                )
                            ) {
                                Text("CLEAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Progress panel
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = glow, strokeWidth = 2.dp)
                                Text(
                                    text = "Scanning…",
                                    color = textPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${state.data.size} hits",
                                    color = glow,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "progress: $current / $total",
                                color = textMuted,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )

                            // ✅ 用 lambda 版本，兼容 Material3 不同版本签名
                            LinearProgressIndicator(
                                progress = { p },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Empty / Results
                if (state.data.isEmpty() && !state.loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⌁", color = glow, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "No data. 输入参数后点击 RUN。",
                                color = textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    val groupedByStudent = state.data.groupBy {
                        "${it.studentName ?: "未知"}-${it.studentNum ?: "未知"}"
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedByStudent.forEach { (key, entries) ->
                            item(key = "student:$key") {
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
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentScoreCardHacker(
    entries: List<ScoreEntry>,
    panel: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color
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

            // Student header
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
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color
) {
    val course = entry.course.orEmpty()
    val v = entry.score?.toFloatOrNull()
    val isRankLike = course.contains("排") || course.contains("排名")

    val scoreColor = when {
        v == null -> textMuted
        isRankLike -> textPrimary
        v >= 90f -> glow
        v >= 60f -> androidx.compose.ui.graphics.Color(0xFF7DD3FC)
        else -> androidx.compose.ui.graphics.Color(0xFFFF6B6B)
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