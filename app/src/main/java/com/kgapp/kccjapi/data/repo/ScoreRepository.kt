package com.kgapp.kccjapi.data.repo

import com.kgapp.kccjapi.data.api.ScoreApi
import com.kgapp.kccjapi.data.model.ExamDetailResponse
import com.kgapp.kccjapi.data.model.ExamListResponse

class ScoreRepository(private val api: ScoreApi) {
    suspend fun loadExams(username: String): ExamListResponse {
        return api.loadExams(username = username, pageSize = 20)
    }

    suspend fun loadExamDetail(examId: String): ExamDetailResponse {
        return api.loadExamDetail(examId = examId)
    }
}
