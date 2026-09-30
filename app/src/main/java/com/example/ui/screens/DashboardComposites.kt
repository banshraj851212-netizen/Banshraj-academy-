package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RestrictedAccessDialog
import com.example.ui.navigation.CentralAuthState
import com.example.ui.navigation.DashboardCompositeType
import com.example.ui.navigation.NavigationManager
import com.example.ui.navigation.UserRole
import com.example.ui.theme.AmberGold
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyTab
import com.example.ui.viewmodel.AcademyViewModel

/**
 * Top-level AuthState holder and redirector.
 * Checks the 'role' field in Firestore after login and renders the specific
 * Student, Teacher, or Admin dashboard composite.
 */
@Composable
fun CentralAuthStateRedirector(
    viewModel: AcademyViewModel,
    navigationManager: NavigationManager
) {
    val authState by navigationManager.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Collect security violations from NavigationManager
    LaunchedEffect(Unit) {
        navigationManager.securityViolations.collect { violationMsg ->
            Toast.makeText(context, "⚠️ $violationMsg", Toast.LENGTH_LONG).show()
        }
    }

    when (val state = authState) {
        is CentralAuthState.Unauthenticated -> {
            AuthScreen(viewModel = viewModel)
        }

        is CentralAuthState.Authenticating -> {
            ResolvingRoleSplash(
                title = "Authenticating...",
                subtitle = "Checking credentials for ${state.identifier}"
            )
        }

        is CentralAuthState.ResolvingRole -> {
            ResolvingRoleSplash(
                title = "Verifying Role in Firestore...",
                subtitle = state.statusMessage
            )
        }

        is CentralAuthState.Blocked -> {
            BlockedAccountComposite(
                reason = state.reason,
                identifier = state.identifier,
                onLogout = {
                    navigationManager.logout()
                    viewModel.logout()
                }
            )
        }

        is CentralAuthState.AuthError -> {
            AuthErrorComposite(
                errorMessage = state.errorMessage,
                onRetry = {
                    navigationManager.logout()
                    viewModel.logout()
                }
            )
        }

        is CentralAuthState.Authenticated -> {
            // Render the specific composite decided by the verified Firestore 'role'
            when (state.currentComposite) {
                DashboardCompositeType.STUDENT_DASHBOARD -> {
                    StudentDashboardComposite(
                        viewModel = viewModel,
                        navigationManager = navigationManager
                    )
                }

                DashboardCompositeType.TEACHER_DASHBOARD -> {
                    TeacherDashboardComposite(
                        viewModel = viewModel,
                        navigationManager = navigationManager
                    )
                }

                DashboardCompositeType.ADMIN_DASHBOARD -> {
                    AdminDashboardComposite(
                        viewModel = viewModel,
                        navigationManager = navigationManager
                    )
                }
            }
        }
    }
}

/**
 * Visual splash shown while Firestore is queried for the user's role.
 */
@Composable
fun ResolvingRoleSplash(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoyalNavy),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(RoyalNavy.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = RoyalNavy,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RoyalNavy,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                CircularProgressIndicator(
                    color = AmberGold,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/**
 * Screen displayed when an account is blocked by the Administrator.
 */
@Composable
fun BlockedAccountComposite(
    reason: String,
    identifier: String,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(
                    text = "Account Suspended",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Red
                )
                Text(
                    text = reason,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Identifier: $identifier",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Login")
                }
            }
        }
    }
}

/**
 * Screen displayed when an authentication error occurs.
 */
