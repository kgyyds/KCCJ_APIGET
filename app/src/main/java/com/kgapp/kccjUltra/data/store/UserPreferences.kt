package com.kgapp.kccjUltra.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kgapp.kccjUltra.data.model.HeaderItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val headersState = MutableStateFlow<List<HeaderItem>>(emptyList())

    private val headersKey = stringPreferencesKey("headers_json")
    private val usernameKey = stringPreferencesKey("saved_username")

    val headersFlow: Flow<List<HeaderItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[headersKey].orEmpty()
        if (json.isBlank()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<HeaderItem>>() {}.type
            runCatching { gson.fromJson<List<HeaderItem>>(json, type) }.getOrDefault(emptyList())
        }
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[usernameKey].orEmpty()
    }

    init {
        scope.launch {
            headersFlow.collect { headersState.value = it }
        }
    }

    suspend fun saveHeaders(headers: List<HeaderItem>) {
        context.dataStore.edit { prefs ->
            prefs[headersKey] = gson.toJson(headers)
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[usernameKey] = username
        }
    }

    fun headersSnapshot(): List<HeaderItem> = headersState.value
}
