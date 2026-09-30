package com.example.data.firestore

import android.util.Log
import com.example.data.firestore.schema.FirestoreDoubtQuery
import com.example.data.firestore.schema.FirestoreDpp
import com.example.data.firestore.schema.FirestoreEnrollment
import com.example.data.firestore.schema.FirestoreLecture
import com.example.data.firestore.schema.FirestoreLiveSession
import com.example.data.firestore.schema.FirestoreUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Service managing Cloud Firestore operations for Banshraj Academy.
 * Handles user profiles & roles (admin/student differentiation),
 * lecture metadata, student enrollments, and student doubt queries.
 * Includes graceful fallback if Firebase project credentials are not yet configured.
 */
class FirestoreService {

    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_LECTURES = "lectures"
        const val COLLECTION_ENROLLMENTS = "enrollments"
        const val COLLECTION_DOUBTS = "doubts"
        const val COLLECTION_DPPS = "dpps"
        const val COLLECTION_LIVE_SESSIONS = "live_sessions"
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore not initialized or missing configuration: ${e.message}")
            null
        }
    }

    val isFirestoreAvailable: Boolean
        get() = firestore != null

    // ==========================================
    // 0. USER PROFILE & ROLE OPERATIONS (Firestore "users" Collection)
    // ==========================================

    suspend fun getUserProfile(email: String): Result<FirestoreUser?> =
        getUserProfileByIdentifier(email)

    suspend fun getUserProfileByIdentifier(identifier: String): Result<FirestoreUser?> =
        suspendCancellableCoroutine { continuation ->
            val clean = identifier.trim().lowercase()
            val isEmail = clean.contains("@")
            val queryValue = if (isEmail) clean else clean.filter { it.isDigit() }.takeLast(10)
            val db = firestore
            if (db != null) {
                try {
                    val field = if (isEmail) "email" else "phone"
                    db.collection(COLLECTION_USERS)
                        .whereEqualTo(field, queryValue)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val doc = snapshot.documents.firstOrNull()
                            val user = doc?.toObject(FirestoreUser::class.java)
                            Log.d(TAG, "Fetched Firestore user doc for $queryValue: role=${user?.role}, blocked=${user?.isBlocked}")
                            if (continuation.isActive) continuation.resume(Result.success(user))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error fetching user from Firestore: ${e.message}")
                            // Fallback gracefully to local mock if firestore query errors
                            val fallback = getFallbackUser(clean, isEmail)
                            if (continuation.isActive) continuation.resume(Result.success(fallback))
                        }
                } catch (e: Exception) {
                    val fallback = getFallbackUser(clean, isEmail)
                    if (continuation.isActive) continuation.resume(Result.success(fallback))
                }
            } else {
                // Local simulation fallback
                val mockUser = getFallbackUser(clean, isEmail)
                if (continuation.isActive) continuation.resume(Result.success(mockUser))
            }
        }

    private fun getFallbackUser(clean: String, isEmail: Boolean): FirestoreUser {
        val digits = clean.filter { it.isDigit() }.takeLast(10)
        val role = when {
            clean == "banshraj851212@gmail.com" || clean == "admin@banshraj.edu" || digits == "9876543212" || clean.contains("admin") -> "ADMIN"
            clean == "teacher@banshraj.edu" || digits == "9876543211" || clean.contains("teacher") -> "TEACHER"
            else -> "STUDENT"
        }
        val name = when (role) {
            "ADMIN" -> "Banshraj Sir (Admin)"
            "TEACHER" -> "Er. Alok Sharma (Faculty)"
            else -> if (isEmail) clean.substringBefore("@").replace(".", " ").capitalize() else "Student ($digits)"
        }
        val phone = if (!isEmail) clean else when (role) {
            "ADMIN" -> "9876543212"
            "TEACHER" -> "9876543211"
            else -> "9876543210"
        }
        val email = if (isEmail) clean else when (role) {
            "ADMIN" -> "admin@banshraj.edu"
            "TEACHER" -> "teacher@banshraj.edu"
            else -> "student@banshraj.edu"
        }
        return FirestoreUser(
            uid = "uid_${clean.replace("@", "_").replace(".", "_")}",
            email = email,
            phone = phone,
            name = name,
            role = role,
            isBlocked = false,
            canCreateContent = (role == "ADMIN" || role == "TEACHER"),
            enrolledBatchIds = "1,2",
            assignedBatchIds = if (role == "TEACHER") "1,2" else ""
        )
    }

    suspend fun saveOrUpdateUser(user: FirestoreUser): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val docId = user.email.trim().lowercase().replace(".", "_")
                    db.collection(COLLECTION_USERS)
                        .document(docId)
                        .set(user.toMap(), com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Saved user to Firestore users collection: ${user.email} (Role: ${user.role})")
                            if (continuation.isActive) continuation.resume(Result.success(Unit))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed saving user to Firestore: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
        }

    // ==========================================
    // 1. LECTURE METADATA OPERATIONS
    // ==========================================

    suspend fun saveLecture(lecture: FirestoreLecture): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val docRef = if (lecture.id.isNotBlank()) {
                        db.collection(COLLECTION_LECTURES).document(lecture.id)
                    } else {
                        db.collection(COLLECTION_LECTURES).document()
                    }
                    val finalLecture = lecture.copy(id = docRef.id)
                    docRef.set(finalLecture.toMap())
                        .addOnSuccessListener {
                            Log.d(TAG, "Lecture metadata saved to Firestore with ID: ${docRef.id}")
                            if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to save lecture metadata: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                // Local simulation fallback
                val mockId = if (lecture.id.isNotBlank()) lecture.id else "lec_${System.currentTimeMillis()}"
                if (continuation.isActive) continuation.resume(Result.success(mockId))
            }
        }

    suspend fun getLecturesForBatch(batchId: Long): Result<List<FirestoreLecture>> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    db.collection(COLLECTION_LECTURES)
                        .whereEqualTo("batchId", batchId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreLecture::class.java) }
                            if (continuation.isActive) continuation.resume(Result.success(list))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(emptyList()))
            }
        }

    // ==========================================
    // 2. STUDENT ENROLLMENTS OPERATIONS
    // ==========================================

    suspend fun saveEnrollment(enrollment: FirestoreEnrollment): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val docRef = if (enrollment.id.isNotBlank()) {
                        db.collection(COLLECTION_ENROLLMENTS).document(enrollment.id)
                    } else {
                        db.collection(COLLECTION_ENROLLMENTS).document()
                    }
                    val finalEnrollment = enrollment.copy(id = docRef.id)
                    docRef.set(finalEnrollment.toMap())
                        .addOnSuccessListener {
                            Log.d(TAG, "Enrollment saved to Firestore with ID: ${docRef.id}")
                            if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to save enrollment: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                val mockId = if (enrollment.id.isNotBlank()) enrollment.id else "enr_${System.currentTimeMillis()}"
                if (continuation.isActive) continuation.resume(Result.success(mockId))
            }
        }

    suspend fun getEnrollmentsForStudent(studentEmail: String): Result<List<FirestoreEnrollment>> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    db.collection(COLLECTION_ENROLLMENTS)
                        .whereEqualTo("studentEmail", studentEmail.trim().lowercase())
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreEnrollment::class.java) }
                            if (continuation.isActive) continuation.resume(Result.success(list))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(emptyList()))
            }
        }

    suspend fun updateEnrollmentStatus(enrollmentId: String, newStatus: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null && enrollmentId.isNotBlank()) {
                try {
                    db.collection(COLLECTION_ENROLLMENTS)
                        .document(enrollmentId)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(Result.success(Unit))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
        }

    // ==========================================
    // 3. DOUBT QUERIES OPERATIONS
    // ==========================================

    suspend fun submitDoubt(doubt: FirestoreDoubtQuery): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val docRef = if (doubt.id.isNotBlank()) {
                        db.collection(COLLECTION_DOUBTS).document(doubt.id)
                    } else {
                        db.collection(COLLECTION_DOUBTS).document()
                    }
                    val finalDoubt = doubt.copy(id = docRef.id)
                    docRef.set(finalDoubt.toMap())
                        .addOnSuccessListener {
                            Log.d(TAG, "Doubt query submitted to Firestore with ID: ${docRef.id}")
                            if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to submit doubt query: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                val mockId = if (doubt.id.isNotBlank()) doubt.id else "dbt_${System.currentTimeMillis()}"
                if (continuation.isActive) continuation.resume(Result.success(mockId))
            }
        }

    suspend fun getAllDoubts(): Result<List<FirestoreDoubtQuery>> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    db.collection(COLLECTION_DOUBTS)
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreDoubtQuery::class.java) }
                            if (continuation.isActive) continuation.resume(Result.success(list))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(emptyList()))
            }
        }

    suspend fun answerDoubt(doubtId: String, answer: String, answeredBy: String, resolutionStatus: String = "RESOLVED"): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null && doubtId.isNotBlank()) {
                try {
                    db.collection(COLLECTION_DOUBTS)
                        .document(doubtId)
                        .update(
                            mapOf(
                                "answer" to answer,
                                "answeredBy" to answeredBy,
                                "isAnswered" to true,
                                "resolutionStatus" to resolutionStatus
                            )
                        )
                        .addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(Result.success(Unit))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
        }

    suspend fun upvoteDoubt(doubtId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null && doubtId.isNotBlank()) {
                try {
                    db.collection(COLLECTION_DOUBTS)
                        .document(doubtId)
                        .update("upvotes", com.google.firebase.firestore.FieldValue.increment(1))
                        .addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(Result.success(Unit))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
        }

    // ==========================================
    // 4. DAILY PRACTICE PROBLEMS (DPP) OPERATIONS
    // ==========================================

    suspend fun saveCourseDpp(dpp: FirestoreDpp): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val docRef = if (dpp.id.isNotBlank()) {
                        db.collection(COLLECTION_DPPS).document(dpp.id)
                    } else {
                        db.collection(COLLECTION_DPPS).document()
                    }
                    val finalDpp = dpp.copy(id = docRef.id)
                    docRef.set(finalDpp.toMap())
                        .addOnSuccessListener {
                            Log.d(TAG, "DPP saved to Firestore with ID: ${docRef.id}")
                            if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to save DPP: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                val mockId = if (dpp.id.isNotBlank()) dpp.id else "dpp_${System.currentTimeMillis()}"
                if (continuation.isActive) continuation.resume(Result.success(mockId))
            }
        }

    suspend fun getCourseDpps(courseId: Long): Result<List<FirestoreDpp>> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val query = if (courseId > 0) {
                        db.collection(COLLECTION_DPPS).whereEqualTo("courseId", courseId)
                    } else {
                        db.collection(COLLECTION_DPPS)
                    }
                    query.get()
                        .addOnSuccessListener { snapshot ->
                            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreDpp::class.java) }
                            if (continuation.isActive) continuation.resume(Result.success(list))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(emptyList()))
            }
        }

    // ==========================================
    // 5. LIVE LECTURES & ACTIVE STREAMING SESSIONS
    // ==========================================

    suspend fun saveLiveSession(session: FirestoreLiveSession): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    val docRef = if (session.id.isNotBlank()) {
                        db.collection(COLLECTION_LIVE_SESSIONS).document(session.id)
                    } else {
                        db.collection(COLLECTION_LIVE_SESSIONS).document()
                    }
                    val finalSession = session.copy(id = docRef.id)
                    docRef.set(finalSession.toMap())
                        .addOnSuccessListener {
                            Log.d(TAG, "Live session saved to Firestore with ID: ${docRef.id}")
                            if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to save live session: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                val mockId = if (session.id.isNotBlank()) session.id else "live_${System.currentTimeMillis()}"
                if (continuation.isActive) continuation.resume(Result.success(mockId))
            }
        }

    suspend fun getActiveLiveSessions(): Result<List<FirestoreLiveSession>> =
        suspendCancellableCoroutine { continuation ->
            val db = firestore
            if (db != null) {
                try {
                    db.collection(COLLECTION_LIVE_SESSIONS)
                        .whereEqualTo("status", "LIVE")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val list = snapshot.documents.mapNotNull { it.toObject(FirestoreLiveSession::class.java) }
                            if (continuation.isActive) continuation.resume(Result.success(list))
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            } else {
                if (continuation.isActive) continuation.resume(Result.success(emptyList()))
            }
        }
}
