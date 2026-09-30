package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyTab
import com.example.ui.viewmodel.AcademyViewModel

data class SubjectDashboardItem(
    val name: String,
    val tagline: String,
    val instructor: String,
    val completedLectures: Int,
    val totalLectures: Int,
    val nextTopic: String,
    val accentColor: Color,
    val icon: ImageVector,
    val pendingDpps: Int = 2
) {
    val progress: Float
        get() = if (totalLectures > 0) completedLectures.toFloat() / totalLectures else 0f
}

enum class AcademicActivityType {
    LECTURE,
    DPP,
    TEST,
    DOUBT
}

data class RecentActivityItem(
    val id: String,
    val type: AcademicActivityType,
    val subject: String,
    val title: String,
    val detail: String,
    val timestamp: String,
    val statusBadge: String,
    val statusColor: Color,
    val actionText: String
)

/**
 * High-fidelity Student Dashboard UI presenting:
 * 1. Academic Performance Header & Daily Streak
 * 2. Enrolled Subjects with syllabus progress and next topics
 * 3. Recent Academic Activity Summary (Lectures, DPPs, Tests, Doubts)
 * 4. Quick-Resume Learning Card
 */
@Composable
fun StudentDashboardSection(
    viewModel: AcademyViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    var subjectSearchQuery by remember { mutableStateOf("") }
    var selectedActivityFilter by remember { mutableStateOf("All") }

    // Subjects enrolled by the student
    val enrolledSubjects = remember {
        listOf(
            SubjectDashboardItem(
                name = "Physics",
                tagline = "Electrostatics & Mechanics Mastery",
                instructor = "Banshraj Sir",
                completedLectures = 8,
                totalLectures = 10,
                nextTopic = "L02: Electric Field Lines & Dipole in Uniform Field",
                accentColor = Color(0xFF2563EB),
                icon = Icons.Default.Bolt,
                pendingDpps = 1
            ),
            SubjectDashboardItem(
                name = "Chemistry",
                tagline = "Physical, Organic & Inorganic",
                instructor = "Dr. Ananya Sen",
                completedLectures = 6,
                totalLectures = 10,
                nextTopic = "Crystal Field Theory & Isomerism",
                accentColor = Color(0xFFD97706),
                icon = Icons.Default.Science,
                pendingDpps = 2
            ),
            SubjectDashboardItem(
                name = "Mathematics",
                tagline = "Integral Calculus & Differential Equations",
                instructor = "Er. Vikram Verma",
                completedLectures = 7,
                totalLectures = 10,
                nextTopic = "Integration by Parts & Definite Integrals",
                accentColor = Color(0xFF7C3AED),
                icon = Icons.Default.Calculate,
                pendingDpps = 3
            ),
            SubjectDashboardItem(
                name = "Biology / Foundation",
                tagline = "Genetics & Human Physiology",
                instructor = "Dr. Ritu Rathore",
                completedLectures = 5,
                totalLectures = 8,
                nextTopic = "DNA Replication & Transcription Mechanisms",
                accentColor = EmeraldGreen,
                icon = Icons.Default.Psychology,
                pendingDpps = 1
            )
        )
    }

    // Recent Academic Activities Summary
    val recentActivities = remember {
        listOf(
            RecentActivityItem(
                id = "act_1",
                type = AcademicActivityType.LECTURE,
                subject = "Physics",
                title = "Coulomb's Law & Principle of Superposition",
                detail = "Watched 1h 18m • Notes Downloaded",
                timestamp = "Today, 10:15 AM",
                statusBadge = "Completed",
                statusColor = EmeraldGreen,
                actionText = "Revise Notes"
            ),
            RecentActivityItem(
                id = "act_2",
                type = AcademicActivityType.DPP,
                subject = "Physics",
                title = "DPP #01: Coulomb's Law Numerical Challenge",
                detail = "Score: 38/40 (95% Accuracy) • 10/10 Solved",
                timestamp = "Yesterday, 07:45 PM",
                statusBadge = "Solved 🎯",
                statusColor = Color(0xFF2563EB),
                actionText = "View Solution"
            ),
            RecentActivityItem(
                id = "act_3",
                type = AcademicActivityType.TEST,
                subject = "All-India Test",
                title = "JEE Main Part Test 01 (Physics + Chem)",
                detail = "Rank: AIR 142 • Score: 104/120 (86%)",
                timestamp = "2 Days Ago",
                statusBadge = "Submitted 🏅",
                statusColor = AmberGold,
                actionText = "Leaderboard"
            ),
            RecentActivityItem(
                id = "act_4",
                type = AcademicActivityType.DOUBT,
                subject = "Physics Doubt",
                title = "Direction of Induced EMF in Lenz's Law",
                detail = "Banshraj Sir answered with handwritten derivation",
                timestamp = "3 Days Ago",
                statusBadge = "Resolved 🟢",
                statusColor = EmeraldGreen,
                actionText = "View Answer"
            ),
            RecentActivityItem(
                id = "act_5",
                type = AcademicActivityType.LECTURE,
                subject = "Chemistry",
                title = "Coordination Compounds Nomenclature & CFT",
                detail = "Watched 45m of 1h 20m • In Progress",
                timestamp = "4 Days Ago",
                statusBadge = "Resume",
                statusColor = Color(0xFFD97706),
                actionText = "Play"
            )
        )
    }

    val filteredActivities = recentActivities.filter { activity ->
        when (selectedActivityFilter) {
            "Lectures" -> activity.type == AcademicActivityType.LECTURE
            "DPPs" -> activity.type == AcademicActivityType.DPP
            "Tests" -> activity.type == AcademicActivityType.TEST
            "Doubts" -> activity.type == AcademicActivityType.DOUBT
            else -> true
        }
    }

    val filteredSubjects = enrolledSubjects.filter { subject ->
        subjectSearchQuery.isBlank() ||
        subject.name.contains(subjectSearchQuery, ignoreCase = true) ||
        subject.tagline.contains(subjectSearchQuery, ignoreCase = true) ||
        subject.instructor.contains(subjectSearchQuery, ignoreCase = true) ||
        subject.nextTopic.contains(subjectSearchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_dashboard_section")
    ) {
        // ========================================================
        // 0. QUICK ENROLLED SUBJECTS SEARCH BAR (AT TOP OF DASHBOARD)
        // ========================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("dashboard_subject_search_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = subjectSearchQuery,
                    onValueChange = { subjectSearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_subject_search_input"),
                    placeholder = {
                        Text(
                            text = "Filter enrolled subjects, faculty, or topics...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Subjects",
                            tint = RoyalNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (subjectSearchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { subjectSearchQuery = "" },
                                modifier = Modifier.testTag("clear_subject_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search filter",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedBorderColor = RoyalNavy,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Quick Subject Filter Shortcuts
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickTags = listOf("All", "Physics", "Chemistry", "Mathematics", "Biology")
                    items(quickTags) { tag ->
                        val isSelected = if (tag == "All") subjectSearchQuery.isBlank() else subjectSearchQuery.equals(tag, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RoyalNavy else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable {
                                    subjectSearchQuery = if (tag == "All") "" else tag
                                }
                                .testTag("quick_subject_chip_${tag.lowercase()}")
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ========================================================
        // 1. STUDENT ACADEMIC STATS & STREAK HERO CARD
        // ========================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("student_stats_hero_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                RoyalNavy,
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Student Learning Dashboard",
                                color = AmberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Welcome back, ${currentUser.name}! 🚀",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Daily Streak Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AmberGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = AmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser.streakDays}d Streak",
                                    color = AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Column Academic Performance Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill(
                            icon = Icons.Default.Timer,
                            value = "${currentUser.studyHours}h",
                            label = "Study Time",
                            accentColor = Color(0xFF60A5FA)
                        )
                        MetricPill(
                            icon = Icons.Default.AssignmentTurnedIn,
                            value = "${currentUser.solvedDpps}",
                            label = "DPPs Solved",
                            accentColor = EmeraldGreen
                        )
                        MetricPill(
                            icon = Icons.Default.Quiz,
                            value = "${currentUser.testsAttempted}",
                            label = "Tests Done",
                            accentColor = AmberGold
                        )
                        MetricPill(
                            icon = Icons.Default.TrendingUp,
                            value = "84%",
                            label = "Avg Score",
                            accentColor = Color(0xFFA78BFA)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overall Syllabus Gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Syllabus Coverage",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "72% Completed",
                            color = AmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { 0.72f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AmberGold,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // ========================================================
        // 2. RESUME LEARNING IN-PROGRESS BANNER
        // ========================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { viewModel.setTab(AcademyTab.CLASSROOM) }
                .testTag("resume_learning_banner"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RoyalNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Resume Lecture",
                        tint = AmberGold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF2563EB).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PHYSICS • CONTINUE",
                                color = Color(0xFF2563EB),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "22m left",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Electric Field Lines & Dipole in Uniform Field",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "By Banshraj Sir • Chapter: Electrostatics",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                Button(
                    onClick = { viewModel.setTab(AcademyTab.CLASSROOM) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                }
            }
        }

        // ========================================================
        // 2.5 VISUAL PROGRESS TRACKER (CIRCULAR GAUGES FOR ENROLLED COURSES)
        // ========================================================
        SubjectCircularProgressTracker(
            subjects = enrolledSubjects,
            onSubjectClick = { subject ->
                subjectSearchQuery = subject.name
            }
        )

        // ========================================================
        // 3. ENROLLED SUBJECTS SECTION
        // ========================================================
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Enrolled Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your syllabus coverage & upcoming topics",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Surface(
                color = if (subjectSearchQuery.isNotBlank()) AmberGold.copy(alpha = 0.2f) else RoyalNavy.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (subjectSearchQuery.isNotBlank())
                        "${filteredSubjects.size} of ${enrolledSubjects.size} Found"
                    else
                        "${enrolledSubjects.size} Active Subjects",
                    color = if (subjectSearchQuery.isNotBlank()) Color(0xFFB45309) else RoyalNavy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("filtered_subjects_count")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredSubjects.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("enrolled_subjects_row")
            ) {
                items(filteredSubjects) { subject ->
                    EnrolledSubjectCard(
                        subject = subject,
                        onOpenSubject = {
                            viewModel.setTab(AcademyTab.CLASSROOM)
                        }
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("no_subjects_found_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No enrolled subjects match \"$subjectSearchQuery\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try searching by subject name (e.g. Physics, Chemistry) or faculty (Banshraj Sir)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    OutlinedButton(
                        onClick = { subjectSearchQuery = "" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_reset_subject_search")
                    ) {
                        Text("Clear Search Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ========================================================
        // 4. SUMMARY OF RECENT ACADEMIC ACTIVITY
        // ========================================================
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recent Academic Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lectures watched, DPPs solved, and mock test scores",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    color = AmberGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Live Sync",
                        color = RoyalNavy,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Activity Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("All", "Lectures", "DPPs", "Tests", "Doubts")
                items(filters) { filter ->
                    val isSelected = selectedActivityFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedActivityFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalNavy,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_activity_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recent Activity Timeline Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredActivities.forEach { item ->
                    RecentActivityCard(
                        item = item,
                        onAction = {
                            when (item.type) {
                                AcademicActivityType.LECTURE -> viewModel.setTab(AcademyTab.CLASSROOM)
                                AcademicActivityType.DPP -> viewModel.setTab(AcademyTab.CLASSROOM)
                                AcademicActivityType.TEST -> viewModel.setTab(AcademyTab.TESTS)
                                AcademicActivityType.DOUBT -> viewModel.setTab(AcademyTab.DOUBTS)
                            }
                        }
                    )
                }
            }

            // ========================================================
            // 5. PUSH NOTIFICATION ALERT SETTINGS
            // ========================================================
            Spacer(modifier = Modifier.height(14.dp))
            StudentNotificationSettingsCard(viewModel = viewModel)
        }
    }
}

@Composable
private fun MetricPill(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun EnrolledSubjectCard(
    subject: SubjectDashboardItem,
    onOpenSubject: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenSubject)
            .testTag("subject_card_${subject.name.lowercase().replace(" ", "_").replace("/", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Subject Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(subject.accentColor, subject.accentColor.copy(alpha = 0.75f))
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = subject.icon,
                                contentDescription = subject.name,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = subject.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Faculty: ${subject.instructor}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${(subject.progress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Subject Content Body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = subject.tagline,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Circular Progress Bar & Completed Lessons Tracker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(52.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { subject.progress },
                            modifier = Modifier.size(52.dp),
                            color = subject.accentColor,
                            trackColor = subject.accentColor.copy(alpha = 0.15f),
                            strokeWidth = 5.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(subject.progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subject.completedLectures} of ${subject.totalLectures} Lessons",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(subject.progress * 100).toInt()}% Course Completed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = subject.accentColor
                        )
                        Text(
                            text = "${subject.pendingDpps} DPPs due • Syllabus On Track",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Next Topic Preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Next Topic:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = subject.accentColor
                        )
                        Text(
                            text = subject.nextTopic,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenSubject,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = subject.accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text("Continue Learning", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    item: RecentActivityItem,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onAction)
            .testTag("recent_activity_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.type) {
                        AcademicActivityType.LECTURE -> Icons.Default.PlayCircleFilled
                        AcademicActivityType.DPP -> Icons.Default.AssignmentTurnedIn
                        AcademicActivityType.TEST -> Icons.Default.Quiz
                        AcademicActivityType.DOUBT -> Icons.Default.HelpCenter
                    },
                    contentDescription = item.type.name,
                    tint = item.statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subject.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.statusColor
                    )
                    Text(
                        text = item.timestamp,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = item.statusColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, item.statusColor.copy(alpha = 0.3f)),
                modifier = Modifier.clickable(onClick = onAction)
            ) {
                Text(
                    text = item.actionText,
                    color = item.statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Visual Progress Tracker for student subjects showing the percentage of completed
 * lessons for each enrolled course using a circular progress bar.
 */
@Composable
fun SubjectCircularProgressTracker(
    subjects: List<SubjectDashboardItem>,
    onSubjectClick: (SubjectDashboardItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("subject_visual_progress_tracker"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(RoyalNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Visual Progress",
                            tint = RoyalNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Course Progress Tracker",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completed lessons percentage by subject",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Real-Time Sync",
                        color = EmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row of Circular Progress Gauges for all enrolled subjects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                subjects.forEach { subject ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSubjectClick(subject) }
                            .padding(4.dp)
                            .testTag("circular_progress_item_${subject.name.lowercase().replace(" ", "_").replace("/", "_")}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(62.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { subject.progress },
                                modifier = Modifier.size(62.dp),
                                color = subject.accentColor,
                                trackColor = subject.accentColor.copy(alpha = 0.15f),
                                strokeWidth = 5.5.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(subject.progress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = subject.name.split(" ").firstOrNull() ?: subject.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )

                        Text(
                            text = "${subject.completedLectures}/${subject.totalLectures} lessons",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

