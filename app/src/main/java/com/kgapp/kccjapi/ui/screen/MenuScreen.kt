package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onExact: () -> Unit,
    onFuzzy: () -> Unit,
    onAbout: () -> Unit
) {
    // Hacker-ish palette (same as other screens)
    val bg = androidx.compose.ui.graphics.Color(0xFF070A0F)
    val panel = androidx.compose.ui.graphics.Color(0xFF0B1220)
    val border = androidx.compose.ui.graphics.Color(0xFF1B2A41)
    val glow = androidx.compose.ui.graphics.Color(0xFF00FF88)
    val textPrimary = androidx.compose.ui.graphics.Color(0xFFE6EEF8)
    val textMuted = androidx.compose.ui.graphics.Color(0xFF8CA0B3)

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TerminalHeader(
                title = "KCCJ SCORE PANEL",
                subtitle = "v0.1  •  secure mode",
                panel = panel,
                border = border,
                glow = glow,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            MenuCardHacker(
                title = "🎯 精确查询",
                desc = "输入姓名 + 学号，直接拉成绩列表",
                hint = "MODE: EXACT",
                onClick = onExact,
                panel = panel,
                border = border,
                glow = glow,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            MenuCardHacker(
                title = "🔎 模糊查询",
                desc = "输入姓名 + 学号范围，匹配可能学号",
                hint = "MODE: RANGE",
                onClick = onFuzzy,
                panel = panel,
                border = border,
                glow = glow,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            MenuCardHacker(
                title = "👤 关于作者",
                desc = "项目说明 / 免责声明 / 联系方式",
                hint = "INFO",
                onClick = onAbout,
                panel = panel,
                border = border,
                glow = glow,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TIP: 仅用于非法入侵",
                color = textMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun TerminalHeader(
    title: String,
    subtitle: String,
    panel: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Dot(color = androidx.compose.ui.graphics.Color(0xFFFF5F57))
                Dot(color = androidx.compose.ui.graphics.Color(0xFFFFBD2E))
                Dot(color = androidx.compose.ui.graphics.Color(0xFF28C840))

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "● ONLINE",
                    color = glow,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = textMuted,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "> select mode_",
                color = glow,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Spacer(
        modifier = Modifier
            .size(10.dp)
            .background(color, RoundedCornerShape(50))
    )
}

@Composable
private fun MenuCardHacker(
    title: String,
    desc: String,
    hint: String,
    onClick: () -> Unit,
    panel: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    glow: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color
) {
    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // ✅ 无 ripple / 无 remember / 无 interactionSource
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = hint,
                    color = glow,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = desc,
                color = textMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(border)
            )

            Text(
                text = "ENTER ↵",
                color = glow,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}