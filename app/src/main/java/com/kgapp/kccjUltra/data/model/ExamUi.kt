package com.kgapp.kccjUltra.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ExamUi(
    val id: String,
    val title: String,
    val pubTime: String,
    val grade: String,
    val courseCount: Int,
    val unReadTotal: Int
)
