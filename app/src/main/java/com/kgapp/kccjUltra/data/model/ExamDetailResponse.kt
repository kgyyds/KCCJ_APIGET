package com.kgapp.kccjUltra.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class ExamDetailResponse(
    @SerializedName("courses_list") val coursesList: List<Course> = emptyList(),
    @SerializedName("student_score_list") val studentScoreList: List<JsonObject> = emptyList(),
    @SerializedName("total_record") val totalRecord: Int = 0
)

data class Course(
    val course: String
)
