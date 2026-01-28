package com.kgapp.kccjUltra

import android.app.Application
import com.kgapp.kccjUltra.data.network.AppContainer

class ScoreApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
