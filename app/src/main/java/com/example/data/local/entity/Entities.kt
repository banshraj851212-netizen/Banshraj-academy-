package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String = "9876543210",
    val role: String = "STUDENT", // "ADMIN", "TEACHER", "STUDENT"
    val isBlocked: Boolean = false,
    val canCreateContent: Boolean = false,
    val enrolledBatchIds: String = "1", // comma-separated IDs
    val assignedBatchIds: String = "",
    val studyHours: Float = 14.5f,
    val solvedDpps: Int = 28,
    val testsAttempted: Int = 6,
    val streakDays: Int = 12
)

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val tagline: String,
    val category: String, // "JEE", "NEET", "CLASS 11", "CLASS 12", "FOUNDATION"
    val originalPrice: Int,
    val discountPrice: Int,
    val instructor: String,
    val startDate: String,
    val validity: String,
    val isEnrolled: Boolean = false,
    val rating: Float = 4.9f,
    val badge: String = "Bestseller",
    val features: String = "Live + Recorded, Daily DPPs, 24x7 Doubts, All India Tests"
)

@Entity(tableName = "lectures")
data class LectureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: Long,
    val subject: String,
    val chapter: String,
    val title: String,
    val instructor: String,
    val duration: String,
    val videoUrl: String = "",
    val isLive: Boolean = false,
    val isCompleted: Boolean = false,
    val notesAvailable: Boolean = true,
    val dppAvailable: Boolean = true,
    val orderIndex: Int = 1,
    val notesContent: String = "Key formulas & concepts for this lecture."
)

@Entity(tableName = "dpps")
data class DppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: Long,
    val lectureId: Long,
    val subject: String,
    val title: String,
    val questionsCount: Int = 10,
    val totalMarks: Int = 40,
    val isSolved: Boolean = false,
    val score: Int = 0
)

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: Long,
    val title: String,
    val subject: String,
    val durationMinutes: Int = 60,
    val totalMarks: Int = 120,
    val questionsJson: String,
    val isAttempted: Boolean = false,
    val userScore: Int = 0,
    val rank: Int = 0
)

@Entity(tableName = "doubts")
data class DoubtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentName: String,
    val studentEmail: String,
    val subject: String,
    val batchName: String,
    val question: String,
    val imageUrl: String? = null,
    val resolutionStatus: String = "PENDING", // "PENDING", "IN_REVIEW", "RESOLVED"
    val answer: String? = null,
    val answeredBy: String? = null,
    val isAnswered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 3
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val postedBy: String = "Banshraj Sir",
    val date: String,
    val isImportant: Boolean = false
)

@Entity(tableName = "access_requests")
data class AccessRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentName: String,
    val studentEmail: String,
    val batchId: Long,
    val batchTitle: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestDate: String
)
