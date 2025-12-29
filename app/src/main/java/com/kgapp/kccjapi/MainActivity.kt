
/*
package com.kgapp.kccjapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.activity.enableEdgeToEdge
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    AppNav()
                }
            }
        }
    }
}*/

package com.kgapp.kccjapi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kgapp.kccjapi.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val updateClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .writeTimeout(4, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface {
                    ForcedUpdateGate(
                        currentVersion = BuildConfig.VERSION_NAME,
                        onOpenUpdate = { url ->
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Throwable) {
                            }
                            finish()
                        },
                        content = { AppNav() }
                    )
                }
            }
        }
    }

    @Composable
    private fun ForcedUpdateGate(
        currentVersion: String,
        onOpenUpdate: (String) -> Unit,
        content: @Composable () -> Unit
    ) {
        var state by remember {
            mutableStateOf<UpdateState>(UpdateState.Checking("正在检查更新…"))
        }

        LaunchedEffect(Unit) {
            state = UpdateState.Checking("正在检查更新…")
            state = checkUpdate(currentVersion)
        }

        when (val s = state) {
            is UpdateState.Pass -> content()

            is UpdateState.Checking -> BlockingDialog(
                title = "检查更新",
                message = s.message,
                buttons = {}
            )

            is UpdateState.NeedUpdate -> {
                BlockingDialog(
                    title = "发现新版本 ${s.latestTag}",
                    message = "当前版本：$currentVersion\n最新版本：${s.latestTag}\n必须更新后才能继续使用。",
                    buttons = {
                        TextButton(onClick = { state = UpdateState.Checking("正在重试…") }) { Text("重试") }
                        SpacerButton()
                        Button(onClick = { onOpenUpdate(s.url) }) { Text("去更新") }
                    }
                )

                LaunchedEffect(state) {
                    if (state is UpdateState.Checking) {
                        state = checkUpdate(currentVersion)
                    }
                }
            }

            is UpdateState.CheckFailed -> {
                BlockingDialog(
                    title = "无法检查更新",
                    message = "当前网络无法连接更新服务。\n请检查网络后重试。\n\n原因：${s.reason}",
                    buttons = {
                        TextButton(onClick = { finish() }) { Text("退出") }
                        SpacerButton()
                        Button(onClick = { state = UpdateState.Checking("正在重试…") }) { Text("重试") }
                    }
                )

                LaunchedEffect(state) {
                    if (state is UpdateState.Checking) {
                        state = checkUpdate(currentVersion)
                    }
                }
            }
        }
    }

    @Composable
    private fun BlockingDialog(
        title: String,
        message: String,
        buttons: @Composable () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = { /* 强制：不允许关闭 */ },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = { buttons() }
        )
    }

    @Composable
    private fun SpacerButton() {
        Spacer(modifier = Modifier.width(8.dp))
    }

    // ====== 更新检查逻辑 ======

    private sealed class UpdateState {
        data class Checking(val message: String) : UpdateState()
        object Pass : UpdateState()
        data class NeedUpdate(val latestTag: String, val url: String) : UpdateState()
        data class CheckFailed(val reason: String) : UpdateState()
    }

    private suspend fun checkUpdate(currentVersion: String): UpdateState = withContext(Dispatchers.IO) {
        val currentNorm = normalizeVersion(currentVersion)

        fetchLatestFromRelease()?.let { (tag, url) ->
            val latestNorm = normalizeVersion(tag)
            return@withContext if (isNewer(latestNorm, currentNorm)) {
                UpdateState.NeedUpdate(latestTag = tag, url = url)
            } else UpdateState.Pass
        }

        fetchLatestFromTags()?.let { (tag, url) ->
            val latestNorm = normalizeVersion(tag)
            return@withContext if (isNewer(latestNorm, currentNorm)) {
                UpdateState.NeedUpdate(latestTag = tag, url = url)
            } else UpdateState.Pass
        }

        UpdateState.CheckFailed("获取版本信息失败（GitHub 可能超时/不可达）")
    }

    private fun fetchLatestFromRelease(): Pair<String, String>? {
        val url = "https://api.github.com/repos/kgyyds/KCCJ_APIGET/releases/latest"
        val body = httpGet(url) ?: return null
        val json = JSONObject(body)

        val tag = json.optString("tag_name", "").trim()
        if (tag.isBlank()) return null

        val htmlUrl = json.optString("html_url", "").trim()
        val jumpUrl = if (htmlUrl.isNotBlank()) htmlUrl else "https://github.com/kgyyds/KCCJ_APIGET/releases"
        return tag to jumpUrl
    }

    private fun fetchLatestFromTags(): Pair<String, String>? {
        val url = "https://api.github.com/repos/kgyyds/KCCJ_APIGET/tags?per_page=1"
        val body = httpGet(url) ?: return null
        val arr = JSONArray(body)
        if (arr.length() <= 0) return null

        val first = arr.getJSONObject(0)
        val tag = first.optString("name", "").trim()
        if (tag.isBlank()) return null

        val jumpUrl = "https://github.com/kgyyds/KCCJ_APIGET/releases/tag/$tag"
        return tag to jumpUrl
    }

    private fun httpGet(url: String): String? {
        return try {
            val req = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "KCCJ_APIGET")
                .build()

            updateClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                resp.body?.string()
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun normalizeVersion(v: String): List<Int> {
        val raw = v.trim().removePrefix("v").removePrefix("V")
        val core = raw.split('-', '_', '+').firstOrNull().orEmpty()
        return core.split('.')
            .map { part -> part.filter { it.isDigit() } }
            .map { it.toIntOrNull() ?: 0 }
    }

    private fun isNewer(a: List<Int>, b: List<Int>): Boolean {
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val ai = a.getOrNull(i) ?: 0
            val bi = b.getOrNull(i) ?: 0
            if (ai != bi) return ai > bi
        }
        return false
    }
}