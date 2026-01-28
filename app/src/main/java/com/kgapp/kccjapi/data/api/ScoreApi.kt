package com.kgapp.kccjapi.data.api

import com.kgapp.kccjapi.data.model.ExamDetailResponse
import com.kgapp.kccjapi.data.model.ExamListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ScoreApi {
    @GET("_load_exams.jsp")
    suspend fun loadExams(
        @Query("username") username: String,
        @Query("filter_date") filterDate: String? = "",
        @Query("current_page") currentPage: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ExamListResponse

    @GET("_load_exam_detail.jsp")
    suspend fun loadExamDetail(
        @Query("exam_id") examId: String,
        @Query("current_page") currentPage: Int = 1,
        @Query("page_size") pageSize: Int = 60
    ): ExamDetailResponse
}
