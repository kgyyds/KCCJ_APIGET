package com.kgapp.kccjUltra.data.session

object SessionManager {
    @Volatile
    private var sessionId: String? = null

    fun setSession(id: String) {
        sessionId = id
    }

    fun clear() {
        sessionId = null
    }

    fun getSession(): String? = sessionId
}
