package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyViewModel

data class StudentAttendanceRecord(
    val studentId: Long,
    val name: String,
    val rollNo: String,
    val batch: String,
    var status: String = "PRESENT", // PRESENT, ABSENT, LATE
    val scorePercent: Int = 85,
    val dppCompletionRate: Int = 92
)

data class TeacherScheduledClass(
    val id: String,
    val topic: String,
    val subject: String,
    val batch: String,
    val date: String,
    val time: String,
    val meetLink: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: AcademyViewModel,
    onBack: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser = viewModel.getCurrentUser()
    val allBatches by viewModel.allBatches.collectAsStateWithLifecycle()
    val allLectures by viewModel.allLectures.collectAsStateWithLifecycle()
    val allTests by viewModel.allTests.collectAsStateWithLifecycle()

    // Modals
    var showUploadLectureDialog by remember { mutableStateOf(false) }
    var showUploadNotesDialog by remember { mutableStateOf(false) }
    var showCreateTestDialog by remember { mutableStateOf(false) }
    var showScheduleLiveDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }

    // Dynamic Teacher Scheduled Classes
    val scheduledClasses = remember {
        mutableStateListOf(
            TeacherScheduledClass(
                id = "live_1",
                topic = "Rotational Dynamics - Moment of Inertia Advanced",
                subject = "Physics",
                batch = "Lakshya JEE 2026",
                date = "Today",
                time = "06:00 PM",
                meetLink = "https://meet.google.com/banshraj-live-jee"
            ),
            TeacherScheduledClass(
                id = "live_2",
                topic = "Thermodynamics & Carnot Engine Problem Solving",
                subject = "Physics",
                batch = "Arjuna NEET 2026",
                date = "Tomorrow",
                time = "04:30 PM",
                meetLink = "https://meet.google.com/banshraj-live-neet"
            )
        )
    }

    // Dynamic Attendance & Students
    val studentRecords = remember {
        mutableStateListOf(
            StudentAttendanceRecord(101L, "Rahul Sharma", "JEE-2601", "Lakshya JEE 2026", "PRESENT", 88, 95),
            StudentAttendanceRecord(102L, "Priya Patel", "JEE-2602", "Lakshya JEE 2026", "PRESENT", 92, 98),
            StudentAttendanceRecord(103L, "Amit Verma", "JEE-2603", "Lakshya JEE 2026", "LATE", 74, 80),
            StudentAttendanceRecord(104L, "Sneha Singh", "NEET-2601", "Arjuna NEET 2026", "PRESENT", 95, 100),
            StudentAttendanceRecord(105L, "Karan Gupta", "NEET-2602", "Arjuna NEET 2026", "ABSENT", 62, 70),
            StudentAttendanceRecord(106L, "Anjali Yadav", "JEE-2604", "Lakshya JEE 2026", "PRESENT", 84, 90)
        )
    }

    val tabs = listOf(
        "Overview",
        "My Batches",
        "Lectures & Notes",
        "Live Classes",
        "Tests & DPPs",
        "Attendance & Marks"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Teacher Management Portal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            "Faculty: ${currentUser.name} • ${currentUser.phone}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAnnouncementDialog = true },
                        modifier = Modifier.testTag("btn_teacher_broadcast")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Send Announcement",
                            tint = Color(0xFFFFD54F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20) // Forest Green for Teacher portal
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        2 -> showUploadLectureDialog = true
                        3 -> showScheduleLiveDialog = true
                        4 -> showCreateTestDialog = true
                        else -> showUploadLectureDialog = true
                    }
                },
                containerColor = EmeraldGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_teacher_quick_action")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Quick Action")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF4F7F6))
        ) {
            // Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1B5E20),
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("teacher_tab_$index")
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> TeacherOverviewContent(
                    currentUser = currentUser,
                    batchesCount = allBatches.size,
                    lecturesCount = allLectures.size,
                    testsCount = allTests.size,
                    studentsCount = studentRecords.size,
                    onNavigate = { tabIdx -> selectedTab = tabIdx },
                    onUploadLecture = { showUploadLectureDialog = true },
                    onUploadNotes = { showUploadNotesDialog = true },
                    onScheduleLive = { showScheduleLiveDialog = true },
                    onCreateTest = { showCreateTestDialog = true }
                )
                1 -> TeacherBatchesContent(batches = allBatches)
                2 -> TeacherLecturesContent(
                    lectures = allLectures,
                    onAddLecture = { showUploadLectureDialog = true },
                    onAddNotes = { showUploadNotesDialog = true }
                )
                3 -> TeacherLiveClassesContent(
                    scheduledClasses = scheduledClasses,
                    onScheduleLive = { showScheduleLiveDialog = true }
                )
                4 -> TeacherTestsContent(
                    tests = allTests,
                    onCreateTest = { showCreateTestDialog = true }
                )
                5 -> TeacherAttendanceMarksContent(
                    studentRecords = studentRecords,
                    onStatusChange = { id, newStatus ->
                        val index = studentRecords.indexOfFirst { it.studentId == id }
                        if (index != -1) {
                            studentRecords[index] = studentRecords[index].copy(status = newStatus)
                        }
                    }
                )
            }
        }
    }

    // Modal: Upload Lecture
    if (showUploadLectureDialog) {
        TeacherUploadLectureModal(
            onDismiss = { showUploadLectureDialog = false },
            onUpload = { title, subject, duration, chapter, link ->
                viewModel.createContentDirect(
                    title = title,
                    subject = subject,
                    chapter = chapter,
                    description = "Uploaded by ${currentUser.name} for Banshraj Academy",
                    duration = duration,
                    videoUrl = link,
                    pdfUrl = "",
                    dppCount = 10,
                    batchId = 1L
                )
                showUploadLectureDialog = false
            }
        )
    }

    // Modal: Upload Notes
    if (showUploadNotesDialog) {
        TeacherUploadNotesModal(
            onDismiss = { showUploadNotesDialog = false },
            onUpload = { title, subject, chapter, pdfUrl ->
                viewModel.createContentDirect(
                    title = "$title [Class Notes PDF]",
                    subject = subject,
                    chapter = chapter,
                    description = "Official faculty lecture notes and DPP handouts",
                    duration = "PDF Handout",
                    videoUrl = "",
                    pdfUrl = pdfUrl,
                    dppCount = 5,
                    batchId = 1L
                )
                showUploadNotesDialog = false
            }
        )
    }

    // Modal: Schedule Live Class
    if (showScheduleLiveDialog) {
        TeacherScheduleLiveModal(
            onDismiss = { showScheduleLiveDialog = false },
            onSchedule = { topic, subject, batch, date, time, link ->
                scheduledClasses.add(
                    TeacherScheduledClass(
                        id = "live_${System.currentTimeMillis()}",
                        topic = topic,
                        subject = subject,
                        batch = batch,
                        date = date,
                        time = time,
                        meetLink = link
                    )
                )
                showScheduleLiveDialog = false
            }
        )
    }

    // Modal: Create Test
    if (showCreateTestDialog) {
        TeacherCreateTestModal(
            onDismiss = { showCreateTestDialog = false },
            onCreate = { title, subject, questions, duration, marks ->
                viewModel.createTestDirect(
                    title = title,
                    subject = subject,
                    duration = duration,
                    questions = questions,
                    marks = marks,
                    batchId = 1L
                )
                showCreateTestDialog = false
            }
        )
    }

    // Modal: Send Broadcast Notification
    if (showAnnouncementDialog) {
        TeacherBroadcastModal(
            onDismiss = { showAnnouncementDialog = false },
            onSend = { title, message ->
                viewModel.createGlobalNotification(
                    title = "[Faculty Update] $title",
                    message = message,
                    targetBatch = "All Assigned Batches"
                )
                showAnnouncementDialog = false
            }
        )
    }
}

