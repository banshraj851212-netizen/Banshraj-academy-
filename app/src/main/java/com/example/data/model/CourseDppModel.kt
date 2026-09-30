package com.example.data.model

import androidx.annotation.Keep

/**
 * Data model representing a Daily Practice Problem (DPP)
 * associated with a specific course/batch.
 */
@Keep
data class CourseDppModel(
    val id: Long = 0L,
    val firestoreId: String = "",
    val courseId: Long = 1L,
    val courseName: String = "Lakshya JEE 2026",
    val lectureId: Long = 0L,
    val lectureTitle: String = "",
    val subject: String = "Physics",
    val chapter: String = "",
    val title: String = "",
    val questionsCount: Int = 10,
    val totalMarks: Int = 40,
    val difficulty: String = "JEE Main", // "JEE Main", "JEE Advanced", "NEET UG", "Board Level"
    val timeLimitMinutes: Int = 30,
    val isSolved: Boolean = false,
    val score: Int = 0,
    val pdfUrl: String = "",
    val solutionUrl: String = "",
    val createdAt: String = "Today"
)
