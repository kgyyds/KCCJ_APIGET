package com.kgapp.kccjUltra.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.kgapp.kccjUltra.data.model.ExamUi
import com.kgapp.kccjUltra.ui.state.ExamListViewModel
import com.kgapp.kccjUltra.ui.theme.HackerGreen
import com.kgapp.kccjUltra.ui.theme.HackerTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    viewModel: ExamListViewModel,
    onExamClick: (ExamUi) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    val cardBorder = remember { androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline) }

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
                title = { Text("快查成绩Ultra", color = HackerGreen) },
                actions = {
                    TextButton(onClick = viewModel::refresh) { Text("刷新", color = HackerGreen) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = HackerGreen,
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
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                Text("加载中...", modifier = Modifier.padding(vertical = 12.dp), color = HackerTextSecondary)
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.exams, key = { it.id }) { exam ->
                    ExamCard(exam = exam, border = cardBorder, onClick = { onExamClick(exam) })
                }
            }
        }
    }
}

@Composable
private fun ExamCard(exam: ExamUi, border: androidx.compose.foundation.BorderStroke, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        border = border
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exam.title, style = MaterialTheme.typography.titleMedium, color = HackerGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("发布时间：${exam.pubTime}", color = HackerTextSecondary)
                    Text("考试ID：${exam.id}", color = HackerTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("成绩：${exam.grade}", color = HackerGreen)
                    Text("课程数：${exam.courseCount}", color = HackerTextSecondary)
                    Text("未读：${exam.unReadTotal}", color = HackerTextSecondary)
                }
            }
        }
    }
}
