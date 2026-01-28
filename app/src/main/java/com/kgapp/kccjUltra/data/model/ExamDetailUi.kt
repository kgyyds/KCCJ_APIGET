package com.kgapp.kccjUltra.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ExamDetailUi(
    val courses: List<String> = emptyList(),
    val students: List<StudentScoreUi> = emptyList()
)

@Immutable
data class StudentScoreUi(
    val studentName: String,
    val studentNum: String,
    val searched: String,
    val scores: List<String>
)
