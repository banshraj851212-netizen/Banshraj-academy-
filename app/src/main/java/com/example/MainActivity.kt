package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthSplashScreen
import com.example.ui.components.RestrictedAccessDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BatchesScreen
import com.example.ui.screens.ClassroomScreen
import com.example.ui.screens.DoubtsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TestSeriesScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BanshrajAcademyTheme
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyTab
import com.example.ui.viewmodel.AcademyViewModel

import com.example.ui.screens.CentralAuthStateRedirector

class MainActivity : ComponentActivity() {
    private val viewModel: AcademyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BanshrajAcademyTheme {
                val isSessionChecking by viewModel.isSessionChecking.collectAsStateWithLifecycle()

                if (isSessionChecking) {
                    AuthSplashScreen()
                } else {
                    CentralAuthStateRedirector(
                        viewModel = viewModel,
                        navigationManager = viewModel.navigationManager
                    )
                }
            }
        }
    }
}

@Composable
fun BanshrajAcademyApp(viewModel: AcademyViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeTestSession by viewModel.activeTestTaking.collectAsStateWithLifecycle()
    val restrictedMsg by viewModel.restrictedDialogMessage.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val accessRequests by viewModel.allAccessRequests.collectAsStateWithLifecycle()
    val currentUser = viewModel.getCurrentUser()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingRequestsCount = accessRequests.count { it.status == "PENDING" }

    // Toast and feedback effect
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Enforce tab routing based on stored user profile role
    LaunchedEffect(currentUser.role, currentTab) {
        if (currentUser.role != "ADMIN" && currentTab == AcademyTab.ADMIN) {
            viewModel.setTab(AcademyTab.HOME)
        }
    }

    // Hardware Back Button Navigation Handler
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
            // Hide bottom bar if student is actively taking a timed test
            if (activeTestSession == null) {
                NavigationBar(
                    containerColor = RoyalNavy,
                    contentColor = Color.White,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("academy_bottom_navigation")
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.HOME,
                        onClick = { viewModel.setTab(AcademyTab.HOME) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_home")
                    )

                    // Batches
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.BATCHES,
                        onClick = { viewModel.setTab(AcademyTab.BATCHES) },
                        icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Batches") },
                        label = { Text("Batches", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_batches")
                    )

                    // Classroom
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.CLASSROOM,
                        onClick = { viewModel.setTab(AcademyTab.CLASSROOM) },
                        icon = { Icon(imageVector = Icons.Default.PlayCircleFilled, contentDescription = "Classroom") },
                        label = { Text("Study", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_classroom")
                    )

                    // Test Series
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.TESTS,
                        onClick = { viewModel.setTab(AcademyTab.TESTS) },
                        icon = { Icon(imageVector = Icons.Default.Quiz, contentDescription = "Test Series") },
                        label = { Text("Tests", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_tests")
                    )

                    // Doubts
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.DOUBTS,
                        onClick = { viewModel.setTab(AcademyTab.DOUBTS) },
                        icon = { Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Doubts") },
                        label = { Text("Doubts", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_doubts")
                    )

                    // Teacher Hub (Accessible when Teacher or Admin)
                    if (currentUser.role == "TEACHER") {
                        NavigationBarItem(
                            selected = currentTab == AcademyTab.TEACHER,
                            onClick = { viewModel.setTab(AcademyTab.TEACHER) },
                            icon = { Icon(imageVector = Icons.Default.School, contentDescription = "Teacher Hub") },
                            label = { Text("Teacher", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = navItemColors(),
                            modifier = Modifier.testTag("nav_teacher")
                        )
                    }

                    // Admin Dashboard (Accessible when Admin)
                    if (currentUser.role == "ADMIN") {
                        NavigationBarItem(
                            selected = currentTab == AcademyTab.ADMIN,
                            onClick = { viewModel.setTab(AcademyTab.ADMIN) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (pendingRequestsCount > 0) {
                                            Badge(containerColor = AmberGold) {
                                                Text("$pendingRequestsCount", color = RoyalNavy, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                                }
                            },
                            label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = navItemColors(),
                            modifier = Modifier.testTag("nav_admin")
                        )
                    }

                    // Profile
                    NavigationBarItem(
                        selected = currentTab == AcademyTab.PROFILE,
                        onClick = { viewModel.setTab(AcademyTab.PROFILE) },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_profile")
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
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    AcademyTab.HOME -> HomeScreen(viewModel = viewModel)
                    AcademyTab.BATCHES -> BatchesScreen(viewModel = viewModel)
                    AcademyTab.CLASSROOM -> ClassroomScreen(viewModel = viewModel)
                    AcademyTab.TESTS -> TestSeriesScreen(viewModel = viewModel)
                    AcademyTab.DOUBTS -> DoubtsScreen(viewModel = viewModel)
                    AcademyTab.TEACHER -> {
                        if (currentUser.role == "TEACHER" || currentUser.role == "ADMIN") {
                            com.example.ui.screens.TeacherDashboardScreen(viewModel = viewModel)
                        } else {
                            HomeScreen(viewModel = viewModel)
                        }
                    }
                    AcademyTab.ADMIN -> {
                        if (currentUser.role == "ADMIN") {
                            AdminDashboardScreen(viewModel = viewModel)
                        } else {
                            HomeScreen(viewModel = viewModel)
                        }
                    }
                    AcademyTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Role-Based Restricted Access Dialog
    restrictedMsg?.let { msg ->
        RestrictedAccessDialog(
            message = msg,
            onDismiss = { viewModel.dismissRestrictedDialog() },
            onSwitchToAdmin = {
                // Switch to Banshraj Sir (Admin User id 1)
                viewModel.switchActiveUser(1L)
            }
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = RoyalNavy,
    selectedTextColor = AmberGold,
    indicatorColor = AmberGold,
    unselectedIconColor = Color.White.copy(alpha = 0.6f),
    unselectedTextColor = Color.White.copy(alpha = 0.6f)
)
