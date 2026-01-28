package com.kgapp.kccjUltra.data.api

import com.kgapp.kccjUltra.data.model.ExamDetailResponse
import com.kgapp.kccjUltra.data.model.ExamListResponse
import com.kgapp.kccjUltra.data.model.UpdateScoreResponse
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

    @GET("_update_exam_score_for_edit.jsp")
    suspend fun updateExamScore(
        @Query("exam_id") examId: String,
        @Query("student_name") studentName: String,
        @Query("student_num") studentNum: String,
        @Query("course") course: String,
        @Query("score") score: String
    ): UpdateScoreResponse
}
