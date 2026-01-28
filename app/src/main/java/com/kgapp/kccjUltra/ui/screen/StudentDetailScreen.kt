package com.kgapp.kccjUltra.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kgapp.kccjUltra.ui.state.ExamDetailViewModel
import com.kgapp.kccjUltra.ui.theme.HackerGreen
import com.kgapp.kccjUltra.ui.theme.HackerTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    viewModel: ExamDetailViewModel,
    studentNum: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.detail.students.firstOrNull { it.studentNum == studentNum }
    val courses = uiState.detail.courses
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var inputScore by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    if (editingIndex != null && student != null) {
        val courseName = courses.getOrNull(editingIndex!!) ?: ""
        val currentScore = student.scores.getOrNull(editingIndex!!) ?: "-"
        AlertDialog(
            onDismissRequest = {
                if (!isUpdating) {
                    editingIndex = null
                }
            },
            title = { Text("修改成绩", color = HackerGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("科目：$courseName", color = colorScheme.onSurface)
                    Text("当前分数：$currentScore", color = HackerTextSecondary)
                    OutlinedTextField(
                        value = inputScore,
                        onValueChange = { value ->
                            inputScore = value
                            inputError = null
                        },
                        label = { Text("新分数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isUpdating
                    )
                    if (!inputError.isNullOrBlank()) {
                        Text(inputError ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = inputScore.trim()
                        val numeric = trimmed.toIntOrNull()
                        if (numeric == null || numeric !in 0..150) {
                            inputError = "请输入 0-150 的数字分数"
                            return@TextButton
                        }
                        isUpdating = true
                        coroutineScope.launch {
                            val success = viewModel.updateExamScore(
                                studentName = student.studentName,
                                studentNum = student.studentNum,
                                course = courseName,
                                score = trimmed
                            )
                            isUpdating = false
                            if (success) {
                                snackbarHostState.showSnackbar("修改成功")
                                editingIndex = null
                                viewModel.refresh()
                            } else {
                                snackbarHostState.showSnackbar("修改失败")
                            }
                        }
                    },
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("确定", color = HackerGreen)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingIndex = null },
                    enabled = !isUpdating
                ) {
                    Text("取消", color = HackerTextSecondary)
                }
            },
            containerColor = colorScheme.background
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学生详情", color = HackerGreen) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回", color = HackerGreen) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = HackerGreen,
                    navigationIconContentColor = HackerGreen
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            if (uiState.isLoading) {
                Text("加载中...", modifier = Modifier.padding(vertical = 12.dp), color = HackerTextSecondary)
            }
            if (student == null) {
                Text("未找到学生信息", color = HackerTextSecondary)
                return@Column
            }
            Text(
                "${student.studentName} (${student.studentNum})",
                style = MaterialTheme.typography.titleMedium,
                color = HackerGreen
            )
            Text("考试ID：${uiState.examId}", color = HackerTextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(courses, key = { index, _ -> index }) { index, course ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = course,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = student.scores.getOrNull(index) ?: "-",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = {
                                editingIndex = index
                                inputScore = student.scores.getOrNull(index)?.takeIf { it != "-" } ?: ""
                                inputError = null
                            }
                        ) {
                            Text("修改", color = HackerGreen)
                        }
                    }
                }
            }
        }
    }
}
