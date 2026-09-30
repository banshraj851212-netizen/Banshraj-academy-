package com.example.data.firestore.schema

import androidx.annotation.Keep

/**
 * Firestore Data Schema for Banshraj Academy.
 * Defines collections and documents for:
 * 1. "lectures" - Video lectures, chapters, notes, instructor metadata
 * 2. "enrollments" - Student batch admissions, validity, approval status
 * 3. "doubts" - 24x7 community and faculty doubt queries
 */

@Keep
data class FirestoreLecture(
    val id: String = "",
    val batchId: Long = 0L,
    val subject: String = "",
    val chapter: String = "",
    val title: String = "",
    val instructor: String = "Banshraj Sir",
    val duration: String = "1h 15m",
    val videoUrl: String = "",
    val notesContent: String = "Key formulas & concepts for this lecture.",
    val isLive: Boolean = false,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "batchId" to batchId,
        "subject" to subject,
        "chapter" to chapter,
        "title" to title,
        "instructor" to instructor,
        "duration" to duration,
        "videoUrl" to videoUrl,
        "notesContent" to notesContent,
        "isLive" to isLive,
        "isCompleted" to isCompleted,
        "orderIndex" to orderIndex,
        "createdAt" to createdAt
    )
}

@Keep
data class FirestoreEnrollment(
    val id: String = "",
    val studentEmail: String = "",
    val studentName: String = "",
    val batchId: Long = 0L,
    val batchTitle: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "PENDING", "REJECTED", "EXPIRED"
    val enrolledAt: Long = System.currentTimeMillis(),
    val validTill: String = "1 Year Access",
    val approvedBy: String = "Banshraj Sir (Admin)"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "studentEmail" to studentEmail,
        "studentName" to studentName,
        "batchId" to batchId,
        "batchTitle" to batchTitle,
        "status" to status,
        "enrolledAt" to enrolledAt,
        "validTill" to validTill,
        "approvedBy" to approvedBy
    )
}

@Keep
data class FirestoreDoubtQuery(
    val id: String = "",
    val studentEmail: String = "",
    val studentName: String = "",
    val subject: String = "",
    val batchName: String = "",
    val question: String = "",
    val imageUrl: String? = null,
    val resolutionStatus: String = "PENDING", // "PENDING", "IN_REVIEW", "RESOLVED"
    val answer: String? = null,
    val answeredBy: String? = null,
    val isAnswered: Boolean = false,
    val upvotes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "studentEmail" to studentEmail,
        "studentName" to studentName,
        "subject" to subject,
        "batchName" to batchName,
        "question" to question,
        "imageUrl" to imageUrl,
        "resolutionStatus" to resolutionStatus,
        "answer" to answer,
        "answeredBy" to answeredBy,
        "isAnswered" to isAnswered,
        "upvotes" to upvotes,
        "timestamp" to timestamp
    )
}

@Keep
data class FirestoreUser(
    val uid: String = "",
    val email: String = "",
    val phone: String = "",
    val name: String = "",
    val role: String = "STUDENT", // "ADMIN", "STUDENT", "TEACHER"
    val isBlocked: Boolean = false,
    val canCreateContent: Boolean = false,
    val enrolledBatchIds: String = "1",
    val assignedBatchIds: String = "",
    val studyHours: Float = 0f,
    val solvedDpps: Int = 0,
    val testsAttempted: Int = 0,
    val streakDays: Int = 1,
    val targetExam: String = "IIT-JEE 2026",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "email" to email,
        "phone" to phone,
        "name" to name,
        "role" to role,
        "isBlocked" to isBlocked,
        "canCreateContent" to canCreateContent,
        "enrolledBatchIds" to enrolledBatchIds,
        "assignedBatchIds" to assignedBatchIds,
        "studyHours" to studyHours,
        "solvedDpps" to solvedDpps,
        "testsAttempted" to testsAttempted,
        "streakDays" to streakDays,
        "targetExam" to targetExam,
        "createdAt" to createdAt
    )
}

@Keep
data class FirestoreDpp(
    val id: String = "",
    val courseId: Long = 0L,
    val courseName: String = "",
    val lectureId: Long = 0L,
    val lectureTitle: String = "",
    val subject: String = "",
    val chapter: String = "",
    val title: String = "",
    val questionsCount: Int = 10,
    val totalMarks: Int = 40,
    val difficulty: String = "JEE Main",
    val timeLimitMinutes: Int = 30,
    val pdfUrl: String = "",
    val solutionUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "courseId" to courseId,
        "courseName" to courseName,
        "lectureId" to lectureId,
        "lectureTitle" to lectureTitle,
        "subject" to subject,
        "chapter" to chapter,
        "title" to title,
        "questionsCount" to questionsCount,
        "totalMarks" to totalMarks,
        "difficulty" to difficulty,
        "timeLimitMinutes" to timeLimitMinutes,
        "pdfUrl" to pdfUrl,
        "solutionUrl" to solutionUrl,
        "createdAt" to createdAt
    )
}

@Keep
data class FirestoreLiveSession(
    val id: String = "",
    val lectureId: Long = 0L,
    val batchId: Long = 1L,
    val batchTitle: String = "Lakshya JEE 2026",
    val title: String = "",
    val subject: String = "Physics",
    val chapter: String = "",
    val instructor: String = "Banshraj Sir",
    val streamingUrl: String = "",
    val status: String = "LIVE", // "LIVE", "UPCOMING", "ENDED"
    val viewersCount: Int = 1240,
    val startedAt: Long = System.currentTimeMillis(),
    val chatEnabled: Boolean = true
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "lectureId" to lectureId,
        "batchId" to batchId,
        "batchTitle" to batchTitle,
        "title" to title,
        "subject" to subject,
        "chapter" to chapter,
        "instructor" to instructor,
        "streamingUrl" to streamingUrl,
        "status" to status,
        "viewersCount" to viewersCount,
        "startedAt" to startedAt,
        "chatEnabled" to chatEnabled
    )
}
