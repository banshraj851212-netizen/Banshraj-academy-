package com.example.data.repository

import com.example.data.local.dao.AcademyDao
import com.example.data.local.entity.AccessRequestEntity
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.DoubtEntity
import com.example.data.local.entity.DppEntity
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class AcademyRepository(private val dao: AcademyDao) {

    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allBatches: Flow<List<BatchEntity>> = dao.getAllBatches()
    val allLectures: Flow<List<LectureEntity>> = dao.getAllLectures()
    val allDpps: Flow<List<DppEntity>> = dao.getAllDpps()
    val allTests: Flow<List<TestEntity>> = dao.getAllTests()
    val allDoubts: Flow<List<DoubtEntity>> = dao.getAllDoubts()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()
    val allAccessRequests: Flow<List<AccessRequestEntity>> = dao.getAllAccessRequests()

    fun getLecturesForBatch(batchId: Long): Flow<List<LectureEntity>> = dao.getLecturesByBatch(batchId)
    fun getDppsForBatch(batchId: Long): Flow<List<DppEntity>> = dao.getDppsByBatch(batchId)
    fun getTestsForBatch(batchId: Long): Flow<List<TestEntity>> = dao.getTestsByBatch(batchId)

    // User & Permissions Management (Admin only)
    suspend fun updateUserPermission(userId: Long, canCreate: Boolean, role: String) {
        dao.updateUserPermission(userId, canCreate, role)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return dao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmailDirect(email)
    }

    suspend fun getUserById(id: Long): UserEntity? {
        return dao.getUserByIdDirect(id)
    }

    suspend fun getUserByPhone(phone: String): UserEntity? {
        return dao.getUserByPhoneDirect(phone)
    }

    suspend fun updateUserBlockedStatus(userId: Long, isBlocked: Boolean) {
        dao.updateUserBlockedStatus(userId, isBlocked)
    }

    suspend fun updateTeacherAssignedBatches(teacherId: Long, batchIds: String) {
        dao.updateTeacherAssignedBatches(teacherId, batchIds)
    }

    // Batch Management
    suspend fun addBatch(batch: BatchEntity): Long = dao.insertBatch(batch)
    suspend fun updateBatch(batch: BatchEntity) = dao.updateBatch(batch)
    suspend fun deleteBatch(batchId: Long) = dao.deleteBatch(batchId)

    suspend fun setBatchEnrolled(batchId: Long, isEnrolled: Boolean) {
        // Find batch and update
        dao.getBatchById(batchId)
    }

    // Lecture & Content Management
    suspend fun addLecture(lecture: LectureEntity): Long = dao.insertLecture(lecture)
    suspend fun updateLecture(lecture: LectureEntity) = dao.updateLecture(lecture)
    suspend fun deleteLecture(lectureId: Long) = dao.deleteLecture(lectureId)

    // DPP Management
    suspend fun addDpp(dpp: DppEntity): Long = dao.insertDpp(dpp)
    suspend fun updateDpp(dpp: DppEntity) = dao.updateDpp(dpp)
    suspend fun deleteDpp(dppId: Long) = dao.deleteDpp(dppId)

    // Test Management
    suspend fun addTest(test: TestEntity): Long = dao.insertTest(test)
    suspend fun updateTest(test: TestEntity) = dao.updateTest(test)

    // Doubt Management
    suspend fun addDoubt(doubt: DoubtEntity): Long = dao.insertDoubt(doubt)
    suspend fun updateDoubt(doubt: DoubtEntity) = dao.updateDoubt(doubt)

    // Announcements
    suspend fun addAnnouncement(announcement: AnnouncementEntity): Long = dao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(id: Long) = dao.deleteAnnouncement(id)

    // Course Access Requests
    suspend fun submitAccessRequest(request: AccessRequestEntity): Long = dao.insertAccessRequest(request)
    suspend fun updateAccessRequest(request: AccessRequestEntity) = dao.updateAccessRequest(request)
    suspend fun deleteAccessRequest(id: Long) = dao.deleteAccessRequest(id)
}
