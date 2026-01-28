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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
    examId: String,
    studentNum: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    val student = remember(uiState.detail.students, studentNum) {
        uiState.detail.students.firstOrNull { it.studentNum == studentNum }
    }
    val courses = uiState.detail.courses

    var dialogOpen by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedScore by remember { mutableStateOf("") }
    var newScore by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HackerGreen,
        focusedLabelColor = HackerGreen,
        cursorColor = HackerGreen,
        unfocusedBorderColor = colorScheme.outline,
        unfocusedLabelColor = HackerTextSecondary,
        unfocusedTextColor = colorScheme.onSurface,
        focusedTextColor = colorScheme.onSurface
    )

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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "${student?.studentName ?: "未知学生"} (${studentNum})",
                style = MaterialTheme.typography.titleMedium,
                color = HackerGreen
            )
            Text(
                text = "考试ID：$examId",
                style = MaterialTheme.typography.bodySmall,
                color = HackerTextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(courses, key = { index, course -> "$course-$index" }) { index, course ->
                    val score = student?.scores?.getOrNull(index) ?: "-"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(course, style = MaterialTheme.typography.bodyMedium, color = HackerGreen)
                            Text("分数：$score", style = MaterialTheme.typography.bodySmall, color = HackerTextSecondary)
                        }
                        Button(
                            onClick = {
                                selectedCourse = course
                                selectedScore = score
                                newScore = score
                                dialogOpen = true
                            },
                            enabled = student != null && !isUpdating
                        ) {
                            Text("修改", color = colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = {
                if (!isUpdating) {
                    dialogOpen = false
                }
            },
            title = { Text("修改成绩", color = HackerGreen) },
            text = {
                Column {
                    Text("科目：$selectedCourse", color = HackerTextSecondary)
                    Text("当前分数：$selectedScore", color = HackerTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newScore,
                        onValueChange = { newScore = it },
                        label = { Text("新分数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (student == null) return@TextButton
                        if (newScore.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("请输入分数")
                            }
                            return@TextButton
                        }
                        if (newScore.toIntOrNull() == null) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("分数必须是数字")
                            }
                            return@TextButton
                        }
                        coroutineScope.launch {
                            isUpdating = true
                            val success = viewModel.updateScore(
                                studentName = student.studentName,
                                studentNum = student.studentNum,
                                course = selectedCourse,
                                score = newScore
                            )
                            isUpdating = false
                            if (success) {
                                snackbarHostState.showSnackbar("修改成功")
                                dialogOpen = false
                            } else {
                                snackbarHostState.showSnackbar("修改失败")
                            }
                        }
                    },
                    enabled = !isUpdating
                ) {
                    Text(if (isUpdating) "提交中..." else "确定", color = HackerGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isUpdating) dialogOpen = false }) {
                    Text("取消", color = HackerTextSecondary)
                }
            },
            containerColor = colorScheme.surface
        )
    }
}
