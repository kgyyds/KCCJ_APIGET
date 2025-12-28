package com.kgapp.kccjapi.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuzzyQueryScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模糊查询") },
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
                .padding(16.dp)
        ) {
            Text("这里我先实话实说一下 🧠")
            Text(
                "“遍历学号范围 + 固定姓名匹配”属于枚举式查询，容易造成他人成绩隐私泄露。\n" +
                    "所以我不会提供可用的撞库/遍历实现。",
                modifier = Modifier.padding(top = 10.dp)
            )

            Text(
                "\n✅ 合规替代方案（我可以继续帮你做）：\n" +
                    "1) 你自己有学校授权的“名单/学号表”（CSV/JSON），App 本地匹配姓名↔学号，然后只做精确查询。\n" +
                    "2) 让后端提供“按班级/授权范围批量查询”的正式接口（带鉴权/权限控制）。\n" +
                    "3) 只支持学号前缀/后缀搜索（如果官方接口本来就支持）。",
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}