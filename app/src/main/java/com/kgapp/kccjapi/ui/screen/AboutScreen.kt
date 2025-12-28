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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    // Same palette as other screens
    val bg = androidx.compose.ui.graphics.Color(0xFF070A0F)
    val panel = androidx.compose.ui.graphics.Color(0xFF0B1220)
    val border = androidx.compose.ui.graphics.Color(0xFF1B2A41)
    val glow = androidx.compose.ui.graphics.Color(0xFF00FF88)
    val textPrimary = androidx.compose.ui.graphics.Color(0xFFE6EEF8)
    val textMuted = androidx.compose.ui.graphics.Color(0xFF8CA0B3)

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Scaffold(
            containerColor = bg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "ABOUT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Text("←", color = glow) }
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

                // Terminal header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = panel),
                    border = BorderStroke(1.dp, border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "> author_profile",
                            color = glow,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "项目说明 / 联系方式 / 免责声明",
                            color = textMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = panel),
                    border = BorderStroke(1.dp, border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                            Text(
                                text = "AUTHOR INFO",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(border)
                                .height(1.dp)
                        )

                        // You can edit these lines freely
                        MonoLine(label = "nickname", value = "（填写你的昵称）", glow = glow, textPrimary = textPrimary, textMuted = textMuted)
                        MonoLine(label = "contact", value = "（邮箱/QQ/Telegram 等）", glow = glow, textPrimary = textPrimary, textMuted = textMuted)
                        MonoLine(label = "repo", value = "git@github.com:kgyyds/KCCJ_APIGET.git", glow = glow, textPrimary = textPrimary, textMuted = textMuted)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "DISCLAIMER",
                            color = glow,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "本工具仅用于学习与授权查询。请勿用于未授权的查询/扫描行为；\n数据仅作演示，注意保护个人隐私。",
                            color = textMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Bottom button (optional, looks cool)
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF111A2B),
                        contentColor = textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("BACK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MonoLine(
    label: String,
    value: String,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = glow,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = textPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
    Text(
        text = "—",
        color = textMuted,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall
    )
}