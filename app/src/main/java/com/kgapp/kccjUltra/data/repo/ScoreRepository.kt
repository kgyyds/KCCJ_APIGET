package com.kgapp.kccjUltra.data.repo

import com.kgapp.kccjUltra.data.api.ScoreApi
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.kgapp.kccjUltra.data.model.ExamDetailUi
import com.kgapp.kccjUltra.data.model.ExamUi
import com.kgapp.kccjUltra.data.model.StudentScoreUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScoreRepository(private val api: ScoreApi) {
    suspend fun loadExams(username: String): List<ExamUi> = withContext(Dispatchers.IO) {
        val response = api.loadExams(username = username, pageSize = 20)
        response.exams.map { exam ->
            ExamUi(
                id = exam.id,
                title = exam.exam,
                pubTime = exam.pubTime,
                grade = exam.grade,
                courseCount = exam.courseCount,
                unReadTotal = exam.unReadTotal
            )
        }
    }

    suspend fun loadExamDetail(examId: String): ExamDetailUi = withContext(Dispatchers.IO) {
        val response = api.loadExamDetail(examId = examId)
        val courses = response.coursesList.map { it.course }
        val students = response.studentScoreList.map { student ->
            val studentName = student.stringValue("student_name")
            val studentNum = student.stringValue("student_num")
            val searched = student.stringValue("searched")
            val scores = courses.map { courseName ->
                student.stringValue(courseName)
            }
            StudentScoreUi(
                studentName = studentName,
                studentNum = studentNum,
                searched = searched,
                scores = scores
            )
        }
        ExamDetailUi(courses = courses, students = students)
    }
}

private fun JsonObject.stringValue(key: String): String {
    val element: JsonElement? = if (has(key)) get(key) else null
    return if (element == null || element.isJsonNull) "-" else element.asString
}
