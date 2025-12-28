package com.kgapp.kccjapi.ui.screen

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.vm.FuzzyQueryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuzzyQueryScreen(
    onBack: () -> Unit,
    // 直接使用viewModel()，去掉hilt相关
    viewModel: FuzzyQueryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var numRange by rememberSaveable { mutableStateOf("") }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) { Text("确定") }
            },
            title = { Text("提示") },
            text = { Text(state.error ?: "") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模糊查询") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
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
            // 查询条件输入区
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "查询条件",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("学生姓名 *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("请输入学生姓名") },
                        isError = name.isBlank()
                    )

                    OutlinedTextField(
                        value = numRange,
                        onValueChange = { numRange = it },
                        label = { Text("学号范围 (可选)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("格式：42000-42999") },
                        supportingText = {
                            if (numRange.isNotBlank() && numRange.split("-").size != 2) {
                                Text("格式错误，应为 起始-结束")
                            }
                        }
                    )
                    
                    Text(
                        text = "说明：遍历指定学号范围，用固定姓名匹配",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // 进度显示
            state.progress?.let { (current, total) ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "正在查询...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "进度: $current / $total",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LinearProgressIndicator(
                            progress = current.toFloat() / total.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "已找到 ${state.data.size} 条记录",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 按钮区
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.search(name, numRange) },
                    enabled = !state.loading && name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.loading) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Text("开始查询")
                    }
                }

                Button(
                    onClick = { 
                        name = ""
                        numRange = ""
                        viewModel.clearData()
                    },
                    enabled = !state.loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
            }

            // 结果显示区标题
            if (state.data.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "查询结果",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "共 ${state.data.size} 条",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 结果列表
            if (state.data.isEmpty() && !state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👋",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = if (name.isBlank() && numRange.isBlank()) 
                                "请输入查询条件" 
                            else 
                                "暂无数据",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 先按学生分组（虽然姓名相同，但学号可能不同）
                    val groupedByStudent = state.data.groupBy { 
                        "${it.studentName ?: "未知"}-${it.studentNum ?: "未知"}" 
                    }
                    
                    groupedByStudent.forEach { (_, entries) ->
                        item {
                            StudentScoreCard(entries)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentScoreCard(entries: List<ScoreEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 学生基本信息
            val firstEntry = entries.firstOrNull()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "👤",
                    style = MaterialTheme.typography.bodyLarge
                )
                Column {
                    Text(
                        text = firstEntry?.studentName ?: "未知姓名",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!firstEntry?.studentNum.isNullOrBlank()) {
                        Text(
                            text = "学号: ${firstEntry?.studentNum}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 按考试分组显示成绩
            val groupedByExam = entries.groupBy { it.examName ?: "未知考试" }
            
            groupedByExam.forEach { (examName, examEntries) ->
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "📝 $examName",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // 显示该考试的所有科目成绩
                    examEntries.forEach { entry ->
                        ScoreItemRow(entry)
                    }
                }
            }
            
            // 显示查询时间（如果有）
            firstEntry?.searchTime?.let { searchTime ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "查询时间: $searchTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ScoreItemRow(entry: ScoreEntry) {
    val scoreColor = when {
        entry.score == null -> MaterialTheme.colorScheme.outline
        else -> {
            val scoreValue = entry.score.toFloatOrNull()
            when {
                scoreValue == null -> MaterialTheme.colorScheme.outline
                scoreValue >= 90f -> MaterialTheme.colorScheme.primary
                scoreValue >= 60f -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.course ?: "未知科目",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = entry.score ?: "-",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor
        )
    }
}