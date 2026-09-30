package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TestEntity
import com.example.ui.components.QuizPlayer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyViewModel

@Composable
fun TestSeriesScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val tests by viewModel.allTests.collectAsStateWithLifecycle()
    val activeTestSession by viewModel.activeTestTaking.collectAsStateWithLifecycle()
    var showCreateTestDialog by remember { mutableStateOf(false) }

    // If active test is being taken, show full screen quiz interface
    if (activeTestSession != null) {
        QuizPlayer(
            test = activeTestSession!!,
            onClose = { viewModel.finishTest() },
            onSubmitScore = { score ->
                viewModel.submitTestScore(activeTestSession!!, score)
            }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.checkPermissionAndRun("create a new test series") {
                        showCreateTestDialog = true
                    }
                },
                containerColor = AmberGold,
                contentColor = RoyalNavy,
                modifier = Modifier.testTag("create_test_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Test")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalNavy)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "All India Test Series (AITS)",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "NTA JEE & NEET standard timed tests with AIR ranking",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Test Rank",
                            tint = AmberGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Tests List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tests) { test ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_card_${test.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = test.subject.uppercase(),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (test.isAttempted) {
                                    Surface(
                                        color = EmeraldGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "AIR #${test.rank} • Score: ${test.userScore}/${test.totalMarks}",
                                                color = EmeraldGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = test.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Duration",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${test.durationMinutes} Mins",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Quiz,
                                            contentDescription = "Marks",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${test.totalMarks} Marks",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.startTest(test)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (test.isAttempted) MaterialTheme.colorScheme.primary else AmberGold
                                    ),
                                    modifier = Modifier.testTag("start_test_btn_${test.id}")
                                ) {
                                    Text(
                                        text = if (test.isAttempted) "Retake / Review" else "Start Test",
                                        color = if (test.isAttempted) Color.White else RoyalNavy,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Test Dialog (Admin / Creator)
    if (showCreateTestDialog) {
        var testTitle by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("Physics") }
        var durationStr by remember { mutableStateOf("45") }
        var marksStr by remember { mutableStateOf("120") }

        AlertDialog(
            onDismissRequest = { showCreateTestDialog = false },
            title = { Text("Create Mock Test", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = testTitle,
                        onValueChange = { testTitle = it },
                        label = { Text("Test Title (e.g. Unit Test 03: Thermodynamics)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (Physics / Chemistry / Mathematics)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duration (mins)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = marksStr,
                            onValueChange = { marksStr = it },
                            label = { Text("Total Marks") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (testTitle.isNotBlank()) {
                            val defaultQuestionsJson = """
                            [
                              {
                                "id": 1,
                                "question": "What happens to the capacitance of a parallel plate capacitor when a dielectric slab is inserted?",
                                "options": ["Increases", "Decreases", "Remains same", "Becomes zero"],
                                "correctIndex": 0,
                                "explanation": "Capacitance C = K * C0. The dielectric constant K is always greater than 1, so capacitance increases by a factor of K."
                              },
                              {
                                "id": 2,
                                "question": "The work done in moving a charge between two points on an equipotential surface is:",
                                "options": ["Zero", "Maximum", "Infinite", "Depends on path"],
                                "correctIndex": 0,
                                "explanation": "Work done W = q * Delta V. On an equipotential surface Delta V = 0, so work done is zero."
                              }
                            ]
                            """.trimIndent()

                            viewModel.addNewTest(
                                batchId = 1,
                                title = testTitle,
                                subject = subject,
                                durationMinutes = durationStr.toIntOrNull() ?: 45,
                                totalMarks = marksStr.toIntOrNull() ?: 120,
                                questionsJson = defaultQuestionsJson
                            )
                            showCreateTestDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy)
                ) {
                    Text("Publish Test")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateTestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
