package com.kgapp.kccjapi.repo

import com.kgapp.kccjapi.data.ScoreEntry
import com.kgapp.kccjapi.net.ScoreApi

class ScoreRepository(private val api: ScoreApi) {

    suspend fun exactQuery(name: String, num: String): Result<List<ScoreEntry>> {
        return try {
            val list = api.searchAllScore(studentName = name.trim(), studentNum = num.trim())
            Result.success(list)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}