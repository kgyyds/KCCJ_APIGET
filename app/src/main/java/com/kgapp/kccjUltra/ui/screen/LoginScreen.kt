package com.kgapp.kccjUltra.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import com.kgapp.kccjUltra.ui.state.LoginEvent
import com.kgapp.kccjUltra.ui.state.LoginViewModel
import com.kgapp.kccjUltra.ui.theme.HackerGreen
import com.kgapp.kccjUltra.ui.theme.HackerText
import com.kgapp.kccjUltra.ui.theme.HackerTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showHackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is LoginEvent.Success) {
                onLoginSuccess(event.username)
            }
        }
    }

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
                title = { Text("快查成绩Ultra", color = HackerGreen) },
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("快查成绩Ultra", style = MaterialTheme.typography.headlineMedium, color = HackerGreen)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("手机号") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("J_session_BASE62安全码") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (uiState.username.trim().isBlank() || uiState.password.trim().isBlank()) {
                        viewModel.login()
                    } else {
                        showHackDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !showHackDialog
            ) {
                Text(
                    when {
                        uiState.isLoading -> "登录中..."
                        showHackDialog -> "安全通道建立中..."
                        else -> "登录"
                    },
                    color = colorScheme.onPrimary
                )
            }
        }
    }

    HackLoginDialog(
        visible = showHackDialog,
        onFinish = {
            showHackDialog = false
            viewModel.login()
        }
    )
}

@Composable
private fun HackLoginDialog(
    visible: Boolean,
    onFinish: () -> Unit
) {
    if (!visible) return

    val logs = remember { mutableStateListOf<String>() }
    var progressPercent by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val onFinishState = rememberUpdatedState(onFinish)

    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor-alpha"
    )

    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        logs.clear()
        progressPercent = 0
        val totalDuration = 3000L
        val messages = listOf(
            "初始化安全上下文…",
            "处理数据中…",
            "解析数据中…",
            "通知协程中…",
            "协程等待中…",
            "建立加密通道…",
            "服务器验签中…",
            "校验会话令牌…",
            "同步考试索引…",
            "加载完成 ✅"
        )
        val stepDelay = (totalDuration / (messages.size + 1))
        messages.forEachIndexed { index, message ->
            delay(stepDelay)
            logs.add(message)
            progressPercent = (((index + 1) * 100f) / messages.size).toInt()
        }
        val elapsed = stepDelay * messages.size
        val remaining = totalDuration - elapsed
        if (remaining > 0) {
            delay(remaining)
        }
        onFinishState.value.invoke()
    }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF0A0A0A),
            border = BorderStroke(1.5.dp, Color(0xFF00E676))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "快查成绩Ultra · 安全通道建立中",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00E676),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        color = Color(0xFF00E676),
                        trackColor = Color(0xFF13311F),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "进度 ${progressPercent}%",
                        color = Color(0xFF00E676),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFF0B0F0E))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs.size) { index ->
                            Text(
                                text = logs[index],
                                color = Color(0xFF00E676),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "root@ultra:~$",
                        color = Color(0xFF00E676),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(16.dp)
                            .background(Color(0xFF00E676).copy(alpha = cursorAlpha))
                    )
                }
            }
        }
    }
}
