package com.kgapp.kccjapi.net

import com.kgapp.kccjapi.data.ScoreEntry
import retrofit2.http.GET
import retrofit2.http.Query

interface ScoreApi {
    @GET("search_exam_all_score.jsp")
    suspend fun searchAllScore(
        @Query("student_name") studentName: String,
        @Query("student_num") studentNum: String,
        @Query("course") course: String = "全部"
    ): List<ScoreEntry>
}