// -------------------------------------------------------------
// 1. OVERVIEW TAB
// -------------------------------------------------------------
@Composable
private fun TeacherOverviewContent(
    currentUser: com.example.data.local.entity.UserEntity,
    batchesCount: Int,
    lecturesCount: Int,
    testsCount: Int,
    studentsCount: Int,
    onNavigate: (Int) -> Unit,
    onUploadLecture: () -> Unit,
    onUploadNotes: () -> Unit,
    onScheduleLive: () -> Unit,
    onCreateTest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Namaste, ${currentUser.name} Sir!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            "Role: Faculty & Subject Expert • Physics & Mathematics",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Stats Matrix
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeacherStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Assigned Batches",
                    value = "$batchesCount",
                    icon = Icons.Default.Class,
                    color = Color(0xFF2E7D32),
                    onClick = { onNavigate(1) }
                )
                TeacherStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Uploaded Content",
                    value = "$lecturesCount",
                    icon = Icons.Default.VideoLibrary,
                    color = Color(0xFF1565C0),
                    onClick = { onNavigate(2) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeacherStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Tests",
                    value = "$testsCount",
                    icon = Icons.Default.Quiz,
                    color = Color(0xFFC2185B),
                    onClick = { onNavigate(4) }
                )
                TeacherStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Enrolled Students",
                    value = "$studentsCount",
                    icon = Icons.Default.Group,
                    color = Color(0xFFE65100),
                    onClick = { onNavigate(5) }
                )
            }
        }

        // Quick Teacher Actions
        item {
            Text(
                "Teacher Quick Management Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2C3E50)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeacherActionRow(
                        title = "Upload Video Lecture",
                        subtitle = "Add recorded class with YouTube/Drive link & DPPs",
                        icon = Icons.Default.VideoLibrary,
                        color = Color(0xFF1B5E20),
                        onClick = onUploadLecture,
                        tag = "action_upload_lecture"
                    )
                    TeacherActionRow(
                        title = "Upload Notes & PDF",
                        subtitle = "Share chapter summary, formulas & class notes",
                        icon = Icons.Default.Description,
                        color = Color(0xFF0277BD),
                        onClick = onUploadNotes,
                        tag = "action_upload_notes"
                    )
                    TeacherActionRow(
                        title = "Schedule Live Class",
                        subtitle = "Set date, time & Google Meet / YouTube live link",
                        icon = Icons.Default.LiveTv,
                        color = Color(0xFFD84315),
                        onClick = onScheduleLive,
                        tag = "action_schedule_live"
                    )
                    TeacherActionRow(
                        title = "Create Test & Quiz",
                        subtitle = "Define chapter test, questions, timer & marks",
                        icon = Icons.Default.Assignment,
                        color = Color(0xFF6A1B9A),
                        onClick = onCreateTest,
                        tag = "action_create_test"
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun TeacherActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = color)
    }
}

