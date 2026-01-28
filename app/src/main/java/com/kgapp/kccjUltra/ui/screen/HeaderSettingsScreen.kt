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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.kgapp.kccjUltra.ui.state.HeaderSettingsViewModel
import com.kgapp.kccjUltra.ui.theme.HackerGreen
import com.kgapp.kccjUltra.ui.theme.HackerText
import com.kgapp.kccjUltra.ui.theme.HackerTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSettingsScreen(
    viewModel: HeaderSettingsViewModel,
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

    val colorScheme = MaterialTheme.colorScheme
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HackerGreen,
        focusedLabelColor = HackerGreen,
        cursorColor = HackerGreen,
        unfocusedBorderColor = colorScheme.outline,
        unfocusedLabelColor = HackerTextSecondary,
        unfocusedTextColor = HackerText,
        focusedTextColor = HackerText
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("请求头设置", color = HackerGreen) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回", color = HackerGreen) }
                },
                actions = {
                    TextButton(onClick = viewModel::addHeader) { Text("新增", color = HackerGreen) }
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "所有请求都会带上这里设置的 Header。Key 不能为空。",
                style = MaterialTheme.typography.bodyMedium,
                color = HackerTextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.headers, key = { index, _ -> index }) { index, item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item.key,
                                onValueChange = { viewModel.updateHeader(index, key = it) },
                                label = { Text("Key") },
                                modifier = Modifier.weight(1f),
                                colors = textFieldColors
                            )
                            IconButton(onClick = { viewModel.deleteHeader(index) }) {
                                Text("删除", color = HackerGreen)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = item.value,
                            onValueChange = { viewModel.updateHeader(index, value = it) },
                            label = { Text("Value") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                    }
                }
            }
            Button(
                onClick = viewModel::saveHeaders,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(
                    if (uiState.isSaving) "保存中..." else "保存",
                    color = colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
