package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onExact: () -> Unit,
    onFuzzy: () -> Unit,
    onAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("KCCJ 成绩查询", style = MaterialTheme.typography.headlineMedium)
        Text("主菜单：选一个功能开干～😺", style = MaterialTheme.typography.bodyMedium)

        MenuCard(title = "🎯 精确查询", desc = "输入姓名 + 学号，直接拉成绩列表") { onExact() }
        MenuCard(title = "🔎 模糊查询", desc = "1111") { onFuzzy() }
        MenuCard(title = "👤 关于作者", desc = "看看作者是谁（别害羞）") { onAbout() }
    }
}

@Composable
private fun MenuCard(
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(desc, style = MaterialTheme.typography.bodyMedium)
        }
    }
}