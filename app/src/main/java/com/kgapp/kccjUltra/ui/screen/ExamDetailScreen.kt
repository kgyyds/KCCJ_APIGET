package com.kgapp.kccjUltra.ui.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kgapp.kccjUltra.data.model.StudentScoreUi
import com.kgapp.kccjUltra.ui.state.ExamDetailViewModel
import com.kgapp.kccjUltra.ui.theme.HackerGreen
import com.kgapp.kccjUltra.ui.theme.HackerTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    viewModel: ExamDetailViewModel,
    examName: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    val horizontalScroll = rememberScrollState()
    val headerBorder = remember(colorScheme.outline) {
        androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline)
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(examName.ifBlank { "考试详情" }, color = HackerGreen) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回", color = HackerGreen) }
                },
                actions = {
                    TextButton(onClick = viewModel::refresh) { Text("刷新", color = HackerGreen) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = HackerGreen,
                    navigationIconContentColor = HackerGreen,
                    actionIconContentColor = HackerGreen
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
            TableHeader(courses = uiState.detail.courses, scrollState = horizontalScroll, border = headerBorder)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.detail.students, key = { it.studentNum }) { student ->
                    StudentRow(
                        student = student,
                        courses = uiState.detail.courses,
                        scrollState = horizontalScroll
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader(
    courses: List<String>,
    scrollState: ScrollState,
    border: androidx.compose.foundation.BorderStroke
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(border, RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("姓名", style = MaterialTheme.typography.bodyMedium, color = HackerGreen)
            Text("学号", style = MaterialTheme.typography.bodyMedium, color = HackerGreen)
            Text("已查", style = MaterialTheme.typography.bodyMedium, color = HackerGreen)
        }
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.forEach { course ->
                Text(course, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun StudentRow(
    student: StudentScoreUi,
    courses: List<String>,
    scrollState: ScrollState
) {
    val outline = MaterialTheme.colorScheme.outline
    val border = remember(outline) { androidx.compose.foundation.BorderStroke(1.dp, outline) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(border, RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(student.studentName, style = MaterialTheme.typography.bodyMedium, color = HackerGreen)
            Text(student.studentNum, style = MaterialTheme.typography.bodySmall, color = HackerTextSecondary)
            Text("已查：${student.searched}", style = MaterialTheme.typography.bodySmall, color = HackerTextSecondary)
        }
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.forEachIndexed { index, _ ->
                Text(
                    student.scores.getOrNull(index) ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
