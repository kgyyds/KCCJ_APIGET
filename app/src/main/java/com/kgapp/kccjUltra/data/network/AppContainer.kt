package com.kgapp.kccjUltra.data.network

import android.content.Context
import com.kgapp.kccjUltra.data.repo.ScoreRepository
import com.kgapp.kccjUltra.data.store.UserPreferences

class AppContainer(context: Context) {
    val preferences = UserPreferences(context)
    private val api = NetworkModule.createApi(preferences)
    val repository = ScoreRepository(api)
}
