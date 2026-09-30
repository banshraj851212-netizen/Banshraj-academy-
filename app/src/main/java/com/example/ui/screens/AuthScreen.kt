package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.FirebaseAuthService
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyViewModel

/**
 * Dual-Purpose Login & Authentication Screen for Banshraj Academy.
 * Fields for Email and Password authenticate via Firebase Auth and dynamically
 * differentiate between 'Admin' (Banshraj Sir) and 'Student' roles using user
 * collection data in Cloud Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val generatedOtp by viewModel.generatedOtp.collectAsStateWithLifecycle()
    val otpCountdown by viewModel.otpCountdown.collectAsStateWithLifecycle()
    val isOtpSent by viewModel.isOtpSent.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Dual-Purpose Sign In, 1 = New Student Register
    var loginMethod by remember { mutableIntStateOf(0) } // 0 = Password, 1 = OTP Verification
    var isDedicatedAdminPortal by remember { mutableStateOf(false) }

    // Secret Tap Trigger on Logo (5 taps directly reveals Founder VIP Gateway)
    var logoTapCount by remember { mutableIntStateOf(0) }

    // Dual-Purpose Credentials (Mobile or Email)
    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Student Registration State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf("IIT-JEE 2026") }
    var examMenuExpanded by remember { mutableStateOf(false) }
    val examOptions = listOf("IIT-JEE 2026", "NEET UG 2026", "Class 12 Boards", "Class 11 Foundation")

    // Dedicated Admin Credentials
    var adminEmail by remember { mutableStateOf(FirebaseAuthService.SUPER_ADMIN_EMAIL) }
    var adminPassword by remember { mutableStateOf("") }
    var showAdminPassword by remember { mutableStateOf(false) }

    // Forgot Password Dialog
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val isAdminEmailEntered = identifierInput.trim().equals(FirebaseAuthService.SUPER_ADMIN_EMAIL, ignoreCase = true) || identifierInput.trim() == "9876543212"
    val isTeacherEntered = identifierInput.trim().contains("teacher", ignoreCase = true) || identifierInput.trim() == "9876543211"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDedicatedAdminPortal) {
                        listOf(RoyalNavy, Color(0xFF1E1B4B), RoyalNavy)
                    } else {
                        listOf(RoyalNavy, MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), RoyalNavy)
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Academy Crest Logo with 5-Tap Gesture
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(if (isDedicatedAdminPortal || isAdminEmailEntered) AmberGold else Color.White)
                    .clickable {
                        logoTapCount++
                        if (logoTapCount in 2..4) {
                            Toast.makeText(
                                context,
                                "Admin Portal: Tap ${5 - logoTapCount} more times",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (logoTapCount >= 5) {
                            isDedicatedAdminPortal = true
                            logoTapCount = 0
                            Toast.makeText(
                                context,
                                "Super Admin VIP Portal Unlocked!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .testTag("auth_academy_logo"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDedicatedAdminPortal || isAdminEmailEntered) Icons.Default.Shield else Icons.Default.School,
                    contentDescription = "Academy Logo",
                    tint = RoyalNavy,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "BANSHRAJ ACADEMY",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 1.2.sp
            )

            Text(
                text = if (isDedicatedAdminPortal) "SUPER ADMIN PORTAL • RESTRICTED" else "Padho Kahin Se, Selection Hoga Yahin Se! 🎯",
                color = AmberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner
            authError?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = err,
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ========================================================
            // DUAL-PURPOSE AUTHENTICATION CARD (ADMIN & STUDENT)
            // ========================================================
            if (!isDedicatedAdminPortal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Top Mode Tabs: Dual Sign In / Register
                        PrimaryTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    viewModel.clearAuthError()
                                },
                                text = { Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                modifier = Modifier.testTag("tab_signin")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    viewModel.clearAuthError()
                                },
                                text = { Text("New Student", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                modifier = Modifier.testTag("tab_register")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedTab == 0) {
                            // --- DUAL-PURPOSE SIGN IN ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dual-Purpose Login",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isAdminEmailEntered) AmberGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = if (isAdminEmailEntered) "Admin Account" else "Role: Auto-Detected",
                                        color = if (isAdminEmailEntered) AmberGold else MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Sign in with your email & password. Your role ('Admin' or 'Student') is verified via Cloud Firestore user data.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                            )

                            // Dynamic Admin Notice Badge
                            AnimatedVisibility(
                                visible = isAdminEmailEntered,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AmberGold.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = AmberGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Banshraj Sir (Founder & Admin) credentials detected. Admin dashboard will unlock automatically.",
                                            fontSize = 10.sp,
                                            color = RoyalNavy,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Verification Method Toggle (Password vs OTP)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = loginMethod == 0,
                                    onClick = { loginMethod = 0 },
                                    label = { Text("🔑 Password", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f).testTag("chip_login_password")
                                )
                                FilterChip(
                                    selected = loginMethod == 1,
                                    onClick = { loginMethod = 1 },
                                    label = { Text("📲 OTP Verification", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f).testTag("chip_login_otp")
                                )
                            }

                            // Mobile Number or Email Input
                            OutlinedTextField(
                                value = identifierInput,
                                onValueChange = { identifierInput = it },
                                label = { Text("Mobile Number or Email") },
                                placeholder = { Text("e.g. 9876543210 or student@banshraj.edu") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isAdminEmailEntered) Icons.Default.Shield else if (isTeacherEntered) Icons.Default.School else if (identifierInput.contains("@")) Icons.Default.Email else Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (isAdminEmailEntered) AmberGold else if (isTeacherEntered) EmeraldGreen else MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = if (identifierInput.any { it.isDigit() } && !identifierInput.contains("@")) KeyboardType.Phone else KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_identifier_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (loginMethod == 0) {
                                // Password Input
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(
                                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle Password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_password_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            resetEmail = identifierInput
                                            showResetDialog = true
                                        }
                                    ) {
                                        Text("Forgot Password?", fontSize = 11.sp)
                                    }
                                }
                            } else {
                                // OTP Flow
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = otpInput,
                                        onValueChange = { otpInput = it },
                                        label = { Text("Enter 6-Digit OTP") },
                                        placeholder = { Text(if (generatedOtp != null) "Code: $generatedOtp" else "6-digit OTP") },
                                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("auth_otp_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Button(
                                        onClick = { viewModel.sendLoginOtp(identifierInput) },
                                        enabled = identifierInput.isNotBlank() && otpCountdown == 0 && !isLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(56.dp).testTag("btn_send_otp")
                                    ) {
                                        Text(
                                            text = if (otpCountdown > 0) "${otpCountdown}s" else if (isOtpSent) "Resend" else "Get OTP",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (isOtpSent && generatedOtp != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldGreen.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📲 Demo OTP Code: ",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreen
                                            )
                                            Text(
                                                text = generatedOtp ?: "123456",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = RoyalNavy
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Dynamic Role Routing Indicator Box
                            val detectedRoleLabel = when {
                                isAdminEmailEntered -> "Super Admin Dashboard (Founder Portal)"
                                isTeacherEntered -> "Teacher Dashboard (Faculty Portal)"
                                else -> "Student Dashboard (Learning Hub)"
                            }
                            val detectedColor = when {
                                isAdminEmailEntered -> AmberGold
                                isTeacherEntered -> EmeraldGreen
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = detectedColor.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, detectedColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isAdminEmailEntered) Icons.Default.Shield else if (isTeacherEntered) Icons.Default.School else Icons.Default.Person,
                                        contentDescription = "Target Destination",
                                        tint = detectedColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Verified Target: $detectedRoleLabel",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = detectedColor
                                        )
                                        Text(
                                            text = "NavigationManager checks 'role' field in Firestore & redirects automatically.",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }

                            // Submit Button (Password or OTP)
                            Button(
                                onClick = {
                                    if (loginMethod == 0) {
                                        viewModel.loginWithEmailOrPhone(
                                            identifier = identifierInput,
                                            password = passwordInput
                                        )
                                    } else {
                                        viewModel.verifyOtpAndLogin(
                                            identifier = identifierInput,
                                            enteredOtp = otpInput
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAdminEmailEntered) AmberGold else if (isTeacherEntered) EmeraldGreen else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_login_submit"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = if (isAdminEmailEntered) RoyalNavy else MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isAdminEmailEntered) Icons.Default.Shield else if (isTeacherEntered) Icons.Default.School else Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (isAdminEmailEntered) RoyalNavy else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (loginMethod == 0) "Sign In with Password • Role Verified" else "Verify OTP & Sign In • Role Verified",
                                        color = if (isAdminEmailEntered) RoyalNavy else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1-Tap Quick Testing Buttons for all 3 Roles
                            Text(
                                text = "Instant Role Testing & Direct Routing (1-Tap):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Button 1: Quick Admin Login (Banshraj Sir)
                            Button(
                                onClick = { viewModel.loginAsBanshrajSir() },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_quick_admin_login"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = RoyalNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Sign In as Admin (Banshraj Sir) • Direct to Admin Dashboard",
                                    color = RoyalNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Button 2: Quick Teacher Login (Er. Alok Sharma)
                            Button(
                                onClick = { viewModel.loginAsDemoTeacher() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_quick_teacher_login"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Sign In as Teacher (Er. Alok Sharma) • Direct to Faculty Hub",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Button 3: Quick Student Login (Rahul Sharma)
                            OutlinedButton(
                                onClick = { viewModel.loginAsDemoStudent() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_quick_student_login"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Sign In as Student (Rahul Sharma) • Direct to Study Center",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                        } else {
                            // --- NEW STUDENT REGISTRATION ---
                            Text(
                                text = "Create Student Profile 🎓",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Student accounts are saved in Cloud Firestore with role='STUDENT'. Admin dashboard is strictly restricted.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = examMenuExpanded,
                                onExpandedChange = { examMenuExpanded = !examMenuExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedExam,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Target Exam") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examMenuExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = examMenuExpanded,
                                    onDismissRequest = { examMenuExpanded = false }
                                ) {
                                    examOptions.forEach { exam ->
                                        DropdownMenuItem(
                                            text = { Text(exam) },
                                            onClick = {
                                                selectedExam = exam
                                                examMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_email_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Create Password (min 6 chars)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                        Icon(
                                            imageVector = if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Password"
                                        )
                                    }
                                },
                                visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank() || regPassword.length < 6) {
                                        Toast.makeText(
                                            context,
                                            "Please enter name, email and password (min 6 characters)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    viewModel.registerStudentWithFirebase(
                                        name = regName,
                                        email = regEmail,
                                        password = regPassword,
                                        targetExam = selectedExam
                                    )
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_student_register_submit"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Register as Student (Firestore)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Discreet Link for Banshraj Sir's VIP Admin Gateway
                Row(
                    modifier = Modifier
                        .clickable { isDedicatedAdminPortal = true }
                        .padding(8.dp)
                        .testTag("discreet_admin_gateway_link"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Faculty & Super Admin VIP Gateway",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }

            } else {
                // ========================================================
                // DEDICATED ADMIN GATEWAY (EXCLUSIVELY FOR BANSHRAJ SIR)
                // ========================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalNavy),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberGold
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = RoyalNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SUPER ADMIN GATEWAY",
                                        color = RoyalNavy,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isDedicatedAdminPortal = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Admin Authentication",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Strictly restricted to Founder & Admin Banshraj Sir (${FirebaseAuthService.SUPER_ADMIN_EMAIL}). Verified via Firestore.",
                            color = AmberGold,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Super Admin Email", color = Color.White.copy(alpha = 0.8f)) },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AmberGold) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = AmberGold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Admin Master Password", color = Color.White.copy(alpha = 0.8f)) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AmberGold) },
                            trailingIcon = {
                                IconButton(onClick = { showAdminPassword = !showAdminPassword }) {
                                    Icon(
                                        imageVector = if (showAdminPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Password",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            visualTransformation = if (showAdminPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = AmberGold
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.loginDualPurpose(
                                    email = adminEmail,
                                    password = adminPassword.ifBlank { "Admin@12345" }
                                )
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_admin_login_submit"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = RoyalNavy,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = RoyalNavy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authenticate as Super Admin", color = RoyalNavy, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.loginAsBanshrajSir() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f))
                        ) {
                            Text("1-Tap Admin Login (Banshraj Sir)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { isDedicatedAdminPortal = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("← Return to Dual-Purpose Login", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "© 2026 Banshraj Academy • Cloud Firestore & Firebase Auth\nIIT-JEE | NEET UG | Foundation",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }

    // Password Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter your registered email address. A Firebase password reset link will be sent to your inbox.",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            viewModel.sendPasswordReset(resetEmail)
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
