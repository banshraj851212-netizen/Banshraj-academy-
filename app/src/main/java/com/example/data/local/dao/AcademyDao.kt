package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccessRequestEntity
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.DoubtEntity
import com.example.data.local.entity.DppEntity
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {
    // Users
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailDirect(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdDirect(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhoneDirect(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET canCreateContent = :canCreate, role = :role WHERE id = :userId")
    suspend fun updateUserPermission(userId: Long, canCreate: Boolean, role: String)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun updateUserBlockedStatus(userId: Long, isBlocked: Boolean)

    @Query("UPDATE users SET assignedBatchIds = :batchIds WHERE id = :teacherId")
    suspend fun updateTeacherAssignedBatches(teacherId: Long, batchIds: String)

    // Batches
    @Query("SELECT * FROM batches ORDER BY id ASC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    fun getBatchById(id: Long): Flow<BatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BatchEntity>)

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("DELETE FROM batches WHERE id = :batchId")
    suspend fun deleteBatch(batchId: Long)

    // Lectures
    @Query("SELECT * FROM lectures ORDER BY batchId ASC, orderIndex ASC")
    fun getAllLectures(): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE batchId = :batchId ORDER BY orderIndex ASC")
    fun getLecturesByBatch(batchId: Long): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE batchId = :batchId AND subject = :subject ORDER BY orderIndex ASC")
    fun getLecturesByBatchAndSubject(batchId: Long, subject: String): Flow<List<LectureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: LectureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLectures(lectures: List<LectureEntity>)

    @Update
    suspend fun updateLecture(lecture: LectureEntity)

    @Query("DELETE FROM lectures WHERE id = :id")
    suspend fun deleteLecture(id: Long)

    // DPPs
    @Query("SELECT * FROM dpps ORDER BY id ASC")
    fun getAllDpps(): Flow<List<DppEntity>>

    @Query("SELECT * FROM dpps WHERE batchId = :batchId ORDER BY id ASC")
    fun getDppsByBatch(batchId: Long): Flow<List<DppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDpp(dpp: DppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDpps(dpps: List<DppEntity>)

    @Update
    suspend fun updateDpp(dpp: DppEntity)

    @Query("DELETE FROM dpps WHERE id = :id")
    suspend fun deleteDpp(id: Long)

    // Tests
    @Query("SELECT * FROM tests ORDER BY id ASC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE batchId = :batchId ORDER BY id ASC")
    fun getTestsByBatch(batchId: Long): Flow<List<TestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestEntity>)

    @Update
    suspend fun updateTest(test: TestEntity)

    // Doubts
    @Query("SELECT * FROM doubts ORDER BY timestamp DESC")
    fun getAllDoubts(): Flow<List<DoubtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubt(doubt: DoubtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubts(doubts: List<DoubtEntity>)

    @Update
    suspend fun updateDoubt(doubt: DoubtEntity)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Long)

    // Access Requests
    @Query("SELECT * FROM access_requests ORDER BY id DESC")
    fun getAllAccessRequests(): Flow<List<AccessRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessRequest(request: AccessRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessRequests(requests: List<AccessRequestEntity>)

    @Update
    suspend fun updateAccessRequest(request: AccessRequestEntity)

    @Query("DELETE FROM access_requests WHERE id = :id")
    suspend fun deleteAccessRequest(id: Long)
}
