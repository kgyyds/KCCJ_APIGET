package com.kgapp.kccjapi.ui.screen

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
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

    var name by rememberSaveable { mutableStateOf("") }
    var num by rememberSaveable { mutableStateOf("") }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            confirmButton = {
                Button(onClick = { vm.clearError() }) { Text("好") }
            },
            title = { Text("提示") },
            text = { Text(state.error ?: "") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("精确查询") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("学生姓名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = num,
                onValueChange = { num = it },
                label = { Text("学生学号") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { vm.search(name, num) },
                    enabled = !state.loading
                ) {
                    Text(if (state.loading) "查询中…" else "开始查询")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 展示：按考试名分组更好读
            val grouped = state.data.groupBy { it.examName ?: "（未知考试）" }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (exam, list) ->
                    item {
                        Text(
                            text = "📌 $exam",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (list.firstOrNull()?.pubDate != null) {
                            Text(
                                text = "日期：${list.firstOrNull()?.pubDate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    items(list) { entry ->
                        ScoreRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(e: ScoreEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${e.course ?: "科目"}：${e.score ?: "-"}",
                style = MaterialTheme.typography.titleMedium
            )
            val meta = buildString {
                if (!e.studentName.isNullOrBlank()) append("👤 ${e.studentName}  ")
                if (!e.studentNum.isNullOrBlank()) append("🆔 ${e.studentNum}  ")
                if (!e.searchTime.isNullOrBlank()) append("🕒 ${e.searchTime}")
            }
            if (meta.isNotBlank()) {
                Text(text = meta, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}