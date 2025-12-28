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
import com.kgapp.kccjapi.vm.ExactQueryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactQueryScreen(
    onBack: () -> Unit,
    vm: ExactQueryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Hacker-ish palette
    val bg = androidx.compose.ui.graphics.Color(0xFF070A0F)
    val panel = androidx.compose.ui.graphics.Color(0xFF0B1220)
    val border = androidx.compose.ui.graphics.Color(0xFF1B2A41)
    val glow = androidx.compose.ui.graphics.Color(0xFF00FF88)
    val textPrimary = androidx.compose.ui.graphics.Color(0xFFE6EEF8)
    val textMuted = androidx.compose.ui.graphics.Color(0xFF8CA0B3)

    var name by rememberSaveable { mutableStateOf("") }
    var num by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            confirmButton = { Button(onClick = { vm.clearError() }) { Text("好") } },
            title = { Text("提示") },
            text = { Text(state.error ?: "") }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Scaffold(
            containerColor = bg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "EXACT QUERY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Text("←", color = glow) } }
                )
            }
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
                            text = "> 输入姓名与学号进行精确检索",
                            color = glow,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "返回数据将按考试分组展示，方便阅读 📚",
                            color = textMuted,
                            style = MaterialTheme.typography.bodySmall
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

                // Empty / Loading / Results
                if (state.loading && state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = glow)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fetching…", color = textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                    return@Scaffold
                }

                val grouped = state.data.groupBy { it.examName ?: "（未知考试）" }

                if (!state.loading && state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⌁", color = glow, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Monospace)
                            Text("No data. 输入参数后点击 RUN。", color = textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
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

                            items(list, key = { e ->
                                "${e.studentNum}-${e.examName}-${e.course}-${e.score}-${e.searchTime}"
                            }) { entry ->
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
        v >= 60f -> androidx.compose.ui.graphics.Color(0xFF7DD3FC) // 偏冷蓝（可读性好）
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