@Composable
fun AuthErrorComposite(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Authentication Error",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
                Text(
                    errorMessage,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}

// =========================================================================
// 1. STUDENT DASHBOARD COMPOSITE (Learning Portal)
// =========================================================================
@Composable
fun StudentDashboardComposite(
    viewModel: AcademyViewModel,
    navigationManager: NavigationManager
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeTestSession by viewModel.activeTestTaking.collectAsStateWithLifecycle()
    val restrictedMsg by viewModel.restrictedDialogMessage.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val currentUser = viewModel.getCurrentUser()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Role-based protection: Student is locked to student tabs
    LaunchedEffect(currentTab) {
        if (currentTab == AcademyTab.ADMIN || currentTab == AcademyTab.TEACHER) {
            viewModel.setTab(AcademyTab.HOME)
        }
    }

    BackHandler(enabled = activeTestSession != null || currentTab != AcademyTab.HOME) {
        if (activeTestSession != null) {
            viewModel.finishTest()
        } else if (currentTab != AcademyTab.HOME) {
            viewModel.setTab(AcademyTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (activeTestSession == null) {
                NavigationBar(
                    containerColor = RoyalNavy,
                    contentColor = Color.White,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("student_bottom_navigation")
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.HOME,
                        onClick = { viewModel.setTab(AcademyTab.HOME) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_student_home")
                    )

                    // Batches
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.BATCHES,
                        onClick = { viewModel.setTab(AcademyTab.BATCHES) },
                        icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Batches") },
                        label = { Text("Batches", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_student_batches")
                    )

                    // Classroom / Study
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.CLASSROOM,
                        onClick = { viewModel.setTab(AcademyTab.CLASSROOM) },
                        icon = { Icon(imageVector = Icons.Default.PlayCircleFilled, contentDescription = "Classroom") },
                        label = { Text("Study", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_student_classroom")
                    )

                    // Tests
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.TESTS,
                        onClick = { viewModel.setTab(AcademyTab.TESTS) },
                        icon = { Icon(imageVector = Icons.Default.Quiz, contentDescription = "Tests") },
                        label = { Text("Tests", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_student_tests")
                    )

                    // Doubts
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.DOUBTS,
                        onClick = { viewModel.setTab(AcademyTab.DOUBTS) },
                        icon = { Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Doubts") },
                        label = { Text("Doubts", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_student_doubts")
                    )

                    // Profile
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.PROFILE,
                        onClick = { viewModel.setTab(AcademyTab.PROFILE) },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_student_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "StudentTabTransition"
            ) { tab ->
                when (tab) {
                    AcademyTab.HOME -> HomeScreen(viewModel = viewModel)
                    AcademyTab.BATCHES -> BatchesScreen(viewModel = viewModel)
                    AcademyTab.CLASSROOM -> ClassroomScreen(viewModel = viewModel)
                    AcademyTab.TESTS -> TestSeriesScreen(viewModel = viewModel)
                    AcademyTab.DOUBTS -> DoubtsScreen(viewModel = viewModel)
                    AcademyTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    restrictedMsg?.let { msg ->
        RestrictedAccessDialog(
            message = msg,
            onDismiss = { viewModel.dismissRestrictedDialog() },
            onSwitchToAdmin = {
                viewModel.switchActiveUser(1L)
            }
        )
    }
}

// =========================================================================
// 2. TEACHER DASHBOARD COMPOSITE (Faculty Management Portal)
// =========================================================================
@Composable
fun TeacherDashboardComposite(
    viewModel: AcademyViewModel,
    navigationManager: NavigationManager
) {
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    TeacherDashboardScreen(
        viewModel = viewModel,
        onBack = {
            // Allows teacher to switch to student curriculum view or log out
            navigationManager.navigateToComposite(DashboardCompositeType.STUDENT_DASHBOARD)
        }
    )
}

// =========================================================================
// 3. ADMIN DASHBOARD COMPOSITE (Academy Super Admin Portal)
// =========================================================================
@Composable
fun AdminDashboardComposite(
    viewModel: AcademyViewModel,
    navigationManager: NavigationManager
) {
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    AdminDashboardScreen(
        viewModel = viewModel,
        onBack = {
            // Allow admin to preview student portal
            navigationManager.navigateToComposite(DashboardCompositeType.STUDENT_DASHBOARD)
        }
    )
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = RoyalNavy,
    selectedTextColor = AmberGold,
    indicatorColor = AmberGold,
    unselectedIconColor = Color.White.copy(alpha = 0.6f),
    unselectedTextColor = Color.White.copy(alpha = 0.6f)
)
