package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BatchEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodel.AcademyTab
import com.example.ui.viewmodel.AcademyViewModel

@Composable
fun BatchesScreen(
    viewModel: AcademyViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val batches by viewModel.allBatches.collectAsStateWithLifecycle()
    var selectedGoal by remember { mutableStateOf("IIT-JEE 2026") }
    val currentUser = viewModel.getCurrentUser()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateBatchDialog by remember { mutableStateOf(false) }

    val filteredBatches = batches.filter {
        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            // Only visible/accessible to Admin / Content Creators
            if (currentUser.role == "ADMIN" || currentUser.canCreateContent) {
                FloatingActionButton(
                    onClick = { showCreateBatchDialog = true },
                    containerColor = AmberGold,
                    contentColor = RoyalNavy,
                    modifier = Modifier.testTag("create_batch_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Batch")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Batch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Top Search Bar + Coins (Matches Screenshot 3 & 5)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF1E293B))
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search for batches", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("₹", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFFB45309))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("0", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }

                // Selected Target Banner (Matches Screenshot 3 & 5: ELEC - AE/JE Change)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFDF4FF))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedGoal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Text(
                                text = "Change",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5),
                                modifier = Modifier.clickable { }
                            )
                        }
                    }
                }

                // Month End Offer Carousel (Matches Screenshot 5)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MONTH END",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color(0xFF881337)
                                    )
                                    Text(
                                        text = "OFFER ✨",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = Color(0xFFBE123C)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(46.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3 Offer batch pills (Matches Screenshot 5)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OfferBatchPill("Ashoka 2027", "Now @ ₹3,299", Modifier.weight(1f))
                                OfferBatchPill("SSC JE 2027", "Now @ ₹2,599", Modifier.weight(1f))
                                OfferBatchPill("RRB JE 2026", "Now @ ₹2,699", Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Section: Popular Courses (Matches Screenshot 3 & 5)
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Popular Courses",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Batches List (Matches Screenshot 3 & 5)
                items(filteredBatches) { batch ->
                    val isEnrolled = viewModel.isUserBatchEnrolled(batch.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("batch_item_${batch.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            // Top Faculty Banner Graphic (Matches Screenshot 3 & 5)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(
                                        when (batch.category) {
                                            "JEE" -> Color(0xFF1E1B4B)
                                            "NEET" -> Color(0xFF064E3B)
                                            else -> Color(0xFF581C87)
                                        }
                                    )
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Surface(
                                        color = AmberGold,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ELECTRICAL / JEE-NEET",
                                            color = RoyalNavy,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = batch.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Diploma Semester + Engineer Kit with Books",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Body details (Matches Screenshot 3)
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Foundation",
                                        color = Color(0xFFEA580C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Text(
                                            text = "HINGLISH",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = batch.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "📖 ${selectedGoal}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "📅 Starts on ${batch.startDate}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Price & Buttons Row (Matches Screenshot 3: Price + Buy Now + > arrow)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "₹${batch.discountPrice}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 17.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "₹${batch.originalPrice}",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        }
                                        Text(
                                            text = "45% OFF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (isEnrolled) {
                                            Button(
                                                onClick = { viewModel.selectBatch(batch) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Enrolled", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.enrollInBatch(batch.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                modifier = Modifier.height(38.dp)
                                            ) {
                                                Text("Buy Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Arrow button to view full syllabus & chapters (Screenshot 13)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clickable { viewModel.selectBatch(batch) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                                    contentDescription = "Details",
                                                    tint = Color(0xFF1E293B),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating AI Doubt Bot Avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E7FF))
                .clickable { viewModel.setTab(AcademyTab.DOUBTS) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👨‍🏫", fontSize = 28.sp)
            }
        }
    }

    // Admin Create Batch Dialog
    if (showCreateBatchDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("JEE") }
        var discountPriceStr by remember { mutableStateOf("2499") }
        var priceStr by remember { mutableStateOf("4999") }

        AlertDialog(
            onDismissRequest = { showCreateBatchDialog = false },
            title = { Text("Publish New Batch (Admin)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Batch Name (e.g. Aadhar JE 2029)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (AE/JE, JEE, NEET)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountPriceStr,
                            onValueChange = { discountPriceStr = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Original (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNewBatch(
                                title = title,
                                tagline = "Comprehensive Preparation Batch",
                                category = category,
                                price = priceStr.toIntOrNull() ?: 4999,
                                discountPrice = discountPriceStr.toIntOrNull() ?: 2499,
                                instructor = "Banshraj Sir",
                                validity = "1 Year Access",
                                features = "Live Lectures, Notes, DPPs, Tests"
                            )
                            showCreateBatchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy)
                ) {
                    Text("Publish Batch")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateBatchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OfferBatchPill(
    name: String,
    priceTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF7ED),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFDE047)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌸", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A3412),
                maxLines = 1
            )
            Text(
                text = priceTag,
                fontSize = 10.sp,
                color = Color(0xFF7C2D12),
                maxLines = 1
            )
        }
    }
}
