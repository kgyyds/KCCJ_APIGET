package com.kgapp.kccjUltra.data.model

import com.google.gson.annotations.SerializedName


data class ExamListResponse(
    val exams: List<Exam> = emptyList(),
    @SerializedName("total_record") val totalRecord: Int = 0
)

data class Exam(
    val exam: String,
    val searchedTotal: Int,
    val searchedPercent: String,
    val total: Int,
    @SerializedName("search_state") val searchState: String,
    @SerializedName("search_start_time") val searchStartTime: String,
    @SerializedName("courseCount") val courseCount: Int,
    @SerializedName("pub_time") val pubTime: String,
    val grade: String,
    val id: String,
    val unReadTotal: Int
)
