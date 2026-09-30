package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.DoubtEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyViewModel

/**
 * 24x7 Doubt Clearing UI section where students can submit queries
 * with text or photo attachments, and track their resolution status in real-time.
 * Data is persisted locally in Room and synced with Cloud Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtsScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val doubts by viewModel.allDoubts.collectAsStateWithLifecycle()
    val batches by viewModel.allBatches.collectAsStateWithLifecycle()
    val currentUser = viewModel.getCurrentUser()

    var selectedSubject by remember { mutableStateOf("ALL") }
    val subjects = listOf("ALL", "PHYSICS", "CHEMISTRY", "MATHEMATICS", "BIOLOGY")

    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "RESOLVED", "PENDING"
    var showOnlyMyDoubts by remember { mutableStateOf(false) }

    var showAskDoubtDialog by remember { mutableStateOf(false) }
    var doubtToAnswer by remember { mutableStateOf<DoubtEntity?>(null) }
    var fullImagePreviewUrl by remember { mutableStateOf<String?>(null) }

    // Filtering logic
    val filteredDoubts = doubts.filter { doubt ->
        val matchesSubject = selectedSubject == "ALL" || doubt.subject.equals(selectedSubject, ignoreCase = true)
        val matchesStatus = when (selectedStatusFilter) {
            "RESOLVED" -> doubt.isAnswered || doubt.resolutionStatus == "RESOLVED"
            "PENDING" -> !doubt.isAnswered && doubt.resolutionStatus != "RESOLVED"
            else -> true
        }
        val matchesUser = !showOnlyMyDoubts || doubt.studentEmail.equals(currentUser.email, ignoreCase = true)
        matchesSubject && matchesStatus && matchesUser
    }

    val resolvedCount = doubts.count { it.isAnswered || it.resolutionStatus == "RESOLVED" }
    val pendingCount = doubts.size - resolvedCount

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAskDoubtDialog = true },
                containerColor = AmberGold,
                contentColor = RoyalNavy,
                modifier = Modifier.testTag("ask_doubt_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ask Doubt")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask Doubt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            // Header Section
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
                                text = "24x7 Doubt Engine",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Direct faculty resolution by Banshraj Sir & expert mentors",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }

                        // Firestore Sync Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firestore Synced",
                                    color = EmeraldGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Stats Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DoubtStatPill(
                            label = "Total Queries",
                            count = "${doubts.size}",
                            color = Color.White.copy(alpha = 0.15f),
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        DoubtStatPill(
                            label = "Resolved",
                            count = "$resolvedCount",
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            textColor = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        DoubtStatPill(
                            label = "Pending",
                            count = "$pendingCount",
                            color = AmberGold.copy(alpha = 0.2f),
                            textColor = AmberGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(subjects) { subj ->
                            val isSelected = selectedSubject == subj
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSubject = subj },
                                label = {
                                    Text(
                                        text = subj,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGold,
                                    selectedLabelColor = RoyalNavy,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Status Filter & My Doubts Toggle Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL", "PENDING", "RESOLVED").forEach { status ->
                        val isSelected = selectedStatusFilter == status
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedStatusFilter = status }
                        ) {
                            Text(
                                text = status,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (showOnlyMyDoubts) AmberGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (showOnlyMyDoubts) androidx.compose.foundation.BorderStroke(1.dp, AmberGold) else null,
                    modifier = Modifier.clickable { showOnlyMyDoubts = !showOnlyMyDoubts }
                ) {
                    Text(
                        text = if (showOnlyMyDoubts) "✓ My Doubts" else "My Doubts",
                        color = if (showOnlyMyDoubts) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Doubts Feed
            if (filteredDoubts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No doubts found in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Got stuck on a formula or problem? Click '+ Ask Doubt' below to upload your question.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredDoubts) { doubt ->
                        DoubtCard(
                            doubt = doubt,
                            currentUserRole = currentUser.role,
                            canAnswer = currentUser.role == "ADMIN" || currentUser.canCreateContent,
                            onUpvote = { viewModel.upvoteDoubt(doubt) },
                            onAnswerClick = { doubtToAnswer = doubt },
                            onImageClick = { url -> fullImagePreviewUrl = url }
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Image Preview Dialog
    fullImagePreviewUrl?.let { imageUrl ->
        Dialog(onDismissRequest = { fullImagePreviewUrl = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { fullImagePreviewUrl = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Doubt Image Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }

    // Ask Doubt Dialog (with Image or Text submission)
    if (showAskDoubtDialog) {
        AskDoubtDialog(
            batches = batches.map { it.title },
            onDismiss = { showAskDoubtDialog = false },
            onSubmit = { subject, batch, question, imageUriString ->
                viewModel.postDoubt(
                    subject = subject,
                    batchName = batch,
                    question = question,
                    imageUrl = imageUriString
                )
                showAskDoubtDialog = false
            }
        )
    }

    // Answer Doubt Dialog (Faculty/Admin)
    doubtToAnswer?.let { targetDoubt ->
        AnswerDoubtDialog(
            doubt = targetDoubt,
            onDismiss = { doubtToAnswer = null },
            onSubmit = { answerText, resolutionStatus ->
                viewModel.answerDoubt(targetDoubt, answerText, resolutionStatus)
                doubtToAnswer = null
            }
        )
    }
}

@Composable
private fun DoubtStatPill(
    label: String,
    count: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun DoubtCard(
    doubt: DoubtEntity,
    currentUserRole: String,
    canAnswer: Boolean,
    onUpvote: () -> Unit,
    onAnswerClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Subject, Batch, and Resolution Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = doubt.subject,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${doubt.batchName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Resolution Status Badge
                val isResolved = doubt.isAnswered || doubt.resolutionStatus == "RESOLVED"
                val statusText = if (isResolved) "RESOLVED" else doubt.resolutionStatus
                val statusColor = when (statusText) {
                    "RESOLVED" -> EmeraldGreen
                    "IN_REVIEW" -> Color(0xFF3B82F6)
                    else -> AmberGold
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isResolved) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = statusText,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = doubt.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            )

            // Optional Image Attachment Preview
            if (!doubt.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onImageClick(doubt.imageUrl) }
                ) {
                    Box(modifier = Modifier.height(180.dp)) {
                        AsyncImage(
                            model = doubt.imageUrl,
                            contentDescription = "Question Diagram / Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(topStart = 8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "🔍 Tap to view photo",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Meta and Upvote
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asked by ${doubt.studentName} (${doubt.studentEmail})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onUpvote() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Upvote",
                        tint = AmberGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${doubt.upvotes}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Solution Box (if answered)
            if (doubt.isAnswered && !doubt.answer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Faculty",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Faculty Verified Solution (${doubt.answeredBy ?: "Banshraj Sir"})",
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = doubt.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else if (canAnswer) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAnswerClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = "Answer",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Provide Faculty Solution & Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Ask Doubt Dialog supporting query submission with Text or Photo Attachment
 * using Android's Photo Picker (zero broad storage permissions).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskDoubtDialog(
    batches: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (subject: String, batch: String, question: String, imageUri: String?) -> Unit
) {
    var doubtSubject by remember { mutableStateOf("Physics") }
    var doubtBatch by remember { mutableStateOf(batches.firstOrNull() ?: "Lakshya JEE 2026") }
    var questionText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var customImageUrl by remember { mutableStateOf("") }

    // Google Play Policy Compliant Photo Picker (PickVisualMedia)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Academic Doubt", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject selection
                Text("Subject:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { subj ->
                        val isSelected = doubtSubject == subj
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { doubtSubject = subj }
                        ) {
                            Text(
                                text = subj,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Batch Selection Text Field
                OutlinedTextField(
                    value = doubtBatch,
                    onValueChange = { doubtBatch = it },
                    label = { Text("Course / Batch Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Question Description Field
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Describe your doubt or step where you got stuck...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                // Image Attachment Section (Photo Picker & URL)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Attach Problem Photo / Diagram (Optional)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (selectedImageUri != null) {
                        // Image selected preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Photo attached", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = { selectedImageUri = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove photo", tint = CoralRed)
                            }
                        }
                    } else {
                        // Photo Picker Button
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_doubt_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Pick Image",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Image from Gallery", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Alternative sample diagram / direct image URL
                        OutlinedTextField(
                            value = customImageUrl,
                            onValueChange = { customImageUrl = it },
                            label = { Text("Or Paste Image URL (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank()) {
                        val finalImage = selectedImageUri?.toString() ?: customImageUrl.ifBlank { null }
                        onSubmit(doubtSubject, doubtBatch, questionText, finalImage)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                shape = RoundedCornerShape(8.dp),
                enabled = questionText.isNotBlank(),
                modifier = Modifier.testTag("submit_doubt_button")
            ) {
                Text("Submit Doubt to Faculty")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Faculty Solution & Resolution Status Dialog
 */
@Composable
private fun AnswerDoubtDialog(
    doubt: DoubtEntity,
    onDismiss: () -> Unit,
    onSubmit: (answer: String, status: String) -> Unit
) {
    var answerText by remember { mutableStateOf(doubt.answer ?: "") }
    var resolutionStatus by remember { mutableStateOf("RESOLVED") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Post Faculty Solution", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Student Question (${doubt.studentName}):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = doubt.question, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text("Set Resolution Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("RESOLVED", "IN_REVIEW").forEach { status ->
                        val isSelected = resolutionStatus == status
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) (if (status == "RESOLVED") EmeraldGreen else Color(0xFF3B82F6)) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { resolutionStatus = status }
                        ) {
                            Text(
                                text = status,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Enter detailed step-by-step solution...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answerText.isNotBlank()) {
                        onSubmit(answerText, resolutionStatus)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                enabled = answerText.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Publish Solution & Update Status")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel")
            }
        }
    )
}