// -------------------------------------------------------------
// 2. MY BATCHES TAB
// -------------------------------------------------------------
@Composable
private fun TeacherBatchesContent(
    batches: List<com.example.data.local.entity.BatchEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "My Assigned Batches & Courses (${batches.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1B5E20)
            )
        }

        items(batches) { batch ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(batch.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                "Assigned Faculty",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(batch.tagline, fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Category: ${batch.category}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Fee: ₹${batch.discountPrice}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalNavy)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. LECTURES & NOTES TAB
// -------------------------------------------------------------
@Composable
private fun TeacherLecturesContent(
    lectures: List<com.example.data.local.entity.LectureEntity>,
    onAddLecture: () -> Unit,
    onAddNotes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Uploaded Lectures & Materials (${lectures.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddNotes,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+ Notes", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onAddLecture,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+ Lecture", fontSize = 11.sp)
                    }
                }
            }
        }

        items(lectures) { lecture ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(lecture.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F8E9)
                        ) {
                            Text(
                                lecture.subject,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF33691E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Chapter: ${lecture.chapter} • Duration: ${lecture.duration}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    if (lecture.notesAvailable) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0277BD),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Notes & Formula Sheet Available", fontSize = 11.sp, color = Color(0xFF0277BD))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. LIVE CLASSES TAB
// -------------------------------------------------------------
@Composable
private fun TeacherLiveClassesContent(
    scheduledClasses: List<TeacherScheduledClass>,
    onScheduleLive: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Scheduled Live Classes (${scheduledClasses.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20)
                )
                Button(
                    onClick = onScheduleLive,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Schedule Live", fontSize = 12.sp)
                }
            }
        }

        items(scheduledClasses) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "LIVE CLASS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            }
                        }
                        Text("${item.date} • ${item.time}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalNavy)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(item.topic, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Batch: ${item.batch} • Subject: ${item.subject}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { /* launch stream */ },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.LiveTv, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start / Broadcast Session", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. TESTS & DPPS TAB
// -------------------------------------------------------------
@Composable
private fun TeacherTestsContent(
    tests: List<com.example.data.local.entity.TestEntity>,
    onCreateTest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Teacher Tests & Quizzes (${tests.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20)
                )
                Button(
                    onClick = onCreateTest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("+ Create Test", fontSize = 12.sp)
                }
            }
        }

        items(tests) { test ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(test.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3E5F5)
                        ) {
                            Text(
                                "${test.totalMarks} Marks",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Subject: ${test.subject} • ${test.durationMinutes} Mins • Total Marks: ${test.totalMarks}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. ATTENDANCE & MARKS TAB
// -------------------------------------------------------------
@Composable
private fun TeacherAttendanceMarksContent(
    studentRecords: List<StudentAttendanceRecord>,
    onStatusChange: (Long, String) -> Unit
) {
    val presentCount = studentRecords.count { it.status == "PRESENT" }
    val absentCount = studentRecords.count { it.status == "ABSENT" }
    val lateCount = studentRecords.count { it.status == "LATE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$presentCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                        Text("Present", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$absentCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Red)
                        Text("Absent", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$lateCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE65100))
                        Text("Late", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        item {
            Text(
                "Students & Performance List (${studentRecords.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1B5E20)
            )
        }

        items(studentRecords) { record ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(record.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Roll: ${record.rollNo} • ${record.batch}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        // Attendance Toggle
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AttendanceBadge("P", record.status == "PRESENT", Color(0xFF2E7D32)) {
                                onStatusChange(record.studentId, "PRESENT")
                            }
                            AttendanceBadge("A", record.status == "ABSENT", Color.Red) {
                                onStatusChange(record.studentId, "ABSENT")
                            }
                            AttendanceBadge("L", record.status == "LATE", Color(0xFFE65100)) {
                                onStatusChange(record.studentId, "LATE")
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Test Avg: ${record.scorePercent}%", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("DPP Solved: ${record.dppCompletionRate}%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceBadge(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (isSelected) color else Color(0xFFEEEEEE))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.DarkGray
        )
    }
}

// -------------------------------------------------------------
// TEACHER MODALS & DIALOGS
// -------------------------------------------------------------
@Composable
private fun TeacherUploadLectureModal(
    onDismiss: () -> Unit,
    onUpload: (title: String, subject: String, duration: String, chapter: String, link: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45 mins") }
    var link by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Video Lecture", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Lecture Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (Physics/Chemistry/Maths/Biology)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 50 mins)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Video URL / YouTube Embed Link") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && chapter.isNotBlank()) {
                        onUpload(title, subject, duration, chapter, link)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text("Publish Lecture")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TeacherUploadNotesModal(
    onDismiss: () -> Unit,
    onUpload: (title: String, subject: String, chapter: String, pdfUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var pdfUrl by remember { mutableStateOf("https://banshraj.edu/notes/physics_sample.pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Notes / PDF Handout", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notes Title (e.g. Kinematics Formula Sheet)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pdfUrl,
                    onValueChange = { pdfUrl = it },
                    label = { Text("PDF Document Link") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onUpload(title, subject, chapter, pdfUrl)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
            ) {
                Text("Publish Notes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TeacherScheduleLiveModal(
    onDismiss: () -> Unit,
    onSchedule: (topic: String, subject: String, batch: String, date: String, time: String, link: String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var batch by remember { mutableStateOf("Lakshya JEE 2026") }
    var date by remember { mutableStateOf("Tomorrow") }
    var time by remember { mutableStateOf("05:00 PM") }
    var link by remember { mutableStateOf("https://meet.google.com/live-banshraj") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Live Class", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Class Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Target Batch") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Live Meet / Stream Link") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onSchedule(topic, subject, batch, date, time, link)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315))
            ) {
                Text("Schedule & Notify")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TeacherCreateTestModal(
    onDismiss: () -> Unit,
    onCreate: (title: String, subject: String, questions: Int, duration: Int, marks: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var questions by remember { mutableStateOf("25") }
    var duration by remember { mutableStateOf("60") }
    var marks by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Test / Quiz", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Test Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = questions,
                        onValueChange = { questions = it },
                        label = { Text("Questions") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (Mins)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = marks,
                    onValueChange = { marks = it },
                    label = { Text("Total Marks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val q = questions.toIntOrNull() ?: 20
                        val d = duration.toIntOrNull() ?: 60
                        val m = marks.toIntOrNull() ?: 100
                        onCreate(title, subject, q, d, m)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Text("Publish Test")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TeacherBroadcastModal(
    onDismiss: () -> Unit,
    onSend: (title: String, message: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast Class Notification", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message / Instructions for Students") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onSend(title, message)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text("Send Alert")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
