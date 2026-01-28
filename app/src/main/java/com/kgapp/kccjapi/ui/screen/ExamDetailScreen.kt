package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.kgapp.kccjapi.data.model.Course
import com.kgapp.kccjapi.ui.state.ExamDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    viewModel: ExamDetailViewModel,
    examName: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text(examName.ifBlank { "考试详情" }) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                },
                actions = {
                    TextButton(onClick = viewModel::refresh) { Text("刷新") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            if (uiState.isLoading) {
                Text("加载中...", modifier = Modifier.padding(vertical = 12.dp))
            }
            val horizontalScroll = rememberScrollState()
            TableHeader(courses = uiState.courses, scrollState = horizontalScroll)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.students.forEach { student ->
                    StudentRow(
                        student = student,
                        courses = uiState.courses,
                        scrollState = horizontalScroll
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader(courses: List<Course>, scrollState: ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("姓名", style = MaterialTheme.typography.bodyMedium)
            Text("学号", style = MaterialTheme.typography.bodyMedium)
            Text("已查", style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.forEach { course ->
                Text(course.course, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StudentRow(
    student: JsonObject,
    courses: List<Course>,
    scrollState: ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(student.stringValue("student_name"), style = MaterialTheme.typography.bodyMedium)
            Text(student.stringValue("student_num"), style = MaterialTheme.typography.bodySmall)
            Text(student.stringValue("searched"), style = MaterialTheme.typography.bodySmall)
        }
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.forEach { course ->
                Text(
                    student.stringValue(course.course),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun JsonObject.stringValue(key: String): String {
    return if (has(key) && !get(key).isJsonNull) {
        get(key).asString
    } else {
        "-"
    }
}
