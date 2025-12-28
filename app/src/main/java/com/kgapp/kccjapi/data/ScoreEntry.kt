package com.kgapp.kccjapi.data

import com.google.gson.annotations.SerializedName

data class ScoreEntry(
    @SerializedName("pub_time") val pubTime: String? = null,
    @SerializedName("pub_date") val pubDate: String? = null,
    @SerializedName("exam_name") val examName: String? = null,
    @SerializedName("course") val course: String? = null,
    @SerializedName("score") val score: String? = null,
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("student_num") val studentNum: String? = null,
    @SerializedName("search_time") val searchTime: String? = null
)