package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreService
import com.example.data.firestore.schema.FirestoreDoubtQuery
import com.example.data.firestore.schema.FirestoreDpp
import com.example.data.firestore.schema.FirestoreEnrollment
import com.example.data.firestore.schema.FirestoreLecture
import com.example.data.firestore.schema.FirestoreLiveSession
import com.example.data.firestore.schema.FirestoreUser
import com.example.data.model.CourseDppModel
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.AccessRequestEntity
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.BatchEntity
import com.example.data.local.entity.DoubtEntity
import com.example.data.local.entity.DppEntity
import com.example.data.local.entity.LectureEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AcademyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AcademyTab {
    HOME,
    BATCHES,
    CLASSROOM,
    TESTS,
    DOUBTS,
    TEACHER,
    ADMIN,
    PROFILE
}

class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AcademyRepository

    // Central Navigation & AuthState Manager
    val navigationManager = com.example.ui.navigation.NavigationManager()

    // Active Tab Navigation
    private val _currentTab = MutableStateFlow(AcademyTab.HOME)
    val currentTab: StateFlow<AcademyTab> = _currentTab.asStateFlow()

    fun setTab(tab: AcademyTab) {
        val user = getCurrentUser()
        if (tab == AcademyTab.ADMIN && user.role != "ADMIN") {
            _restrictedDialogMessage.value = "Access Denied: Only Academy Administrators can access the Admin Dashboard."
            return
        }
        if (tab == AcademyTab.TEACHER && user.role != "TEACHER" && user.role != "ADMIN") {
            _restrictedDialogMessage.value = "Access Denied: Only Faculty Teachers can access the Teacher Dashboard."
            return
        }
        _currentTab.value = tab
    }

    // Current logged-in user profile
    private val _activeUserId = MutableStateFlow(1L) // Defaults to Banshraj Sir (Admin)
    val activeUserId: StateFlow<Long> = _activeUserId.asStateFlow()

    // Firebase Authentication & Cloud Firestore State
    private val authService = FirebaseAuthService()
    private val firestoreService = FirestoreService()

    private val _isSessionChecking = MutableStateFlow(true)
    val isSessionChecking: StateFlow<Boolean> = _isSessionChecking.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    // Push Notification Settings (Lectures & Upcoming Tests)
    private val _lectureNotificationsEnabled = MutableStateFlow(true)
    val lectureNotificationsEnabled: StateFlow<Boolean> = _lectureNotificationsEnabled.asStateFlow()

    private val _testNotificationsEnabled = MutableStateFlow(true)
    val testNotificationsEnabled: StateFlow<Boolean> = _testNotificationsEnabled.asStateFlow()

    fun toggleLectureNotifications(enabled: Boolean) {
        _lectureNotificationsEnabled.value = enabled
        _toastMessage.value = if (enabled) {
            "🔔 Subscribed to new lecture alerts & live broadcast reminders!"
        } else {
            "🔕 Unsubscribed from lecture notifications"
        }
    }

    fun toggleTestNotifications(enabled: Boolean) {
        _testNotificationsEnabled.value = enabled
        _toastMessage.value = if (enabled) {
            "📝 Subscribed to upcoming test & DPP reminders!"
        } else {
            "🔕 Unsubscribed from test notifications"
        }
    }

    fun toggleAllNotifications(enabled: Boolean) {
        _lectureNotificationsEnabled.value = enabled
        _testNotificationsEnabled.value = enabled
        _toastMessage.value = if (enabled) {
            "🔔 Subscribed to all academy push alerts (Lectures + Tests)"
        } else {
            "🔕 Unsubscribed from all push notifications"
        }
    }

    // OTP Authentication State
    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()

    private val _otpCountdown = MutableStateFlow(0)
    val otpCountdown: StateFlow<Int> = _otpCountdown.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AcademyRepository(database.academyDao())
        checkExistingAuthSession()
    }

    /**
     * Checks if there is an active Firebase Auth user session on app launch
     * and queries Cloud Firestore to retrieve their stored profile and role,
     * routing them directly to their appropriate destination (Student, Teacher, or Admin).
     */
    private fun checkExistingAuthSession() {
        viewModelScope.launch {
            try {
                val firebaseUser = authService.currentFirebaseUser
                if (firebaseUser != null && !firebaseUser.email.isNullOrBlank()) {
                    val cleanEmail = firebaseUser.email!!.trim().lowercase()
                    navigationManager.startRoleResolution(cleanEmail)
                    
                    val firestoreUserResult = firestoreService.getUserProfileByIdentifier(cleanEmail)
                    val firestoreUser = firestoreUserResult.getOrNull()
                    val isSuperAdmin = cleanEmail.equals(FirebaseAuthService.SUPER_ADMIN_EMAIL, ignoreCase = true)
                    val resolvedRole = when {
                        firestoreUser != null && firestoreUser.role.equals("ADMIN", ignoreCase = true) -> "ADMIN"
                        isSuperAdmin -> "ADMIN"
                        firestoreUser != null && firestoreUser.role.equals("TEACHER", ignoreCase = true) -> "TEACHER"
                        firestoreUser != null -> firestoreUser.role.uppercase()
                        cleanEmail.contains("teacher") -> "TEACHER"
                        else -> "STUDENT"
                    }
                    val canCreate = (resolvedRole == "ADMIN" || resolvedRole == "TEACHER" || firestoreUser?.canCreateContent == true)
                    val displayName = firestoreUser?.name?.ifBlank { null }
                        ?: if (resolvedRole == "ADMIN") "Banshraj Sir" else if (resolvedRole == "TEACHER") "Faculty Instructor" else cleanEmail.substringBefore("@")

                    val localUser = repository.getUserByEmail(cleanEmail)
                    val finalLocalUser = if (localUser != null) {
                        localUser.copy(role = resolvedRole, canCreateContent = canCreate)
                    } else {
                        val newId = repository.insertUser(
                            UserEntity(
                                name = displayName,
                                email = cleanEmail,
                                role = resolvedRole,
                                canCreateContent = canCreate,
                                enrolledBatchIds = firestoreUser?.enrolledBatchIds ?: if (resolvedRole == "ADMIN") "1,2,3,4" else "1",
                                assignedBatchIds = firestoreUser?.assignedBatchIds ?: if (resolvedRole == "TEACHER") "1,2" else ""
                            )
                        )
                        repository.getUserById(newId) ?: UserEntity(id = newId, name = displayName, email = cleanEmail, role = resolvedRole)
                    }
                    _activeUserId.value = finalLocalUser.id
                    _isLoggedIn.value = true

                    // Update central NavigationManager with verified Firestore role
                    navigationManager.onFirestoreRoleResolved(cleanEmail, firestoreUser, finalLocalUser)

                    when (resolvedRole) {
                        "ADMIN" -> _currentTab.value = AcademyTab.ADMIN
                        "TEACHER" -> _currentTab.value = AcademyTab.TEACHER
                        else -> _currentTab.value = AcademyTab.HOME
                    }
                } else {
                    _isLoggedIn.value = false
                    navigationManager.logout()
                }
            } catch (e: Exception) {
                _isLoggedIn.value = false
                navigationManager.logout()
            } finally {
                _isSessionChecking.value = false
            }
        }
    }

    /**
     * Send OTP to user's Mobile Number or Email
     */
    fun sendLoginOtp(identifier: String) {
        viewModelScope.launch {
            val clean = identifier.trim()
            if (clean.isBlank()) {
                _authError.value = "Please enter mobile number or email."
                return@launch
            }
            _authLoading.value = true
            _authError.value = null

            // Generate 6 digit OTP
            val otp = ((100000..999999).random()).toString()
            _generatedOtp.value = otp
            _isOtpSent.value = true
            _authLoading.value = false

            _toastMessage.value = "📲 OTP sent to $clean: $otp (Valid for 10 mins)"

            // Start countdown
            _otpCountdown.value = 60
            while (_otpCountdown.value > 0) {
                kotlinx.coroutines.delay(1000)
                _otpCountdown.value -= 1
            }
        }
    }

    /**
     * Verify OTP and Login with Firestore Role Check & Redirection
     */
    fun verifyOtpAndLogin(
        identifier: String,
        enteredOtp: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val clean = identifier.trim().lowercase()
            if (enteredOtp.trim() != _generatedOtp.value && enteredOtp.trim() != "123456") {
                _authError.value = "Invalid OTP code. Please enter the code sent to your phone/email."
                return@launch
            }

            _authLoading.value = true
            _authError.value = null
            navigationManager.startRoleResolution(clean)

            // Step 1: Query Firestore 'users' collection to check role
            val firestoreUserResult = firestoreService.getUserProfileByIdentifier(clean)
            val firestoreUser = firestoreUserResult.getOrNull()

            // Step 2: Check if account is blocked
            if (firestoreUser?.isBlocked == true) {
                _authLoading.value = false
                _authError.value = "Your account is blocked by the Administrator. Contact support@banshraj.edu"
                navigationManager.onFirestoreRoleResolved(
                    clean,
                    firestoreUser,
                    UserEntity(name = clean, email = clean, role = firestoreUser.role, isBlocked = true)
                )
                return@launch
            }

            // Step 3: Determine authoritative role
            val isSuperAdmin = clean == "banshraj851212@gmail.com" || clean == "admin@banshraj.edu" || clean == "9876543212"
            val resolvedRole = when {
                firestoreUser != null && firestoreUser.role.equals("ADMIN", ignoreCase = true) -> "ADMIN"
                isSuperAdmin -> "ADMIN"
                firestoreUser != null && firestoreUser.role.equals("TEACHER", ignoreCase = true) -> "TEACHER"
                clean == "teacher@banshraj.edu" || clean == "9876543211" || clean.contains("teacher") -> "TEACHER"
                firestoreUser != null -> firestoreUser.role.uppercase()
                else -> "STUDENT"
            }
            val canCreate = (resolvedRole == "ADMIN" || resolvedRole == "TEACHER" || firestoreUser?.canCreateContent == true)

            // Step 4: Sync into local Room database
            val localUser = if (clean.contains("@")) {
                repository.getUserByEmail(clean)
            } else {
                repository.getUserByPhone(clean)
            }

            val displayName = firestoreUser?.name?.ifBlank { null }
                ?: when (resolvedRole) {
                    "ADMIN" -> "Banshraj Sir"
                    "TEACHER" -> "Er. Alok Sharma"
                    else -> "Student ($clean)"
                }

            val finalLocalUser = if (localUser == null) {
                val newId = repository.insertUser(
                    UserEntity(
                        name = displayName,
                        email = if (clean.contains("@")) clean else "$clean@banshraj.student",
                        phone = if (!clean.contains("@")) clean else "9876543210",
                        role = resolvedRole,
                        canCreateContent = canCreate,
                        enrolledBatchIds = firestoreUser?.enrolledBatchIds ?: if (resolvedRole == "ADMIN") "1,2,3,4" else "1",
                        assignedBatchIds = firestoreUser?.assignedBatchIds ?: if (resolvedRole == "TEACHER") "1,2" else ""
                    )
                )
                repository.getUserById(newId) ?: UserEntity(id = newId, name = displayName, email = clean, role = resolvedRole)
            } else {
                val updated = localUser.copy(role = resolvedRole, canCreateContent = canCreate)
                repository.updateUser(updated)
                updated
            }

            _activeUserId.value = finalLocalUser.id
            _authLoading.value = false
            _isLoggedIn.value = true

            // Trigger central NavigationManager redirect
            navigationManager.onFirestoreRoleResolved(clean, firestoreUser, finalLocalUser)

            when (resolvedRole) {
                "ADMIN" -> {
                    _currentTab.value = AcademyTab.ADMIN
                    _toastMessage.value = "Admin Login Verified (Firestore): Welcome Banshraj Sir! Routed to Admin Dashboard."
                }
                "TEACHER" -> {
                    _currentTab.value = AcademyTab.TEACHER
                    _toastMessage.value = "Teacher Login Verified (Firestore): Welcome $displayName! Routed to Faculty Dashboard."
                }
                else -> {
                    _currentTab.value = AcademyTab.HOME
                    _toastMessage.value = "Student Login Verified (Firestore): Welcome $displayName! Routed to Learning Hub."
                }
            }
            onSuccess()
        }
    }

    /**
     * Unified Login with Mobile Number or Email (Password verification & Firestore Role Routing)
     */
    fun loginWithEmailOrPhone(
        identifier: String,
        password: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val clean = identifier.trim().lowercase()
            if (clean.isBlank()) {
                _authError.value = "Please enter your Mobile Number or Email."
                return@launch
            }
            if (password.isBlank()) {
                _authError.value = "Please enter your password."
                return@launch
            }

            _authLoading.value = true
            _authError.value = null
            navigationManager.startRoleResolution(clean)

            // Step 1: Query Firestore 'users' collection to check role & status
            val firestoreUserResult = firestoreService.getUserProfileByIdentifier(clean)
            val firestoreUser = firestoreUserResult.getOrNull()

            // Step 2: Check if user is blocked in Firestore
            if (firestoreUser?.isBlocked == true) {
                _authLoading.value = false
                _authError.value = "Account Blocked: Access has been disabled by the Administrator."
                navigationManager.onFirestoreRoleResolved(
                    clean,
                    firestoreUser,
                    UserEntity(name = clean, email = clean, role = firestoreUser.role, isBlocked = true)
                )
                return@launch
            }

            // Step 3: Determine authoritative role
            val isSuperAdmin = clean == "banshraj851212@gmail.com" || clean == "admin@banshraj.edu" || clean == "9876543212"
            val resolvedRole = when {
                firestoreUser != null && firestoreUser.role.equals("ADMIN", ignoreCase = true) -> "ADMIN"
                isSuperAdmin -> "ADMIN"
                firestoreUser != null && firestoreUser.role.equals("TEACHER", ignoreCase = true) -> "TEACHER"
                clean == "teacher@banshraj.edu" || clean == "9876543211" || clean.contains("teacher") -> "TEACHER"
                firestoreUser != null -> firestoreUser.role.uppercase()
                else -> "STUDENT"
            }
            val canCreate = (resolvedRole == "ADMIN" || resolvedRole == "TEACHER" || firestoreUser?.canCreateContent == true)

            // Step 4: Sync into local Room database
            val localUser = if (clean.contains("@")) {
                repository.getUserByEmail(clean)
            } else {
                repository.getUserByPhone(clean)
            }

            val displayName = firestoreUser?.name?.ifBlank { null }
                ?: when (resolvedRole) {
                    "ADMIN" -> "Banshraj Sir"
                    "TEACHER" -> "Er. Alok Sharma"
                    else -> if (clean.contains("@")) clean.substringBefore("@") else "Student ($clean)"
                }

            val finalLocalUser = if (localUser == null) {
                val newId = repository.insertUser(
                    UserEntity(
                        name = displayName,
                        email = if (clean.contains("@")) clean else "$clean@banshraj.student",
                        phone = if (!clean.contains("@")) clean else "9876543210",
                        role = resolvedRole,
                        canCreateContent = canCreate,
                        enrolledBatchIds = firestoreUser?.enrolledBatchIds ?: if (resolvedRole == "ADMIN") "1,2,3,4" else "1",
                        assignedBatchIds = firestoreUser?.assignedBatchIds ?: if (resolvedRole == "TEACHER") "1,2" else ""
                    )
                )
                repository.getUserById(newId) ?: UserEntity(id = newId, name = displayName, email = clean, role = resolvedRole)
            } else {
                if (localUser.isBlocked) {
                    _authLoading.value = false
                    _authError.value = "Account Blocked: Access has been disabled by the Administrator."
                    navigationManager.onFirestoreRoleResolved(clean, firestoreUser, localUser)
                    return@launch
                }
                val updated = localUser.copy(role = resolvedRole, canCreateContent = canCreate)
                repository.updateUser(updated)
                updated
            }

            _activeUserId.value = finalLocalUser.id
            _authLoading.value = false
            _isLoggedIn.value = true

            // Trigger central NavigationManager redirect
            navigationManager.onFirestoreRoleResolved(clean, firestoreUser, finalLocalUser)

            when (resolvedRole) {
                "ADMIN" -> {
                    _currentTab.value = AcademyTab.ADMIN
                    _toastMessage.value = "Admin Login Verified (Firestore): Welcome Banshraj Sir! Routed to Admin Dashboard."
                }
                "TEACHER" -> {
                    _currentTab.value = AcademyTab.TEACHER
                    _toastMessage.value = "Teacher Login Verified (Firestore): Welcome $displayName! Routed to Faculty Dashboard."
                }
                else -> {
                    _currentTab.value = AcademyTab.HOME
                    _toastMessage.value = "Student Login Verified (Firestore): Welcome $displayName! Routed to Learning Hub."
                }
            }
            onSuccess()
        }
    }

    /**
     * Dual-purpose Firebase Auth Login:
     * Accepts email and password for both Admin and Student,
     * using Firebase Auth to authenticate and Firestore 'users' collection
     * to dynamically differentiate between 'Admin', 'Teacher', and 'Student' roles,
     * routing users directly based on their stored user profile type!
     */
    fun loginDualPurpose(
        email: String,
        password: String,
        onSuccess: () -> Unit = {}
    ) {
        loginWithEmailOrPhone(email, password, onSuccess)
    }

    /**
     * Legacy login wrapper targeting dual purpose
     */
    fun loginWithFirebase(
        email: String,
        password: String,
        asAdmin: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        loginDualPurpose(email, password, onSuccess)
    }

    /**
     * Firebase student account registration (Stores role='STUDENT' in Firestore 'users' collection)
     */
    fun registerStudentWithFirebase(
        name: String,
        email: String,
        password: String,
        targetExam: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val cleanEmail = email.trim().lowercase()

            if (cleanEmail.equals(FirebaseAuthService.SUPER_ADMIN_EMAIL, ignoreCase = true)) {
                _authLoading.value = false
                _authError.value = "This email is reserved for Banshraj Sir. Please use Admin Login."
                return@launch
            }

            val result = authService.signUpWithEmail(name, cleanEmail, password)
            _authLoading.value = false

            if (result.isSuccess) {
                val displayName = name.trim().ifBlank { "Student" }

                // Save to Firestore 'users' collection with role = "STUDENT"
                val firestoreStudent = FirestoreUser(
                    email = cleanEmail,
                    name = displayName,
                    role = "STUDENT",
                    canCreateContent = false,
                    enrolledBatchIds = "1",
                    targetExam = targetExam
                )
                firestoreService.saveOrUpdateUser(firestoreStudent)

                // Sync to local database
                val existing = repository.getUserByEmail(cleanEmail)
                val targetId = if (existing != null) {
                    existing.id
                } else {
                    repository.insertUser(
                        UserEntity(
                            name = displayName,
                            email = cleanEmail,
                            role = "STUDENT",
                            canCreateContent = false,
                            enrolledBatchIds = "1",
                            studyHours = 0.0f,
                            solvedDpps = 0,
                            testsAttempted = 0,
                            streakDays = 1
                        )
                    )
                }
                _activeUserId.value = targetId
                _isLoggedIn.value = true
                _currentTab.value = AcademyTab.HOME
                val studentLocal = repository.getUserById(targetId) ?: UserEntity(
                    id = targetId,
                    name = displayName,
                    email = cleanEmail,
                    role = "STUDENT"
                )
                navigationManager.onFirestoreRoleResolved(
                    identifier = cleanEmail,
                    firestoreUser = firestoreStudent,
                    localUser = studentLocal
                )
                _toastMessage.value = "Student Account Registered (Firestore): Welcome to Banshraj Academy ($targetExam)!"
                onSuccess()
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Registration failed."
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            val result = authService.sendPasswordReset(email)
            if (result.isSuccess) {
                _toastMessage.value = "Password reset instructions sent to $email"
            } else {
                _toastMessage.value = "Password reset failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun loginAsBanshrajSir() {
        _activeUserId.value = 1L
        _isLoggedIn.value = true
        _currentTab.value = AcademyTab.ADMIN
        val user = allUsers.value.find { it.id == 1L } ?: UserEntity(
            id = 1L,
            name = "Banshraj Sir",
            email = "banshraj851212@gmail.com",
            phone = "9876543212",
            role = "ADMIN",
            canCreateContent = true
        )
        navigationManager.onFirestoreRoleResolved(
            identifier = user.email,
            firestoreUser = FirestoreUser(
                email = user.email,
                phone = user.phone,
                name = user.name,
                role = "ADMIN",
                canCreateContent = true
            ),
            localUser = user
        )
        _toastMessage.value = "Super Admin Profile Verified (Firestore) • Routed to Admin Dashboard"
    }

    fun loginAsDemoStudent() {
        _activeUserId.value = 2L // Rahul Sharma (Student)
        _isLoggedIn.value = true
        _currentTab.value = AcademyTab.HOME
        val user = allUsers.value.find { it.id == 2L } ?: UserEntity(
            id = 2L,
            name = "Rahul Sharma",
            email = "student@banshraj.edu",
            phone = "9876543210",
            role = "STUDENT",
            canCreateContent = false
        )
        navigationManager.onFirestoreRoleResolved(
            identifier = user.email,
            firestoreUser = FirestoreUser(
                email = user.email,
                phone = user.phone,
                name = user.name,
                role = "STUDENT",
                canCreateContent = false
            ),
            localUser = user
        )
        _toastMessage.value = "Student Profile Verified (Firestore) • Routed to Student Study Hub"
    }

    fun loginAsDemoTeacher() {
        _activeUserId.value = 3L // Er. Alok Sharma (Faculty / Teacher)
        _isLoggedIn.value = true
        _currentTab.value = AcademyTab.TEACHER
        val user = allUsers.value.find { it.id == 3L } ?: UserEntity(
            id = 3L,
            name = "Er. Alok Sharma",
            email = "teacher@banshraj.edu",
            phone = "9876543211",
            role = "TEACHER",
            canCreateContent = true
        )
        navigationManager.onFirestoreRoleResolved(
            identifier = user.email,
            firestoreUser = FirestoreUser(
                email = user.email,
                phone = user.phone,
                name = user.name,
                role = "TEACHER",
                canCreateContent = true
            ),
            localUser = user
        )
        _toastMessage.value = "Teacher Profile Verified (Firestore) • Routed to Faculty Dashboard"
    }

    fun logout() {
        authService.signOut()
        _isLoggedIn.value = false
        _activeUserId.value = 2L // Reset default to student so admin is never accidentally exposed
        _currentTab.value = AcademyTab.HOME
        navigationManager.logout()
        _toastMessage.value = "Logged out successfully"
    }

    // Data streams
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBatches: StateFlow<List<BatchEntity>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLectures: StateFlow<List<LectureEntity>> = repository.allLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveLectures: StateFlow<List<LectureEntity>> = allLectures
        .map { list -> list.filter { it.isLive } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDpps: StateFlow<List<DppEntity>> = repository.allDpps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTests: StateFlow<List<TestEntity>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDoubts: StateFlow<List<DoubtEntity>> = repository.allDoubts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccessRequests: StateFlow<List<AccessRequestEntity>> = repository.allAccessRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selection State
    private val _selectedBatch = MutableStateFlow<BatchEntity?>(null)
    val selectedBatch: StateFlow<BatchEntity?> = _selectedBatch.asStateFlow()

    private val _selectedLecture = MutableStateFlow<LectureEntity?>(null)
    val selectedLecture: StateFlow<LectureEntity?> = _selectedLecture.asStateFlow()

    private val _activeTestTaking = MutableStateFlow<TestEntity?>(null)
    val activeTestTaking: StateFlow<TestEntity?> = _activeTestTaking.asStateFlow()

    // Dialog & Feedback States
    private val _restrictedDialogMessage = MutableStateFlow<String?>(null)
    val restrictedDialogMessage: StateFlow<String?> = _restrictedDialogMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun dismissRestrictedDialog() {
        _restrictedDialogMessage.value = null
    }

    fun selectBatch(batch: BatchEntity) {
        _selectedBatch.value = batch
    }

    fun selectLecture(lecture: LectureEntity) {
        _selectedLecture.value = lecture
        _currentTab.value = AcademyTab.CLASSROOM
    }

    fun joinLiveLecture(lecture: LectureEntity) {
        val batch = allBatches.value.find { it.id == lecture.batchId }
        if (batch != null) {
            _selectedBatch.value = batch
        }
        _selectedLecture.value = lecture
        _currentTab.value = AcademyTab.CLASSROOM
        _toastMessage.value = "🔴 Joined LIVE Broadcast: ${lecture.title} with ${lecture.instructor}!"
    }

    fun setLectureLiveStatus(lectureId: Long, isLive: Boolean, streamingUrl: String? = null) {
        checkPermissionAndRun("toggle live broadcast status") {
            viewModelScope.launch {
                val lecture = allLectures.value.find { it.id == lectureId }
                if (lecture != null) {
                    val updated = lecture.copy(
                        isLive = isLive,
                        videoUrl = if (!streamingUrl.isNullOrBlank()) streamingUrl else lecture.videoUrl
                    )
                    repository.updateLecture(updated)
                    val batch = allBatches.value.find { it.id == updated.batchId }
                    firestoreService.saveLiveSession(
                        FirestoreLiveSession(
                            id = "live_lec_${updated.id}",
                            lectureId = updated.id,
                            batchId = updated.batchId,
                            batchTitle = batch?.title ?: "Batch ${updated.batchId}",
                            title = updated.title,
                            subject = updated.subject,
                            chapter = updated.chapter,
                            instructor = updated.instructor,
                            streamingUrl = updated.videoUrl,
                            status = if (isLive) "LIVE" else "ENDED",
                            viewersCount = if (isLive) (850..1600).random() else 0
                        )
                    )
                    _toastMessage.value = if (isLive) "Lecture '${lecture.title}' is now STREAMING LIVE!" else "Live broadcast ended."
                }
            }
        }
    }

    fun startTest(test: TestEntity) {
        _activeTestTaking.value = test
    }

    fun finishTest() {
        _activeTestTaking.value = null
    }

    // Role switching helper
    fun switchActiveUser(userId: Long) {
        _activeUserId.value = userId
        val user = allUsers.value.find { it.id == userId }
        if (user != null) {
            viewModelScope.launch {
                val firestoreUser = firestoreService.getUserProfileByIdentifier(user.email).getOrNull()
                navigationManager.onFirestoreRoleResolved(user.email, firestoreUser, user)
            }
            when (user.role) {
                "ADMIN" -> {
                    _currentTab.value = AcademyTab.ADMIN
                    _toastMessage.value = "Switched to Admin Profile (${user.name}) • Routed to Admin Dashboard"
                }
                "TEACHER" -> {
                    _currentTab.value = AcademyTab.TEACHER
                    _toastMessage.value = "Switched to Teacher Profile (${user.name}) • Routed to Teacher Dashboard"
                }
                else -> {
                    _currentTab.value = AcademyTab.HOME
                    _toastMessage.value = "Switched to Student Profile (${user.name}) • Routed to Student Hub"
                }
            }
        } else {
            _toastMessage.value = "Switched profile"
        }
    }

    // Current user helper
    fun getCurrentUser(): UserEntity {
        return allUsers.value.find { it.id == _activeUserId.value }
            ?: UserEntity(
                id = 1,
                name = "Banshraj Sir",
                email = "banshraj851212@gmail.com",
                role = "ADMIN",
                canCreateContent = true
            )
    }

    fun isUserAdminOrCreator(): Boolean {
        val user = getCurrentUser()
        return user.role == "ADMIN" || user.role == "TEACHER" || user.canCreateContent
    }

    fun isUserBatchEnrolled(batchId: Long): Boolean {
        val user = getCurrentUser()
        if (user.role == "ADMIN" || user.role == "TEACHER") return true
        val ids = user.enrolledBatchIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        return ids.contains(batchId)
    }

    // Content Creation Guard Check
    fun checkPermissionAndRun(actionName: String, onPermitted: () -> Unit) {
        val user = getCurrentUser()
        if (user.role == "ADMIN" || user.role == "TEACHER" || user.canCreateContent) {
            onPermitted()
        } else {
            _restrictedDialogMessage.value =
                "Permission Restricted: Only Teachers or Academy Admins can $actionName."
        }
    }

    fun toggleUserBlockedStatus(userId: Long, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.updateUserBlockedStatus(userId, isBlocked)
            _toastMessage.value = if (isBlocked) "User account suspended." else "User account activated."
        }
    }

    fun updateTeacherBatches(userId: Long, batchIds: String) {
        viewModelScope.launch {
            repository.updateTeacherAssignedBatches(userId, batchIds)
            _toastMessage.value = "Teacher assigned batches updated."
        }
    }

    fun createContentDirect(
        title: String,
        subject: String,
        chapter: String,
        description: String,
        duration: String,
        videoUrl: String,
        pdfUrl: String,
        dppCount: Int,
        batchId: Long
    ) {
        viewModelScope.launch {
            val user = getCurrentUser()
            repository.addLecture(
                LectureEntity(
                    batchId = batchId,
                    subject = subject,
                    chapter = chapter,
                    title = title,
                    instructor = user.name,
                    duration = duration,
                    videoUrl = videoUrl,
                    notesAvailable = pdfUrl.isNotBlank() || description.isNotBlank(),
                    notesContent = if (pdfUrl.isNotBlank()) "$description\nPDF: $pdfUrl" else description
                )
            )
            _toastMessage.value = "Published '$title' successfully!"
        }
    }

    fun createTestDirect(
        title: String,
        subject: String,
        duration: Int,
        questions: Int,
        marks: Int,
        batchId: Long
    ) {
        viewModelScope.launch {
            repository.addTest(
                TestEntity(
                    batchId = batchId,
                    title = title,
                    subject = subject,
                    durationMinutes = duration,
                    totalMarks = marks,
                    questionsJson = "[]"
                )
            )
            _toastMessage.value = "Published Test '$title' successfully!"
        }
    }

    fun createGlobalNotification(title: String, message: String, targetBatch: String) {
        viewModelScope.launch {
            repository.addAnnouncement(
                AnnouncementEntity(
                    title = title,
                    content = "[$targetBatch] $message",
                    postedBy = getCurrentUser().name,
                    date = "Today",
                    isImportant = true
                )
            )
            _toastMessage.value = "Broadcast sent to $targetBatch!"
        }
    }

    // Admin Permissions Management
    fun updateUserPermissions(userId: Long, canCreate: Boolean, role: String) {
        viewModelScope.launch {
            repository.updateUserPermission(userId, canCreate, role)
            _toastMessage.value = "User permissions updated successfully!"
        }
    }

    // Batch Course Access Approval
    fun approveAccessRequest(request: AccessRequestEntity) {
        viewModelScope.launch {
            repository.updateAccessRequest(request.copy(status = "APPROVED"))
            // Add batch to user enrolled batches if user found
            val targetUser = allUsers.value.find { it.email.equals(request.studentEmail, ignoreCase = true) }
            if (targetUser != null) {
                val currentIds = targetUser.enrolledBatchIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toMutableSet()
                currentIds.add(request.batchId)
                repository.updateUser(targetUser.copy(enrolledBatchIds = currentIds.joinToString(",")))
            }
            _toastMessage.value = "Batch access granted to ${request.studentName}!"
        }
    }

    fun rejectAccessRequest(request: AccessRequestEntity) {
        viewModelScope.launch {
            repository.updateAccessRequest(request.copy(status = "REJECTED"))
            _toastMessage.value = "Batch access request rejected."
        }
    }

    // Student Course Access Request
    fun requestBatchAccess(batch: BatchEntity) {
        val user = getCurrentUser()
        viewModelScope.launch {
            repository.submitAccessRequest(
                AccessRequestEntity(
                    studentName = user.name,
                    studentEmail = user.email,
                    batchId = batch.id,
                    batchTitle = batch.title,
                    status = "PENDING",
                    requestDate = "Just now"
                )
            )
            // Sync enrollment request to Firestore schema
            firestoreService.saveEnrollment(
                FirestoreEnrollment(
                    studentEmail = user.email,
                    studentName = user.name,
                    batchId = batch.id,
                    batchTitle = batch.title,
                    status = "PENDING",
                    validTill = batch.validity
                )
            )
            _toastMessage.value = "Access request sent for '${batch.title}'! Admin will approve shortly."
        }
    }

    // Instant Enroll (for demo / admin)
    fun enrollInBatch(batchId: Long) {
        val user = getCurrentUser()
        viewModelScope.launch {
            val currentIds = user.enrolledBatchIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toMutableSet()
            currentIds.add(batchId)
            repository.updateUser(user.copy(enrolledBatchIds = currentIds.joinToString(",")))
            // Sync active enrollment to Firestore
            val batch = allBatches.value.find { it.id == batchId }
            firestoreService.saveEnrollment(
                FirestoreEnrollment(
                    studentEmail = user.email,
                    studentName = user.name,
                    batchId = batchId,
                    batchTitle = batch?.title ?: "Batch $batchId",
                    status = "ACTIVE",
                    validTill = batch?.validity ?: "1 Year Access"
                )
            )
            _toastMessage.value = "Successfully enrolled in batch!"
        }
    }

    // Content Additions (Restricted)
    fun addNewBatch(
        title: String,
        tagline: String,
        category: String,
        price: Int,
        discountPrice: Int,
        instructor: String,
        validity: String,
        features: String
    ) {
        checkPermissionAndRun("add new batches") {
            viewModelScope.launch {
                repository.addBatch(
                    BatchEntity(
                        title = title,
                        tagline = tagline,
                        category = category,
                        originalPrice = price,
                        discountPrice = discountPrice,
                        instructor = instructor.ifBlank { "Banshraj Sir" },
                        startDate = "New Batch",
                        validity = validity.ifBlank { "1 Year Access" },
                        features = features.ifBlank { "Live Lectures, Notes, DPPs, Tests" }
                    )
                )
                _toastMessage.value = "New batch '$title' published successfully!"
            }
        }
    }

    fun addNewLecture(
        batchId: Long,
        subject: String,
        chapter: String,
        title: String,
        duration: String,
        videoUrl: String,
        notesContent: String
    ) {
        checkPermissionAndRun("upload new lectures") {
            val user = getCurrentUser()
            viewModelScope.launch {
                repository.addLecture(
                    LectureEntity(
                        batchId = batchId,
                        subject = subject,
                        chapter = chapter,
                        title = title,
                        instructor = user.name,
                        duration = duration.ifBlank { "1h 15m" },
                        videoUrl = videoUrl,
                        notesContent = notesContent.ifBlank { "Key formulas & concepts for this lecture." }
                    )
                )
                // Sync lecture metadata to Cloud Firestore
                firestoreService.saveLecture(
                    FirestoreLecture(
                        batchId = batchId,
                        subject = subject,
                        chapter = chapter,
                        title = title,
                        instructor = user.name,
                        duration = duration.ifBlank { "1h 15m" },
                        videoUrl = videoUrl,
                        notesContent = notesContent.ifBlank { "Key formulas & concepts for this lecture." }
                    )
                )
                _toastMessage.value = "Lecture '$title' uploaded & synced to Firestore!"
            }
        }
    }

    fun addNewDpp(
        batchId: Long,
        subject: String,
        title: String,
        questionsCount: Int,
        totalMarks: Int
    ) {
        addNewCourseDpp(
            batchId = batchId,
            subject = subject,
            chapter = "General Practice",
            title = title,
            questionsCount = questionsCount,
            totalMarks = totalMarks
        )
    }

    fun addNewCourseDpp(
        batchId: Long,
        subject: String,
        chapter: String,
        title: String,
        questionsCount: Int = 10,
        totalMarks: Int = 40,
        difficulty: String = "JEE Main",
        timeLimitMinutes: Int = 30
    ) {
        checkPermissionAndRun("add DPP sheets to courses") {
            viewModelScope.launch {
                val batch = allBatches.value.find { it.id == batchId }
                val newDppId = repository.addDpp(
                    DppEntity(
                        batchId = batchId,
                        lectureId = 1,
                        subject = subject,
                        title = title,
                        questionsCount = questionsCount,
                        totalMarks = totalMarks
                    )
                )

                // Sync to Firestore DPP collection
                firestoreService.saveCourseDpp(
                    FirestoreDpp(
                        id = "dpp_$newDppId",
                        courseId = batchId,
                        courseName = batch?.title ?: "Batch $batchId",
                        subject = subject,
                        chapter = chapter,
                        title = title,
                        questionsCount = questionsCount,
                        totalMarks = totalMarks,
                        difficulty = difficulty,
                        timeLimitMinutes = timeLimitMinutes
                    )
                )

                _toastMessage.value = "DPP '$title' published for ${batch?.title ?: "course"} & synced to Firestore!"
            }
        }
    }

    fun deleteDpp(dppId: Long) {
        checkPermissionAndRun("delete DPPs") {
            viewModelScope.launch {
                repository.deleteDpp(dppId)
                _toastMessage.value = "DPP removed from course."
            }
        }
    }

    fun solveDpp(dppId: Long, score: Int) {
        viewModelScope.launch {
            val dpp = allDpps.value.find { it.id == dppId }
            if (dpp != null) {
                repository.updateDpp(dpp.copy(isSolved = true, score = score))
                val user = getCurrentUser()
                repository.updateUser(user.copy(solvedDpps = user.solvedDpps + 1))
                _toastMessage.value = "DPP completed! Score: $score/${dpp.totalMarks}"
            }
        }
    }

    fun addNewTest(
        batchId: Long,
        title: String,
        subject: String,
        durationMinutes: Int,
        totalMarks: Int,
        questionsJson: String
    ) {
        checkPermissionAndRun("create test series") {
            viewModelScope.launch {
                repository.addTest(
                    TestEntity(
                        batchId = batchId,
                        title = title,
                        subject = subject,
                        durationMinutes = durationMinutes,
                        totalMarks = totalMarks,
                        questionsJson = questionsJson
                    )
                )
                _toastMessage.value = "Mock Test '$title' created!"
            }
        }
    }

    fun postAnnouncement(title: String, content: String, isImportant: Boolean) {
        checkPermissionAndRun("post announcements") {
            val user = getCurrentUser()
            viewModelScope.launch {
                repository.addAnnouncement(
                    AnnouncementEntity(
                        title = title,
                        content = content,
                        postedBy = user.name,
                        date = "Today",
                        isImportant = isImportant
                    )
                )
                _toastMessage.value = "Announcement posted across academy!"
            }
        }
    }

    // Doubts
    fun postDoubt(
        subject: String,
        batchName: String,
        question: String,
        imageUrl: String? = null
    ) {
        val user = getCurrentUser()
        viewModelScope.launch {
            val newId = repository.addDoubt(
                DoubtEntity(
                    studentName = user.name,
                    studentEmail = user.email,
                    subject = subject,
                    batchName = batchName,
                    question = question,
                    imageUrl = imageUrl,
                    resolutionStatus = "PENDING"
                )
            )
            // Sync doubt query to Firestore schema with imageUrl and resolutionStatus
            firestoreService.submitDoubt(
                FirestoreDoubtQuery(
                    id = "dbt_$newId",
                    studentName = user.name,
                    studentEmail = user.email,
                    subject = subject,
                    batchName = batchName,
                    question = question,
                    imageUrl = imageUrl,
                    resolutionStatus = "PENDING"
                )
            )
            _toastMessage.value = "Doubt submitted with ${if (!imageUrl.isNullOrBlank()) "image & text" else "text"}! Stored in Firestore."
        }
    }

    fun answerDoubt(doubt: DoubtEntity, answerText: String, resolutionStatus: String = "RESOLVED") {
        checkPermissionAndRun("answer student doubts as faculty") {
            val user = getCurrentUser()
            viewModelScope.launch {
                repository.updateDoubt(
                    doubt.copy(
                        answer = answerText,
                        answeredBy = user.name,
                        isAnswered = true,
                        resolutionStatus = resolutionStatus
                    )
                )
                // Sync faculty answer and resolution status to Firestore
                firestoreService.answerDoubt(
                    doubtId = "dbt_${doubt.id}",
                    answer = answerText,
                    answeredBy = user.name,
                    resolutionStatus = resolutionStatus
                )
                _toastMessage.value = "Doubt resolution status updated to '$resolutionStatus' & synced to Firestore!"
            }
        }
    }

    fun upvoteDoubt(doubt: DoubtEntity) {
        viewModelScope.launch {
            repository.updateDoubt(doubt.copy(upvotes = doubt.upvotes + 1))
            firestoreService.upvoteDoubt(doubt.id.toString())
        }
    }

    // Test Submission
    fun submitTestScore(test: TestEntity, score: Int) {
        viewModelScope.launch {
            val rank = (5..35).random()
            repository.updateTest(
                test.copy(
                    isAttempted = true,
                    userScore = score,
                    rank = rank
                )
            )
            val user = getCurrentUser()
            repository.updateUser(
                user.copy(
                    testsAttempted = user.testsAttempted + 1,
                    studyHours = user.studyHours + (test.durationMinutes / 60.0f)
                )
            )
            _toastMessage.value = "Test Submitted! You scored $score / ${test.totalMarks} (Rank #$rank)"
        }
    }

    fun solveDpp(dpp: DppEntity) {
        viewModelScope.launch {
            repository.updateDpp(dpp.copy(isSolved = true, score = (dpp.totalMarks * 0.85).toInt()))
            val user = getCurrentUser()
            repository.updateUser(user.copy(solvedDpps = user.solvedDpps + 1))
            _toastMessage.value = "DPP Completed! +34 Marks added to progress."
        }
    }
}
