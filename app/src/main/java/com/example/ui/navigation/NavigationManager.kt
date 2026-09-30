package com.example.ui.navigation

import androidx.compose.runtime.Immutable
import com.example.data.firestore.schema.FirestoreUser
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Standard User Roles supported by Banshraj Academy.
 */
enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN;

    companion object {
        fun fromString(roleStr: String?): UserRole {
            return when (roleStr?.trim()?.uppercase()) {
                "ADMIN" -> ADMIN
                "TEACHER", "FACULTY", "INSTRUCTOR" -> TEACHER
                else -> STUDENT
            }
        }
    }
}

/**
 * Represents the distinct dashboard composites tailored to each role.
 */
enum class DashboardCompositeType {
    STUDENT_DASHBOARD,
    TEACHER_DASHBOARD,
    ADMIN_DASHBOARD
}

/**
 * Central sealed authentication state representing the full lifecycle:
 * Unauthenticated -> Authenticating -> ResolvingRole (Firestore check) -> Authenticated (redirected) | Blocked | Error
 */
@Immutable
sealed interface CentralAuthState {
    object Unauthenticated : CentralAuthState

    data class Authenticating(val identifier: String) : CentralAuthState

    data class ResolvingRole(
        val identifier: String,
        val statusMessage: String = "Verifying role in Firestore..."
    ) : CentralAuthState

    data class Authenticated(
        val user: UserEntity,
        val firestoreUser: FirestoreUser?,
        val role: UserRole,
        val currentComposite: DashboardCompositeType,
        val permissions: Set<String> = emptySet()
    ) : CentralAuthState

    data class Blocked(
        val reason: String,
        val identifier: String
    ) : CentralAuthState

    data class AuthError(
        val errorMessage: String
    ) : CentralAuthState
}

/**
 * Central Navigation & AuthState Manager for Banshraj Academy.
 * Strictly verifies role from Firestore after login and enforces role-based redirection.
 */
class NavigationManager {

    private val _authState = MutableStateFlow<CentralAuthState>(CentralAuthState.Unauthenticated)
    val authState: StateFlow<CentralAuthState> = _authState.asStateFlow()

    private val _securityViolations = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val securityViolations: SharedFlow<String> = _securityViolations.asSharedFlow()

    /**
     * Called immediately after user enters credentials or OTP.
     * Transitions state to ResolvingRole while Firestore 'role' check is performed.
     */
    fun startRoleResolution(identifier: String) {
        _authState.value = CentralAuthState.ResolvingRole(identifier)
    }

    /**
     * Evaluates the role from Firestore and automatically redirects to the specific
     * Student, Teacher, or Admin dashboard composite.
     */
    fun onFirestoreRoleResolved(
        identifier: String,
        firestoreUser: FirestoreUser?,
        localUser: UserEntity
    ) {
        // 1. Security Check: Is the account suspended or blocked?
        if (firestoreUser?.isBlocked == true || localUser.isBlocked) {
            _authState.value = CentralAuthState.Blocked(
                reason = "Your account has been suspended by the Academy Administrator. Please contact support@banshraj.edu",
                identifier = identifier
            )
            return
        }

        // 2. Determine authoritative role (prefer Firestore 'role' field, fallback to local user)
        val roleString = firestoreUser?.role ?: localUser.role
        val resolvedRole = UserRole.fromString(roleString)

        // 3. Map role to specific dashboard composite
        val targetComposite = when (resolvedRole) {
            UserRole.ADMIN -> DashboardCompositeType.ADMIN_DASHBOARD
            UserRole.TEACHER -> DashboardCompositeType.TEACHER_DASHBOARD
            UserRole.STUDENT -> DashboardCompositeType.STUDENT_DASHBOARD
        }

        // 4. Calculate permissions
        val permissions = when (resolvedRole) {
            UserRole.ADMIN -> setOf(
                "MANAGE_USERS", "BLOCK_ACCOUNTS", "ASSIGN_TEACHERS",
                "CREATE_BATCHES", "MANAGE_CONTENT", "VIEW_REVENUE", "BROADCAST_ALERTS"
            )
            UserRole.TEACHER -> setOf(
                "VIEW_ASSIGNED_BATCHES", "UPLOAD_LECTURES", "UPLOAD_NOTES",
                "CREATE_TESTS", "SCHEDULE_LIVE", "TAKE_ATTENDANCE", "GRADE_STUDENTS", "NOTIFY_CLASS"
            )
            UserRole.STUDENT -> setOf(
                "VIEW_ENROLLED_BATCHES", "WATCH_LECTURES", "READ_NOTES",
                "ATTEMPT_TESTS", "ASK_DOUBTS", "VIEW_PROGRESS"
            )
        }

        _authState.value = CentralAuthState.Authenticated(
            user = localUser.copy(role = resolvedRole.name),
            firestoreUser = firestoreUser,
            role = resolvedRole,
            currentComposite = targetComposite,
            permissions = permissions
        )
    }

    /**
     * Enforces role-based navigation guard.
     * Prevents students from opening Teacher or Admin composites, and Teachers from accessing Admin pages.
     */
    fun navigateToComposite(target: DashboardCompositeType): Boolean {
        val current = _authState.value
        if (current !is CentralAuthState.Authenticated) {
            _securityViolations.tryEmit("Unauthorized: Please log in first.")
            return false
        }

        val userRole = current.role

        // Strict role access checks
        when (target) {
            DashboardCompositeType.ADMIN_DASHBOARD -> {
                if (userRole != UserRole.ADMIN) {
                    _securityViolations.tryEmit("Access Denied: Only Academy Administrators can access the Admin Dashboard.")
                    return false
                }
            }
            DashboardCompositeType.TEACHER_DASHBOARD -> {
                if (userRole != UserRole.TEACHER && userRole != UserRole.ADMIN) {
                    _securityViolations.tryEmit("Access Denied: Only faculty teachers can access the Teacher Dashboard.")
                    return false
                }
            }
            DashboardCompositeType.STUDENT_DASHBOARD -> {
                // Allowed for Student, and permitted for Teacher/Admin for previewing curriculum
            }
        }

        _authState.value = current.copy(currentComposite = target)
        return true
    }

    /**
     * Resets auth state and logs user out safely.
     */
    fun logout() {
        _authState.value = CentralAuthState.Unauthenticated
    }

    /**
     * Sets error state on authentication failure.
     */
    fun setAuthError(message: String) {
        _authState.value = CentralAuthState.AuthError(message)
    }
}
