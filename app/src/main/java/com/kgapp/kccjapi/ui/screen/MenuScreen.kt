package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onExact: () -> Unit,
    onFuzzy: () -> Unit,
    onAbout: () -> Unit
) {
    // “黑客风”配色（不依赖主题文件，直接在页面里控）
    val bg = androidx.compose.ui.graphics.Color(0xFF070A0F)        // 深黑蓝
    val panel = androidx.compose.ui.graphics.Color(0xFF0B1220)     // 面板底
    val border = androidx.compose.ui.graphics.Color(0xFF1B2A41)    // 边框蓝灰
    val glow = androidx.compose.ui.graphics.Color(0xFF00FF88)      // 荧光绿
    val textPrimary = androidx.compose.ui.graphics.Color(0xFFE6EEF8)
    val textMuted = androidx.compose.ui.graphics.Color(0xFF8CA0B3)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 顶部“终端标题栏”
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
                desc = "输入姓名 + 学号，拉取成绩列表",
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
                desc = "输入姓名 + 学号范围，显示匹配记录",
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

            // 底部小脚注
            Text(
                text = "TIP: 仅用于学习与授权测试，注意保护隐私。",
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

            // 三个“窗口按钮”小圆点（终端感）
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

            // “光标行”
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
    Box(
        modifier = Modifier
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(PaddingValues(horizontal = 5.dp))
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
    val interaction = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() }
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

            // 底部“分隔线 + 小状态”
            Box(
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